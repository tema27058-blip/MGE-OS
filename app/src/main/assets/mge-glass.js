(()=>{
'use strict';
if(window.MgeGlass)return;
const root=document.getElementById('root'),bg=document.getElementById('bg');if(!root)return;
const key='mge.glass.v1';let state={quality:'balanced',theme:'auto',opacity:.62,coloros:true};
try{const saved=JSON.parse(localStorage.getItem(key)||'{}');if(saved&&typeof saved==='object')state={...state,...saved};}catch(e){}
const targets='.dock,.sheet,.folderPanel,.folderBox,.popover,.card,.quick-settings';
let frame=0,last=0,until=0,bitmapKey='',cache='',generation=0,brightness=.25,stopped=document.hidden;
const reduced=matchMedia('(prefers-reduced-motion: reduce)');
const css=document.createElement('style');css.id='mge-glass-style';css.textContent=`
#root{--mg-tint:20,16,25;--mg-text:#f6edf3;--mg-edge:rgba(255,255,255,.23);--mg-alpha:.62;--mg-blur:7px}
#root[data-mg-theme="light"]{--mg-tint:239,233,241;--mg-text:#211921;--mg-edge:rgba(255,255,255,.68)}
.mge-glass{isolation:isolate;position:relative;background:rgba(var(--mg-tint),var(--mg-alpha))!important;border:1px solid var(--mg-edge)!important;box-shadow:inset 0 1px 0 #ffffff30,inset 0 -1px 0 #00000015,0 12px 28px #00000026!important;color:var(--mg-text)}
.sheet.mge-glass{position:absolute}
.mge-glass:before{content:'';position:absolute;inset:0;z-index:-1;border-radius:inherit;pointer-events:none;background-image:linear-gradient(155deg,#ffffff16,transparent 35%,#ffffff04),var(--mg-wall,none);background-size:100% 100%,var(--mg-wall-size,100% 100%);background-position:0 0,var(--mg-wall-position,center);opacity:.46}
.mge-glass.mg-live{-webkit-backdrop-filter:blur(var(--mg-blur));backdrop-filter:blur(var(--mg-blur))}
#root[data-mg-quality="economy"] .mge-glass{box-shadow:inset 0 1px #ffffff20,0 4px 10px #0003!important}
#root[data-mg-quality="economy"] .mge-glass:before{opacity:.24}
#root[data-mg-motion="on"] .sheet{transition:transform var(--dur,280ms) cubic-bezier(.18,.85,.24,1),visibility var(--dur,280ms)}
#root[data-mg-motion="on"] .pages{transition-timing-function:cubic-bezier(.18,.85,.24,1)}
#root[data-mg-motion="on"] .pages.drag{transition:none!important}
#root[data-mg-motion="on"] .app .icon{transition:transform 85ms ease-out}
#root[data-mg-motion="on"] .app:active .icon{transform:scale(.91)}
#root[data-mg-motion="off"] .mge-glass{backdrop-filter:none;-webkit-backdrop-filter:none}
#mge-glass-controls{padding:14px;border:1px solid #ffffff26;border-radius:17px;margin:12px 0}
#mge-glass-controls h3{font:600 15px system-ui;margin:0 0 14px}
#mge-glass-controls label{display:flex;align-items:center;justify-content:space-between;gap:12px;margin:14px 0;font-size:12px}
#mge-glass-controls p{font-size:11px;line-height:1.6;opacity:.7}
`;
document.head.append(css);
function quality(){const q=state.quality==='auto'?(root.dataset.mgeQuality||'balanced'):state.quality;return ['high','balanced','economy'].includes(q)?q:'balanced';}
function persist(){try{localStorage.setItem(key,JSON.stringify(state));}catch(e){}}
function apply(){const q=quality();root.dataset.mgQuality=q;root.dataset.mgTheme=state.theme==='auto'?(brightness>.58?'light':'dark'):state.theme;root.dataset.mgMotion=state.coloros&&!reduced.matches?'on':'off';root.style.setProperty('--mg-alpha',Math.min(.92,Math.max(.25,Number(state.opacity)||.62)));root.style.setProperty('--mg-blur',q==='high'?'12px':q==='economy'?'0px':'6px');schedule(0);}
function source(){if(!bg)return '';const m=getComputedStyle(bg).backgroundImage.match(/url\(["']?(.*?)["']?\)/);return m?m[1]:'';}
async function refreshWallpaper(){const src=source(),q=quality(),tag=src+'|'+q;if(!src||tag===bitmapKey)return;bitmapKey=tag;const token=++generation;
 try{const image=new Image();image.src=src;await image.decode();if(token!==generation||stopped)return;
  const w=q==='high'?160:112,h=Math.max(1,Math.round(w*root.clientHeight/Math.max(1,root.clientWidth))),canvas=document.createElement('canvas');canvas.width=w;canvas.height=h;const c=canvas.getContext('2d',{willReadFrequently:true});if(!c)return;
  const scale=Math.max(w/image.naturalWidth,h/image.naturalHeight);const dw=image.naturalWidth*scale,dh=image.naturalHeight*scale;c.drawImage(image,(w-dw)/2,(h-dh)/2,dw,dh);
  const pixels=c.getImageData(0,0,w,h).data;let sum=0,count=0;for(let i=0;i<pixels.length;i+=64){sum+=(pixels[i]*.2126+pixels[i+1]*.7152+pixels[i+2]*.0722)/255;count++;}brightness=count?sum/count:.25;
  const output=document.createElement('canvas');output.width=w;output.height=h;const out=output.getContext('2d');out.filter=q==='economy'?'blur(3px)':'blur(7px)';out.drawImage(canvas,-8,-8,w+16,h+16);cache=output.toDataURL('image/jpeg',.72);root.style.setProperty('--mg-wall','url("'+cache+'")');apply();
 }catch(e){if(token===generation){cache='';root.style.removeProperty('--mg-wall');apply();}}
}
function update(now){frame=0;if(stopped)return;const q=quality(),gap=q==='high'?33:q==='balanced'?67:200;if(now-last<gap){if(now<until)frame=requestAnimationFrame(update);return;}last=now;
 const rr=root.getBoundingClientRect(),panels=[...root.querySelectorAll('.mge-glass')].filter(el=>{const r=el.getBoundingClientRect();return r.width>1&&r.height>1&&r.bottom>rr.top&&r.top<rr.bottom&&getComputedStyle(el).visibility!=='hidden';});
 let live=0;for(const el of root.querySelectorAll('.mge-glass')){const visible=panels.includes(el),allow=visible&&q!=='economy'&&live<(q==='high'?2:1);el.classList.toggle('mg-live',allow);if(allow)live++;if(!visible)continue;const r=el.getBoundingClientRect(),position=`${Math.round(rr.left-r.left)}px ${Math.round(rr.top-r.top)}px`,size=`${Math.round(rr.width)}px ${Math.round(rr.height)}px`;if(el.style.getPropertyValue('--mg-wall-position')!==position)el.style.setProperty('--mg-wall-position',position);if(el.style.getPropertyValue('--mg-wall-size')!==size)el.style.setProperty('--mg-wall-size',size);}
 if(now<until)frame=requestAnimationFrame(update);
}
function schedule(ms=360){if(stopped)return;until=Math.max(until,performance.now()+ms);if(!frame)frame=requestAnimationFrame(update);}
function mount(){for(const el of root.querySelectorAll(targets))if(!el.classList.contains('mge-glass'))el.classList.add('mge-glass');
 if(!document.getElementById('mge-glass-controls')){const parent=root.querySelector('.settings .scroll')||root.querySelector('.settings');if(parent){const panel=document.createElement('section');panel.id='mge-glass-controls';const title=document.createElement('h3');title.textContent='Liquid Glass и переходы';panel.append(title);
 function select(title,key,options){const label=document.createElement('label');label.append(document.createTextNode(title));const s=document.createElement('select');for(const [v,t]of options){const o=document.createElement('option');o.value=v;o.textContent=t;s.append(o);}s.value=state[key];s.onchange=()=>{state[key]=s.value;persist();apply();refreshWallpaper();};label.append(s);panel.append(label);}
 select('Качество','quality',[['auto','Автоматически'],['high','Высокое'],['balanced','Сбалансированное'],['economy','Экономия']]);select('Тема стекла','theme',[['auto','По обоям'],['dark','Тёмная'],['light','Светлая']]);
 const label=document.createElement('label');label.textContent='Прозрачность';const range=document.createElement('input');range.type='range';range.min='25';range.max='92';range.value=String(state.opacity*100);range.oninput=()=>{state.opacity=Number(range.value)/100;persist();apply();};label.append(range);panel.append(label);
 const motion=document.createElement('label');motion.textContent='Эффект ColorOS (переходы лаунчера)';const check=document.createElement('input');check.type='checkbox';check.checked=state.coloros;check.onchange=()=>{state.coloros=check.checked;persist();apply();};motion.append(check);panel.append(motion);const note=document.createElement('p');note.textContent='Этот эффект меняет панели и отклик иконок внутри лаунчера. Он не управляет закрытием окон других приложений. FPS на устройстве не измерен.';panel.append(note);parent.append(panel);}}
 schedule(0);
}
let mounted=false;const tree=new MutationObserver(()=>{if(mounted)return;mounted=true;requestAnimationFrame(()=>{mounted=false;mount();});});tree.observe(root,{subtree:true,childList:true});
if(bg)new MutationObserver(()=>{refreshWallpaper();schedule();}).observe(bg,{attributes:true,attributeFilter:['style','class']});
root.addEventListener('transitionrun',()=>schedule(400),true);root.addEventListener('scroll',()=>schedule(100),{capture:true,passive:true});root.addEventListener('pointermove',()=>schedule(100),{passive:true});root.addEventListener('pointerup',()=>schedule(350),{passive:true});
document.addEventListener('visibilitychange',()=>{stopped=document.hidden;if(stopped){cancelAnimationFrame(frame);frame=0;for(const el of root.querySelectorAll('.mg-live'))el.classList.remove('mg-live');}else{bitmapKey='';refreshWallpaper();schedule();}});
addEventListener('resize',()=>{bitmapKey='';refreshWallpaper();schedule();});
reduced.addEventListener('change',apply);
window.MgeGlass={refresh(){bitmapKey='';refreshWallpaper();mount();apply();},settings:()=>({...state})};mount();apply();refreshWallpaper();
})();
