package com.mna.launcher;
import android.graphics.*;
final class IconTile {
 private IconTile(){}
 static Bitmap wrap(Bitmap source){
  int size=source.getWidth();Bitmap tile=Bitmap.createBitmap(size,size,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(tile);
  Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);float pad=size*.025f,radius=size*.23f;
  RectF shape=new RectF(pad,pad,size-pad,size-pad);Path clip=new Path();clip.addRoundRect(shape,radius,radius,Path.Direction.CW);
  canvas.save();canvas.clipPath(clip);paint.setShader(new LinearGradient(0,0,size,size,new int[]{0xff674452,0xff392936,0xff211d2b},null,Shader.TileMode.CLAMP));canvas.drawRect(shape,paint);paint.setShader(null);
  float inset=size*.15f;canvas.drawBitmap(source,null,new RectF(inset,inset,size-inset,size-inset),paint);
  canvas.restore();paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(Math.max(1,size*.009f));paint.setColor(0x55ffffff);canvas.drawRoundRect(shape,radius,radius,paint);return tile;
 }
}
