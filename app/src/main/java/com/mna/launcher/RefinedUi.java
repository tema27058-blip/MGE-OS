package com.mna.launcher;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.view.View;

final class FrostPanel extends Drawable {
 private static volatile Bitmap backdrop;
 private static int request=0;
 private static final java.util.concurrent.ExecutorService sampling=java.util.concurrent.Executors.newSingleThreadExecutor();
 private static final android.os.Handler main=new android.os.Handler(android.os.Looper.getMainLooper());
 private static final java.util.WeakHashMap<FrostPanel,Boolean> panels=new java.util.WeakHashMap<>();
 private static volatile int screenW=1,screenH=1;
 private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
 private final RectF rect=new RectF(),inner=new RectF();
 private final Path clip=new Path();
 private final Matrix matrix=new Matrix();
 private final int[] location=new int[2];
 private final int radius; private final boolean shine;
 private Shader edge,fill;private Bitmap previous;private BitmapShader sample;
 FrostPanel(int radius,boolean shine){this.radius=radius;this.shine=shine;synchronized(panels){panels.put(this,true);}}
 static void setViewport(int w,int h){if(w>0&&h>0){screenW=w;screenH=h;}}
 static synchronized void setWallpaper(Bitmap source,int width,int height){
  if(source==null)return;setViewport(width,height);final int generation=++request;
  sampling.execute(()->{
  int w=64,h=Math.min(192,Math.max(1,Math.round(64f*source.getHeight()/source.getWidth())));
  Bitmap tiny=Bitmap.createScaledBitmap(source,w,h,true);
  int[] pixels=new int[w*h],next=new int[w*h];tiny.getPixels(pixels,0,w,0,0,w,h);
  for(int pass=0;pass<3;pass++){
   for(int y=0;y<h;y++)for(int x=0;x<w;x++){
    int r=0,g=0,b=0,n=0;
    for(int dy=-2;dy<=2;dy++)for(int dx=-2;dx<=2;dx++){
     int c=pixels[Math.max(0,Math.min(h-1,y+dy))*w+Math.max(0,Math.min(w-1,x+dx))];r+=Color.red(c);g+=Color.green(c);b+=Color.blue(c);n++;
    }
    next[y*w+x]=Color.rgb(r/n,g/n,b/n);
   }
   int[] swap=pixels;pixels=next;next=swap;
  }
  final Bitmap ready=Bitmap.createBitmap(pixels,w,h,Bitmap.Config.ARGB_8888);
  main.post(()->{if(generation!=request)return;backdrop=ready;synchronized(panels){for(FrostPanel panel:panels.keySet())if(panel!=null)panel.invalidateSelf();}});
  });
 }
 @Override protected void onBoundsChange(Rect b){rect.set(b);inner.set(rect);inner.inset(1,1);clip.reset();clip.addRoundRect(rect,radius,radius,Path.Direction.CW);fill=new LinearGradient(rect.left,rect.top,rect.right,rect.bottom,new int[]{0x557b5567,0x55301f2c,0x7730212c},null,Shader.TileMode.CLAMP);edge=new LinearGradient(0,rect.top,0,rect.bottom,new int[]{0x99ffffff,0x14ffffff,0x48ffffff},new float[]{0,.42f,1},Shader.TileMode.CLAMP);}
 @Override public void draw(Canvas c){
  c.save();c.clipPath(clip);p.setStyle(Paint.Style.FILL);Bitmap b=backdrop;
  if(b!=null){if(b!=previous){previous=b;sample=new BitmapShader(b,Shader.TileMode.CLAMP,Shader.TileMode.CLAMP);}if(getCallback() instanceof View)((View)getCallback()).getLocationOnScreen(location);else{location[0]=0;location[1]=0;}float scale=Math.max((float)screenW/b.getWidth(),(float)screenH/b.getHeight());matrix.setScale(scale,scale);matrix.postTranslate((screenW-b.getWidth()*scale)/2-location[0],(screenH-b.getHeight()*scale)/2-location[1]);sample.setLocalMatrix(matrix);p.setShader(sample);p.setAlpha(205);c.drawRect(rect,p);p.setAlpha(255);}
  p.setShader(fill);c.drawRect(rect,p);
  if(shine){p.setShader(null);p.setColor(0x13ffffff);c.drawOval(rect.left-rect.width()*.2f,rect.top-rect.height()*.8f,rect.right+rect.width()*.25f,rect.top+rect.height()*.2f,p);}c.restore();
  if(shine){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.4f);p.setShader(edge);c.drawRoundRect(inner,radius,radius,p);p.setStyle(Paint.Style.FILL);}p.setShader(null);
 }
 @Override public void getOutline(Outline o){o.setRoundRect(getBounds(),radius);}
 @Override public void setAlpha(int alpha){} @Override public void setColorFilter(ColorFilter f){} @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}

final class SystemGlyph extends View {
 private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);private final Path path=new Path();private final String symbol;
 SystemGlyph(Context context,String symbol){super(context);this.symbol=symbol;p.setColor(0xfff7edf1);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.75f);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);setFocusable(true);setClickable(true);}
 @Override protected void onDraw(Canvas c){super.onDraw(c);float size=getResources().getDisplayMetrics().density*23;c.save();c.translate((getWidth()-size)/2,(getHeight()-size)/2);c.scale(size/24,size/24);
  if(symbol.equals("⚙")){c.drawLine(14,17,5,17,p);c.drawLine(19,7,10,7,p);c.drawCircle(17,17,3,p);c.drawCircle(7,7,3,p);}
  else if(symbol.equals("＋")){c.drawLine(12,5,12,19,p);c.drawLine(5,12,19,12,p);}
  else if(symbol.equals("✎")){path.reset();path.moveTo(4,16);path.lineTo(15,5);path.lineTo(19,9);path.lineTo(8,20);path.lineTo(4,20);path.close();c.drawPath(path,p);c.drawLine(13,7,17,11,p);}
  else{c.drawLine(6,6,18,18,p);c.drawLine(18,6,6,18,p);}c.restore();
 }
}
