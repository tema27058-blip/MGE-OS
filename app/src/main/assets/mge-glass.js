(()=>{
'use strict';
if(window.MgeGlass)return;
const root=document.getElementById('root'),bg=document.getElementById('bg'),pages=document.querySelector('.pages');if(!root)return;
const KEY='mge.glass.v1';
let state={glass:true,strength:'balanced',wallBlur:true,theme:'auto',opacity:.62,coloros:true,iconDim:.18,iconRadius:19,iconSize:52,grid:4,labels:true,labelSize:10,glow:.55,heroDim:.15,heroZoom:true,dockAlpha:.8};
try{const saved=JSON.parse(localStorage.getItem(KEY)||'{}');if(saved&&typeof saved==='object')state={...state,...saved};}catch(e){}
const reduced=matchMedia('(prefers-reduced-motion: reduce)');
let frame=0,until=0,last=0,generation=0,wallKey='',brightness=.25,stopped=document.hidden;
const css=document.createElement('style');css.id='mge-glass-style';css.textContent=`
#root{--mg-alpha:.62;--mg-tint:20,16,25;--mg-text:#f6edf3;--mg-edge:rgba(255,255,255,.23);--mg-blur:6px;--mg-icon-dim:.18;--mg-icon-radius:19px;--mg-icon-size:52px;--mg-cols:4;--mg-label:10px;--mg-glow:.55;--mg-hero-dim:.15;--mg-dock-alpha:.8}
#root[data-mg-theme="light"]{--mg-tint:239,233,241;--mg-text:#211921;--mg-edge:rgba(255,255,255,.7)}
#root{--size:var(--mg-icon-size);--cols:var(--mg-cols);--round:var(--mg-icon-radius);--label:var(--mg-label);--glow:var(--mg-glow);--dim:var(--mg-hero-dim);--dockalpha:var(--mg-dock-alpha)}
#root.no-labels .label{display:none}
.mg-panel{isolation:isolate;position:relative;background:rgba(var(--mg-tint),var(--mg-alpha))!important;border:1px solid var(--mg-edge)!important;box-shadow:inset 0 1px 0 #ffffff2e,inset 0 -1px 0 #00000014,0 14px 30px #0000002b!important;color:var(--mg-text)}
.mg-panel:before{content:'';position:absolute;inset:0;z-index:-1;border-radius:inherit;pointer-events:none;background-image:linear-gradient(155deg,#ffffff17,transparent 36%,#ffffff05),var(--mg-wall,none);background-size:100% 100%,var(--mg-wall-size,100% 100%);background-position:0 0,var(--mg-wall-position,center);opacity:.42}
.mg-panel.mg-live{-webkit-backdrop-filter:blur(var(--mg-blur));backdrop-filter:blur(var(--mg-blur))}
#root[data-mg-glass="off"] .mg-panel{background:rgba(33,12,24,var(--mg-dock-alpha))!important;border-color:#ffffff0e!important;box-shadow:inset 0 1px #ffffff0c!important}
#root[data-mg-glass="off"] .mg-panel:before,#root[data-mg-glass="off"] .mg-panel.mg-live{background-image:none;-webkit-backdrop-filter:none;backdrop-filter:none}
#root[data-mg-strength="economy"] .mg-panel{box-shadow:inset 0 1px #ffffff1d,0 5px 12px #0003!important}
#root[data-mg-strength="economy"] .mg-panel:before{opacity:.22}
#root[data-mg-wallblur="off"] .mg-panel:before{background-image:linear-gradient(155deg,#ffffff17,transparent 36%,#ffffff05)}
#root[data-mg-motion="on"] .pages{transition-timing-function:cubic-bezier(.18,.85,.24,1)}
#root[data-mg-motion="on"] .pages.drag{transition:none!important}
#root[data-mg-motion="on"] .sheet{transition:transform var(--dur) cubic-bezier(.18,.85,.24,1),visibility var(--dur)}
#root[data-mg-motion="on"] .app .icon{transition:transform 90ms cubic-bezier(.2,.8,.25,1)}
#root[data-mg-motion="on"] .app:active .icon,#root[data-mg-motion="on"] .app.pressed .icon{transform:scale(.9)}
#root[data-mg-motion="on"] .enter .desktopGrid .app{animation:iconIn 230ms cubic-bezier(.2,.8,.25,1) both;animation-delay:calc(var(--i)*var(--cascade))}
#root[data-mg-motion="off"] .hero,#root[data-mg-motion="off"] .hero .frame img,#root[data-mg-motion="off"] .hero .halo,#root[data-mg-motion="off"] .hero .frame:before{animation:none!important}
#root[data-mg-motion="off"] #light{animation:none!important}
#root[data-mg-motion="off"] .mg-panel{-webkit-backdrop-filter:none;backdrop-filter:none}
#root[data-mg-motion="off"] .pages{transition:none!important}
#root[data-mg-motion="off"] .sheet{transition:none!important}
#root[data-mg-motion="off"] .app .icon{transition:none!important}
#root[data-mg-motion="off"] .enter .desktopGrid .app{animation:none!important}
#root[data-mg-icondim="on"] .icon:after{content:'';position:absolute;inset:0;border-radius:inherit;background:rgba(0,0,0,var(--mg-icon-dim));pointer-events:none}
#mge-glass-controls{border:1px solid #ffffff26;border-radius:17px;padding:0 13px;margin:11px 0;background:#ffffff07}
#mge-glass-controls summary{padding:16px 0;font-weight:600;color:#efc5d4;cursor:pointer}
#mge-glass-controls .row{border-bottom:1px solid #ffffff08}
#mge-glass-controls .note{margin:12px 0 16px}
`;
document.head.append(css);
function strength(){const s=state.strength==='auto'?(root.dataset.mgeQuality||'balanced'):state.strength;return ['high','balanced','economy'].includes(s)?s:'balanced';}
function save(){try{localStorage.setItem(KEY,JSON.stringify(state));}catch(e){}}
function apply(){const s=strength();root.dataset.mgGlass=state.glass?'on':'off';root.dataset.mgStrength=s;root.dataset.mgWallblur=state.wallBlur?'on':'off';root.dataset.mgMotion=state.coloros&&!reduced.matches?'on':'off';root.dataset.mgIcondim=state.iconDim>0?'on':'off';root.dataset.mgTheme=state.theme==='auto'?(brightness>.58?'light':'dark'):state.theme;root.classList.toggle('no-labels',!state.labels);
 root.style.setProperty('--mg-alpha',Math.min(.92,Math.max(.2,Number(state.opacity)||.62)));root.style.setProperty('--mg-blur',s==='high'?'11px':s==='economy'?'0px':'5px');root.style.setProperty('--mg-icon-dim',Math.min(.75,Math.max(0,Number(state.iconDim)||0)));root.style.setProperty('--mg-icon-radius',Math.min(28,Math.max(8,Number(state.iconRadius)||19))+'px');root.style.setProperty('--mg-icon-size',Math.min(72,Math.max(38,Number(state.iconSize)||52))+'px');root.style.setProperty('--mg-cols',Math.min(6,Math.max(3,Number(state.grid)||4)));root.style.setProperty('--mg-label',Math.min(14,Math.max(8,Number(state.labelSize)||10))+'px');root.style.setProperty('--mg-glow',Math.min(1,Math.max(0,Number(state.glow)||.55)));root.style.setProperty('--mg-hero-dim',Math.min(.7,Math.max(0,Number(state.heroDim)||.15)));root.style.setProperty('--mg-dock-alpha',Math.min(1,Math.max(.2,Number(state.dockAlpha)||.8)));schedule(0);}
function source(){if(!bg)return '';const m=getComputedStyle(bg).backgroundImage.match(/url\(["']?(.*?)["']?\)/);return m?m[1]:'';}
async function wallpaper(){const src=source(),s=strength(),tag=src+'|'+s+'|'+(state.wallBlur?'1':'0');if(!src||tag===wallKey)return;wallKey=tag;const token=++generation;
 try{const image=new Image();image.src=src;await image.decode();if(token!==generation||stopped)return;
  const width=s==='high'?160:112,height=Math.max(1,Math.round(width*root.clientHeight/Math.max(1,root.clientWidth))),canvas=document.createElement('canvas');canvas.width=width;canvas.height=height;const c=canvas.getContext('2d',{willReadFrequently:true});if(!c)return;
  const scale=Math.max(width/image.naturalWidth,height/image.naturalHeight),dw=image.naturalWidth*scale,dh=image.naturalHeight*scale;c.drawImage(image,(width-dw)/2,(height-dh)/2,dw,dh);
  const pixels=c.getImageData(0,0,width,height).data;let sum=0,count=0;for(let i=0;i<pixels.length;i+=64){sum+=(pixels[i]*.2126+pixels[i+1]*.7152+pixels[i+2]*.0722)/255;count++;}brightness=count?sum/count:.25;
  const output=document.createElement('canvas');output.width=width;output.height=height;const out=output.getContext('2d');out.filter=state.wallBlur?(s==='economy'?'blur(3px)':'blur(7px)'):'none';out.drawImage(canvas,-8,-8,width+16,height+16);const cache=output.toDataURL('image/jpeg',.72);root.style.setProperty('--mg-wall','url("'+cache+'")');apply();
 }catch(e){if(token===generation){root.style.removeProperty('--mg-wall');apply();}}
}
function update(now){frame=0;if(stopped)return;const s=strength(),gap=s==='high'?33:s==='balanced'?67:200;if(now-last<gap){if(now<until)frame=requestAnimationFrame(update);return;}last=now;
 const rr=root.getBoundingClientRect(),panels=[...root.querySelectorAll('.mg-panel')].filter(el=>{const r=el.getBoundingClientRect();return r.width>1&&r.height>1&&r.bottom>rr.top&&r.top<rr.bottom&&getComputedStyle(el).visibility!=='hidden';});
 let live=0;for(const el of root.querySelectorAll('.mg-panel')){const visible=panels.includes(el),allow=visible&&state.glass&&s!=='economy'&&live<(s==='high'?2:1);el.classList.toggle('mg-live',allow);if(allow)live++;if(!visible)continue;const r=el.getBoundingClientRect(),position=`${Math.round(rr.left-r.left)}px ${Math.round(rr.top-r.top)}px`,size=`${Math.round(rr.width)}px ${Math.round(rr.height)}px`;if(el.style.getPropertyValue('--mg-wall-position')!==position)el.style.setProperty('--mg-wall-position',position);if(el.style.getPropertyValue('--mg-wall-size')!==size)el.style.setProperty('--mg-wall-size',size);}
 if(now<until)frame=requestAnimationFrame(update);
}
function schedule(ms=360){if(stopped)return;until=Math.max(until,performance.now()+ms);if(!frame)frame=requestAnimationFrame(update);}
function mount(){for(const el of root.querySelectorAll('.dock,.sheet,.folderPanel,.folderBox,.popover,.modal .box,.quick-settings'))if(!el.classList.contains('mg-panel'))el.classList.add('mg-panel');
 if(!document.getElementById('mge-glass-controls')){const parent=root.querySelector('.settings .scroll');if(parent){const details=document.createElement('details');details.id='mge-glass-controls';const summary=document.createElement('summary');summary.textContent='Liquid Glass, иконки и переходы';details.append(summary);
 const row=(title,control)=>{const r=document.createElement('div');r.className='row';const span=document.createElement('span');span.textContent=title;r.append(span,control);details.append(r);};
 const select=(key,options,title)=>{const s=document.createElement('select');for(const [v,t]of options){const o=document.createElement('option');o.value=v;o.textContent=t;s.append(o);}s.value=state[key];s.onchange=()=>{state[key]=s.value;save();apply();wallpaper();};row(title,s);};
 const range=(key,min,max,step,title)=>{const input=document.createElement('input');input.type='range';input.min=min;input.max=max;input.step=step;input.value=state[key];input.oninput=()=>{state[key]=Number(input.value);save();apply();};row(title,input);};
 const toggle=(key,title)=>{const input=document.createElement('input');input.type='checkbox';input.checked=!!state[key];input.onchange=()=>{state[key]=input.checked;save();apply();wallpaper();};row(title,input);};
 select('strength',[['auto','Автоматически'],['high','Высокое'],['balanced','Сбалансированное'],['economy','Экономия']],'Качество стекла');select('theme',[['auto','По обоям'],['dark','Тёмная'],['light','Светлая']],'Тема стекла');range('opacity',.2,.92,.02,'Прозрачность панелей');toggle('wallBlur','Размытые обои под стеклом');toggle('glass','Liquid Glass');toggle('coloros','Плавные переходы и отклик');range('iconDim',0,.75,.02,'Затемнение иконок');range('iconRadius',8,28,1,'Скругление иконок');range('iconSize',38,72,1,'Размер иконок');range('grid',3,6,1,'Колонок на странице');toggle('labels','Подписи иконок');range('labelSize',8,14,1,'Размер подписей');range('glow',0,1,.05,'Свечение фото');range('heroDim',0,.7,.05,'Затемнение фото');toggle('heroZoom','Анимация фото');range('dockAlpha',.2,1,.05,'Плотность дока');const note=document.createElement('p');note.className='note';note.textContent='Стекло и переходы работают внутри лаунчера. Они не управляют закрытием окон сторонних приложений и не измеряют FPS. Настройки хранятся локально.';details.append(note);parent.append(details);}}
 schedule(0);}
let queued=false;const tree=new MutationObserver(()=>{if(queued)return;queued=true;requestAnimationFrame(()=>{queued=false;mount();});});tree.observe(root,{subtree:true,childList:true});
if(bg)new MutationObserver(()=>{wallKey='';wallpaper();schedule();}).observe(bg,{attributes:true,attributeFilter:['style','class']});
if(pages)pages.addEventListener('transitionrun',()=>schedule(420),true);
root.addEventListener('scroll',()=>schedule(100),{capture:true,passive:true});root.addEventListener('pointermove',()=>schedule(100),{passive:true});root.addEventListener('pointerup',()=>schedule(380),{passive:true});
document.addEventListener('visibilitychange',()=>{stopped=document.hidden;if(stopped){cancelAnimationFrame(frame);frame=0;for(const el of root.querySelectorAll('.mg-live'))el.classList.remove('mg-live');}else{wallKey='';wallpaper();schedule();}});
addEventListener('resize',()=>{wallKey='';wallpaper();schedule();});
reduced.addEventListener('change',apply);
window.MgeGlass={refresh(){wallKey='';wallpaper();mount();apply();},settings:()=>({...state})};mount();apply();wallpaper();
})();
