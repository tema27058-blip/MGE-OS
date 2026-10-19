from pathlib import Path
import re
root=Path(__file__).resolve().parents[1]
p=root/'app/src/main/assets/index.html'
addon=root/'app/src/main/assets/mge-glass.js'
html=p.read_text(encoding='utf-8')
js=addon.read_text(encoding='utf-8')
if 'id="mge-glass-addon"' in html:
    raise SystemExit('Already installed; do not duplicate the add-on')
if '<script' not in html or 'id="root"' not in html:
    raise SystemExit('Unexpected HTML: patch stopped without changing files')
backup=p.with_name('index.before-glass.html')
if backup.exists():
    raise SystemExit('Backup exists: inspect it before patching')
backup.write_text(html,encoding='utf-8')
script='<script id="mge-glass-addon">\n'+js.replace('</script','<\\/script')+'\n</script>'
position=html.lower().rfind('</body>')
patched=html[:position]+script+html[position:] if position>=0 else html+script
p.write_text(patched,encoding='utf-8')
print('Glass add-on installed. Existing HTML and state keys preserved; backup kept.')
