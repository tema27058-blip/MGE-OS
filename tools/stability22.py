from pathlib import Path
p=Path('app/src/main/java/com/mna/launcher/MainActivity.java')
s=p.read_text(encoding='utf-8')
if '// MNA_REFINED_UI' not in s: raise SystemExit('Run refine_ui.py first')
if '// MNA_STABILITY_22' in s: raise SystemExit('Already patched')
def change(a,b):
    global s
    if s.count(a)!=1: raise SystemExit('Unexpected source anchor: '+a[:90])
    s=s.replace(a,b,1)
change('private int lastPage(){int n=0;for(Item i:layout)n=Math.max(n,i.page);return n;}', '''private int lastPage(){int n=0;for(Item i:layout)n=Math.max(n,i.page);return n;}
 private int desktopPageCount(){return PagePolicy.count(lastPage(),prefs.getInt("desktopPages",1));}
 private void createDesktopPage(){int count=desktopPageCount();prefs.edit().putInt("desktopPages",count+1).apply();setPage(count);}
 private boolean isSettingsEntry(){ComponentName c=getIntent().getComponent();return c!=null&&c.getClassName().endsWith(".Settings");}
 private void chooseIconStyle(){new android.app.AlertDialog.Builder(this).setTitle("Оформление значков").setItems(new String[]{"Единые квадратные плитки","Оригинальные значки"},(dialog,which)->{prefs.edit().putBoolean("uniformIcons",which==0).putBoolean("iconStyleChosen",true).apply();loadApps();}).show();}
''')
change('Math.min(target,lastPage()+1)','PagePolicy.clamp(target,desktopPageCount())')
s=s.replace('Math.max(page+1,lastPage()+1)','desktopPageCount()')
s=s.replace('setPage(lastPage()+1)','createDesktopPage()')
s=s.replace('n<=lastPage()+1','n<desktopPageCount()')
change('private void render(boolean animate){if(workspace.getWidth()==0', 'private void render(boolean animate){page=PagePolicy.clamp(page,desktopPageCount());if(workspace.getWidth()==0')
change('top.addView(topButton("⚙",this::settings),new LinearLayout.LayoutParams(dp(46),dp(46)));','')
change('r.activityInfo.packageName.equals(getPackageName())','(r.activityInfo.packageName.equals(getPackageName())&&!r.activityInfo.name.endsWith(".Settings"))')
change('icon.draw(c);a.icon.prepareToDraw();','icon.draw(c);if(prefs.getBoolean("uniformIcons",true))a.icon=IconTile.wrap(a.icon);a.icon.prepareToDraw();')
change('loadWall();loadApps();', 'loadWall();loadApps();if(isSettingsEntry())ui.post(this::settings);')
change('if(!launched)render(false);}', 'if(!launched)render(false);if(isSettingsEntry())ui.post(this::settings);}')
change('save();workspace.post(()->{render(false);renderDock();});', '''for(App app:apps){if(app.component.getPackageName().equals(getPackageName())&&app.component.getClassName().endsWith(".Settings")){boolean exists=false;for(Item item:layout)if(item.component.equals(app.id))exists=true;if(!exists)placeApp(app,false);}}
 save();workspace.post(()->{render(false);renderDock();if(!prefs.getBoolean("iconStyleChosen",false))chooseIconStyle();});''')
change('private void settings(){LinearLayout box=panel("Mna OS 2.1");','private void settings(){LinearLayout box=panel("Mna OS 2.2");action(box,"Оформление значков",this::chooseIconStyle);')
change('getAssets().open("wall"+n+".jpg")','getAssets().open("burgundy.jpg")')
change('String[] names={"Aurora","Jade","Bloom"};for(int n=0;n<3;n++){','String[] names={"Burgundy"};for(int n=0;n<1;n++){')
change('restore();if(!prefs.getBoolean("glassRefinement1",false))', 'restore();if(!prefs.getBoolean("burgundy22",false)){prefs.edit().putBoolean("customWall",false).putBoolean("burgundy22",true).putInt("dim",0).apply();}if(!prefs.getBoolean("glassRefinement1",false))')
change('opts.inPreferredConfig=Bitmap.Config.RGB_565;','opts.inPreferredConfig=Bitmap.Config.ARGB_8888;')
s=s.replace('MnaLauncher 2.1','MnaLauncher 2.2')
p.write_text('// MNA_STABILITY_22\n'+s,encoding='utf-8')
# Apply neutral overlays, retaining the existing off-thread cached wallpaper blur.
ui=Path('app/src/main/java/com/mna/launcher/RefinedUi.java')
t=ui.read_text(encoding='utf-8')
t=t.replace('0x557b5567,0x55301f2c,0x7730212c','0x32ffffff,0x14090b12,0x24251a22').replace('p.setAlpha(205)','p.setAlpha(232)')
ui.write_text(t,encoding='utf-8')
print('Applied bounded pages, Settings entry, consistent icon tiles and Burgundy default')
