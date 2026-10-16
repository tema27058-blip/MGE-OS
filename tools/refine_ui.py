from pathlib import Path
p=Path('app/src/main/java/com/mna/launcher/MainActivity.java')
s=p.read_text(encoding='utf-8')
if '// MNA_REFINED_UI' in s:
    raise SystemExit('Already refined')
def change(a,b):
    global s
    if s.count(a)!=1: raise SystemExit('Unexpected source anchor: '+a[:80])
    s=s.replace(a,b,1)
change('private Drawable glass(int radius){return new Glass(dp(radius),prefs.getBoolean("shine",true));}', 'private Drawable glass(int radius){return new FrostPanel(dp(radius),prefs.getBoolean("shine",true));}')
change('private TextView topButton(String s,Runnable r){TextView v=label(s,22);v.setGravity(Gravity.CENTER);v.setOnClickListener(x->r.run());press(v);return v;}', 'private View topButton(String s,Runnable r){SystemGlyph v=new SystemGlyph(this,s);v.setContentDescription(s.equals("＋")?"Добавить":s.equals("✎")?"Редактировать":s.equals("⚙")?"Настройки":"Закрыть");v.setOnClickListener(x->r.run());press(v);return v;}')
change('private final int ACCENT=0xffc4b8ff;', '''private final int ACCENT=0xffe9b8ca;
 private final java.util.LinkedHashMap<String,Tile> tilePool=new java.util.LinkedHashMap<>(32,.75f,true);
 private final java.util.HashMap<String,String> tileKeys=new java.util.HashMap<>();
 private Tile obtainTile(Item i){
  String key=i.type+"|"+i.component+"|"+i.title+"|"+i.file+"|"+i.widget+"|"+i.children+"|"+edit+"|"+prefs.getBoolean("labels",true)+"|"+prefs.getInt("labelSize",11)+"|"+prefs.getInt("round",22)+"|"+prefs.getBoolean("shine",true)+"|"+System.identityHashCode(byId.get(i.component));
  Tile t=tilePool.get(i.id);
  if(t==null||!key.equals(tileKeys.get(i.id))){t=new Tile(i);tilePool.put(i.id,t);tileKeys.put(i.id,key);}
  t.animate().cancel();t.setAlpha(1);t.setScaleX(1);t.setScaleY(1);t.setTranslationY(0);
  if(t.getParent() instanceof android.view.ViewGroup)((android.view.ViewGroup)t.getParent()).removeView(t);
  while(tilePool.size()>64){String first=tilePool.keySet().iterator().next();tilePool.remove(first);tileKeys.remove(first);}
  return t;
 }''')
change('Tile tile=new Tile(i);','Tile tile=obtainTile(i);')
change('private long duration(){return prefs.getInt("speed",210);}', 'private long duration(){return Math.max(150,Math.min(280,prefs.getInt("speed",200)));}')
change('restore();if(state!=null)', 'restore();if(!prefs.getBoolean("glassRefinement1",false)){prefs.edit().putBoolean("glassRefinement1",true).putBoolean("parallax",false).putBoolean("wallMotion",false).putInt("speed",200).apply();}if(state!=null)')
a=s.index(' private void setPage(int target)');b=s.index(' private void launch(',a)
s=s[:a]+''' private void setPage(int target){
  int next=Math.max(0,Math.min(target,lastPage()+1)),direction=Integer.compare(next,page);page=next;
  workspace.animate().cancel();workspace.setTranslationX(0);workspace.setAlpha(1);selected="";render(false);
  if(direction!=0&&!minimal()){workspace.setTranslationX(dp(22)*direction);workspace.setAlpha(.92f);workspace.animate().translationX(0).alpha(1).setDuration(duration()).setInterpolator(new android.view.animation.PathInterpolator(.2f,.8f,.2f,1f)).start();}
  wall.animate().cancel();wall.setTranslationX(0);save();
 }
'''+s[b:]
a=s.index(' private class Workspace extends FrameLayout');b=s.index(' private LinearLayout panel(',a)
w=s[a:b].replace('e.getX()','e.getRawX()').replace('e.getY()','e.getRawY()')
w=w.replace('x>=t.getX()&&x<t.getX()+t.getWidth()&&y>=t.getY()&&y<t.getY()+t.getHeight()', 'e.getX()>=t.getX()&&e.getX()<t.getX()+t.getWidth()&&e.getY()>=t.getY()&&e.getY()<t.getY()+t.getHeight()')
w=w.replace('setTranslationX(dx*.22f)','setTranslationX(clamp(dx,-getWidth()*.25f,getWidth()*.25f))')
s=s[:a]+w+s[b:]
change('void setBitmap(Bitmap b){bitmap=b;invalidate();}', 'void setBitmap(Bitmap b){bitmap=b;FrostPanel.setWallpaper(b,getWidth(),getHeight());invalidate();}')
s=s.replace('"                         "','"    ·    "').replace('lp.dimAmount=.3f','lp.dimAmount=.12f')
s=s.replace('prefs.getBoolean("wallMotion",true)','prefs.getBoolean("wallMotion",false)')
s=s.replace('toggle(box,"Мягкое движение обоев","wallMotion",true,','toggle(box,"Мягкое движение обоев","wallMotion",false,')
s=s.replace('toggle(box,"Параллакс при свайпе","parallax",true,()->{});','')
s=s.replace('Mna OS 2.0','Mna OS 2.1').replace('MnaLauncher 2.0','MnaLauncher 2.1')
s=s.replace('if(!minimal()){if(e.getActionMasked()==MotionEvent.ACTION_DOWN)','if(!minimal()&&!edit){if(e.getActionMasked()==MotionEvent.ACTION_DOWN)')
change('@Override protected void onDraw(Canvas c){c.drawColor(0xff10152b);', '@Override protected void onDraw(Canvas c){FrostPanel.setViewport(getWidth(),getHeight());c.drawColor(0xff10152b);')
p.write_text('// MNA_REFINED_UI\n'+s,encoding='utf-8')
print('Applied refined native UI')
