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
  // Keep the trusted UI inside system bars, including on Android 15 edge-to-edge.
  web.setOnApplyWindowInsetsListener((v,in)->{if(Build.VERSION.SDK_INT>=30){android.graphics.Insets s=in.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout()|WindowInsets.Type.ime());v.setPadding(s.left,s.top,s.right,s.bottom);}else v.setPadding(in.getSystemWindowInsetLeft(),in.getSystemWindowInsetTop(),in.getSystemWindowInsetRight(),in.getSystemWindowInsetBottom());return in;});
  web.requestApplyInsets();
  WebSettings w=web.getSettings();w.setJavaScriptEnabled(true);w.setDomStorageEnabled(true);w.setAllowFileAccess(false);w.setAllowContentAccess(false);w.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);w.setJavaScriptCanOpenWindowsAutomatically(false);
  web.addJavascriptInterface(new Bridge(),"MGE");
  web.setWebViewClient(new WebViewClient(){
   @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){return !trusted(r.getUrl());}
   @Override public WebResourceResponse shouldInterceptRequest(WebView v,WebResourceRequest r){
    Uri u=r.getUrl();if(trusted(u)&&"/index.html".equals(u.getPath())){try{return new WebResourceResponse("text/html","UTF-8",getAssets().open("index.html"));}catch(IOException e){}}
    return new WebResourceResponse("text/plain","UTF-8",new ByteArrayInputStream(new byte[0]));
   }
   @Override public void onPageFinished(WebView v,String url){push();}
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
 @Override protected void onResume(){super.onResume();if(web!=null){web.onResume();push();web.evaluateJavascript("window.onHomeResume&&window.onHomeResume()",null);}}
 @Override protected void onPause(){if(web!=null)web.onPause();super.onPause();}
 @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);web.evaluateJavascript("window.goHome&&window.goHome()",null);}
 @Override public void onBackPressed(){web.evaluateJavascript("window.goHome&&window.goHome()",null);}
 @Override protected void onDestroy(){if(receiverRegistered)unregisterReceiver(changes);worker.shutdownNow();if(fileCallback!=null)fileCallback.onReceiveValue(null);if(web!=null){web.removeJavascriptInterface("MGE");web.destroy();web=null;}super.onDestroy();}
}
