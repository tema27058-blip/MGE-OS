from pathlib import Path
root=Path(__file__).resolve().parents[1]
p=root/'app/src/main/assets/index.html'
addon=root/'app/src/main/assets/mge-glass.js'
html=p.read_text(encoding='utf-8')
js=addon.read_text(encoding='utf-8')
if 'id="mge-glass-addon"' in html:raise SystemExit('Already installed')
if 'id="root"' not in html or 'class="pages"' not in html or 'class="dock"' not in html:raise SystemExit('Unexpected MGE OS HTML; stopped without changes')
backup=p.with_name('index.before-glass.html')
if backup.exists():raise SystemExit('Backup already exists; inspect before retry')
backup.write_text(html,encoding='utf-8')
tag='<script id="mge-glass-addon">\n'+js.replace('</script','<\\/script')+'\n</script>'
pos=html.lower().rfind('</body>')
p.write_text((html[:pos]+tag+html[pos:]) if pos>=0 else html+tag,encoding='utf-8')
print('Installed glass, icon and motion controls.')
