package os.mge.launcher;
import android.app.*;
import android.app.role.RoleManager;
import android.os.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;
import android.view.*;
import android.webkit.*;
import android.widget.Toast;
import org.json.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends Activity {
 private WebView web;
 private final ExecutorService worker=Executors.newSingleThreadExecutor();
 private volatile String appJson="[]";
 private final Map<String,ComponentName> allowed=new ConcurrentHashMap<>();
 private boolean receiverRegistered=false;
 private ValueCallback<Uri[]> fileCallback;
 private final BroadcastReceiver changes=new BroadcastReceiver(){public void onReceive(Context c,Intent i){loadApps();}};
 private boolean trusted(Uri uri){return "https".equals(uri.getScheme())&&"appassets.androidplatform.net".equals(uri.getHost())&&uri.getPort()==-1;}
 @Override public void onCreate(Bundle state){super.onCreate(state);
  web=new WebView(this);web.setBackgroundColor(Color.rgb(8,5,7));setContentView(web);
  getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
  web.setOnApplyWindowInsetsListener((v,in)->{
   if(Build.VERSION.SDK_INT>=30){android.graphics.Insets nav=in.getInsets(WindowInsets.Type.navigationBars()|WindowInsets.Type.ime());android.graphics.Insets cut=in.getInsetsIgnoringVisibility(WindowInsets.Type.displayCutout());v.setPadding(Math.max(nav.left,cut.left),cut.top,Math.max(nav.right,cut.right),nav.bottom);}
   else {DisplayCutout cut=in.getDisplayCutout();v.setPadding(in.getSystemWindowInsetLeft(),cut==null?0:cut.getSafeInsetTop(),in.getSystemWindowInsetRight(),in.getSystemWindowInsetBottom());}
   return in;
  });
  web.requestApplyInsets();hideStatus();
  if(Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,()->{if(web!=null)web.evaluateJavascript("window.handleBack&&window.handleBack()",null);});
  WebSettings w=web.getSettings();w.setJavaScriptEnabled(true);w.setDomStorageEnabled(true);w.setAllowFileAccess(false);w.setAllowContentAccess(false);w.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);w.setJavaScriptCanOpenWindowsAutomatically(false);
  web.addJavascriptInterface(new Bridge(),"MGE");
  web.setWebViewClient(new WebViewClient(){
   @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){return !trusted(r.getUrl());}
   @Override public WebResourceResponse shouldInterceptRequest(WebView v,WebResourceRequest r){
    Uri u=r.getUrl();if(trusted(u)&&"/index.html".equals(u.getPath())){try{return new WebResourceResponse("text/html","UTF-8",getAssets().open("index.html"));}catch(IOException e){}}
    return new WebResourceResponse("text/plain","UTF-8",new ByteArrayInputStream(new byte[0]));
   }
   @Override public void onPageFinished(WebView v,String url){push();sendStatus();}
  });
  web.setWebChromeClient(new WebChromeClient(){@Override public boolean onShowFileChooser(WebView v,ValueCallback<Uri[]> callback,FileChooserParams params){if(fileCallback!=null)fileCallback.onReceiveValue(null);fileCallback=callback;Intent i=new Intent(Intent.ACTION_GET_CONTENT).setType("image/*").addCategory(Intent.CATEGORY_OPENABLE);try{startActivityForResult(i,41);return true;}catch(Exception e){fileCallback.onReceiveValue(null);fileCallback=null;return false;}}});
  IntentFilter f=new IntentFilter();f.addAction(Intent.ACTION_PACKAGE_ADDED);f.addAction(Intent.ACTION_PACKAGE_REMOVED);f.addAction(Intent.ACTION_PACKAGE_CHANGED);f.addDataScheme("package");
  if(Build.VERSION.SDK_INT>=33)registerReceiver(changes,f,Context.RECEIVER_EXPORTED);else registerReceiver(changes,f);receiverRegistered=true;
  web.loadUrl("https://appassets.androidplatform.net/index.html");loadApps();
 }
 private void push(){if(web!=null)web.evaluateJavascript("window.receiveApps&&window.receiveApps("+appJson+");window.homeState&&window.homeState("+isHome()+");",null);}
 private boolean isHome(){RoleManager r=getSystemService(RoleManager.class);return r!=null&&r.isRoleHeld(RoleManager.ROLE_HOME);}
 private void loadApps(){worker.execute(()->{
  try{PackageManager pm=getPackageManager();Intent i=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
   List<ResolveInfo> list=pm.queryIntentActivities(i,0);list.sort((a,b)->String.CASE_INSENSITIVE_ORDER.compare(a.loadLabel(pm).toString(),b.loadLabel(pm).toString()));JSONArray array=new JSONArray();Set<String> seen=new HashSet<>();Map<String,ComponentName> next=new HashMap<>();
   for(ResolveInfo r:list){if(r.activityInfo==null||!r.activityInfo.exported||r.activityInfo.packageName.equals(getPackageName()))continue;ComponentName c=new ComponentName(r.activityInfo.packageName,r.activityInfo.name);String id=c.flattenToString();if(!seen.add(id))continue;JSONObject o=new JSONObject();o.put("id",id);o.put("name",r.loadLabel(pm).toString());
    try{Drawable dr=r.loadIcon(pm);Bitmap bm=Bitmap.createBitmap(80,80,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(bm);dr.setBounds(8,8,72,72);dr.draw(canvas);ByteArrayOutputStream out=new ByteArrayOutputStream();bm.compress(Bitmap.CompressFormat.PNG,100,out);o.put("icon","data:image/png;base64,"+android.util.Base64.encodeToString(out.toByteArray(),android.util.Base64.NO_WRAP));bm.recycle();}catch(Exception e){o.put("icon","");}
    array.put(o);next.put(id,c);
   }
   allowed.putAll(next);allowed.keySet().retainAll(next.keySet());appJson=array.toString();runOnUiThread(()->push());
  }catch(Exception e){runOnUiThread(()->Toast.makeText(this,"Не удалось обновить приложения",Toast.LENGTH_SHORT).show());}
 });}
 private final Handler statusHandler=new Handler(Looper.getMainLooper());
 private boolean foreground=false;
 private final Runnable statusTick=new Runnable(){public void run(){if(!foreground)return;sendStatus();statusHandler.postDelayed(this,10000);}};
 private void hideStatus(){
  if(Build.VERSION.SDK_INT>=30){WindowInsetsController c=getWindow().getInsetsController();if(c!=null){c.hide(WindowInsets.Type.statusBars());c.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);}}
  else getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
 }
 @Override public void onWindowFocusChanged(boolean focus){super.onWindowFocusChanged(focus);if(focus)hideStatus();}
 private void sendStatus(){if(web==null)return;try{
  JSONObject o=new JSONObject();BatteryManager bm=(BatteryManager)getSystemService(BATTERY_SERVICE);int level=bm==null?-1:bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);o.put("battery",level>=0&&level<=100?level:-1);o.put("charging",bm!=null&&bm.isCharging());
  android.net.ConnectivityManager cm=(android.net.ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);android.net.NetworkCapabilities n=cm==null?null:cm.getNetworkCapabilities(cm.getActiveNetwork());String network="Нет сети";
  if(n!=null){if(n.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI))network="Wi-Fi";else if(n.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR))network="Моб. сеть";else if(n.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET))network="Ethernet";else if(n.hasTransport(android.net.NetworkCapabilities.TRANSPORT_VPN))network="VPN";else network="Сеть";}
  o.put("network",network);web.evaluateJavascript("window.deviceStatus&&window.deviceStatus("+o+")",null);
 }catch(Exception ignored){}}
 public class Bridge {
  @JavascriptInterface public void refresh(){loadApps();}
  @JavascriptInterface public void setHome(){runOnUiThread(()->{try{RoleManager r=getSystemService(RoleManager.class);if(r!=null&&r.isRoleAvailable(RoleManager.ROLE_HOME)&&!r.isRoleHeld(RoleManager.ROLE_HOME))startActivityForResult(r.createRequestRoleIntent(RoleManager.ROLE_HOME),42);else startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));}catch(Exception e){try{startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));}catch(Exception ignored){}}});}
  @JavascriptInterface public void launch(String id,float x,float y,float width,float height,boolean animate){runOnUiThread(()->{
   ComponentName c=allowed.get(id);if(c==null)return;
   try{Intent intent=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setComponent(c).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
    float scale=getResources().getDisplayMetrics().density;int xx=Math.max(0,Math.min(web.getWidth(),Math.round(x*scale)))+web.getPaddingLeft();int yy=Math.max(0,Math.min(web.getHeight(),Math.round(y*scale)))+web.getPaddingTop();int ww=Math.max(1,Math.round(Math.min(width,200)*scale)),hh=Math.max(1,Math.round(Math.min(height,200)*scale));
    if(animate)startActivity(intent,ActivityOptions.makeScaleUpAnimation(web,xx,yy,ww,hh).toBundle());else startActivity(intent);
   }catch(Exception e){Toast.makeText(MainActivity.this,"Не удалось открыть приложение",Toast.LENGTH_SHORT).show();loadApps();}
  });}
  @JavascriptInterface public void appInfo(String id){runOnUiThread(()->{ComponentName c=allowed.get(id);if(c!=null)try{startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+c.getPackageName())));}catch(Exception ignored){}});}
 }
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request==41&&fileCallback!=null){fileCallback.onReceiveValue(result==RESULT_OK?WebChromeClient.FileChooserParams.parseResult(result,data):null);fileCallback=null;}push();}
 @Override protected void onResume(){super.onResume();foreground=true;hideStatus();statusHandler.removeCallbacks(statusTick);statusHandler.post(statusTick);if(web!=null){web.onResume();push();web.evaluateJavascript("window.onHomeResume&&window.onHomeResume()",null);}}
 @Override protected void onPause(){foreground=false;statusHandler.removeCallbacks(statusTick);if(web!=null){web.evaluateJavascript("window.onHomePause&&window.onHomePause()",null);web.onPause();}super.onPause();}
 @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);web.evaluateJavascript("window.goHome&&window.goHome()",null);}
 @Override public void onBackPressed(){web.evaluateJavascript("window.handleBack&&window.handleBack()",null);}
 @Override protected void onDestroy(){if(receiverRegistered)unregisterReceiver(changes);worker.shutdownNow();if(fileCallback!=null)fileCallback.onReceiveValue(null);if(web!=null){web.removeJavascriptInterface("MGE");web.destroy();web=null;}super.onDestroy();}
}
