import {rotate, launcher, heightAt, flight, gamepadValues} from './model.mjs';
const $ = id => document.getElementById(id);
const num = id => Number($(id).value);
let state = {x:1.83,y:.65,yaw:0,width:.45,length:.45,trail:[],motors:{},time:0,status:'pronto'};
let shot = null, examples = {}, lastPad = 0, polling = false, padBusy = false;
const keys = new Set(), top = $('top'), side = $('side');
const configIds = ['left','right','width','length','track','max_speed','ticks_per_m','duration','x','y','initial-yaw','invert_left','invert_right'];
const launchIds = ['offset-x','offset-y','height','speed','elevation','azimuth','target-x','target-y','target-h'];
function config() {
  const data = {};
  configIds.forEach(id => { const input=$(id); data[id==='initial-yaw'?'yaw':id]=input.type==='checkbox'?input.checked:input.type==='number'?Number(input.value):input.value; });
  return data;
}
function persist() {
  try { const values={}; [...configIds,...launchIds].forEach(id=>values[id]=$(id).type==='checkbox'?$(id).checked:$(id).value);
    localStorage.setItem('meca-sim-v01',JSON.stringify({source:$('source').value,values})); } catch {}
}
function notice(text) {$('message').textContent=text;}
async function api(path, data={}) {
  const response = await fetch('/api/'+path,{method:'POST',headers:{'Content-Type':'application/json','X-Sim-Token':window.SIM_TOKEN},body:JSON.stringify(data)});
  const result = await response.json();
  if (!response.ok) throw new Error(result.error||'Falha no servidor');
  return result;
}
const action = (id, callback) => $(id).addEventListener('click',async()=>{try{notice('');await callback();}catch(e){notice(e.message);}});
action('compile',async()=>{persist();shot=null;await api('run',{source:$('source').value,config:config()});});
action('start',()=>api('start'));
action('stop',()=>api('stop'));
action('reset',async()=>{shot=null;await api('reset',{config:config()});});
action('export',async()=>{
  const blob=new Blob([JSON.stringify({version:'0.1',config:config(),launcher:Object.fromEntries(launchIds.map(id=>[id,num(id)])),result:state},null,2)],{type:'application/json'});
  const url=URL.createObjectURL(blob), a=document.createElement('a');a.href=url;a.download='meca-sim-resultado.json';a.click();setTimeout(()=>URL.revokeObjectURL(url),1000);
});
action('shoot',async()=>{
  const values=launchValues();if(!values.valid)throw new Error('Confira altura, velocidade, elevação e coordenadas do lançador.');
  shot={...values,origin:launcher(state,values.lateral,values.forward),yaw:state.yaw,start:performance.now()};
});
document.querySelectorAll('[data-tab]').forEach(button=>button.addEventListener('click',()=>{
  document.querySelectorAll('[data-tab]').forEach(b=>b.classList.toggle('selected',b===button));
  ['code','robot','launcher'].forEach(name=>$('tab-'+name).hidden=name!==button.dataset.tab);
}));
$('source').addEventListener('input',persist);
[...configIds,...launchIds].forEach(id=>$(id).addEventListener('change',persist));
$('example').addEventListener('change',()=>{if(examples[$('example').value]){$('source').value=examples[$('example').value];persist();}});
$('source').addEventListener('keydown',event=>{if(event.key==='Tab'){event.preventDefault();const input=event.target,start=input.selectionStart,end=input.selectionEnd;input.setRangeText('    ',start,end,'end');persist();}});

function launchValues() {
  const v={lateral:num('offset-x'),forward:num('offset-y'),height:num('height'),speed:num('speed'),elevation:num('elevation'),azimuth:num('azimuth'),tx:num('target-x'),ty:num('target-y'),th:num('target-h')};
  v.valid=Object.values(v).every(Number.isFinite)&&v.height>=0&&v.height<=5&&v.speed>0&&v.speed<=30&&v.elevation>=0&&v.elevation<90&&v.th>=0&&v.th<=5;
  return v;
}
function canvasContext(canvas) {
  const rect=canvas.getBoundingClientRect(),dpr=window.devicePixelRatio||1;
  const w=rect.width,h=rect.height;
  if(canvas.width!==Math.round(w*dpr)||canvas.height!==Math.round(h*dpr)){canvas.width=Math.round(w*dpr);canvas.height=Math.round(h*dpr);}
  const ctx=canvas.getContext('2d');ctx.setTransform(dpr,0,0,dpr,0,0);ctx.clearRect(0,0,w,h);return {ctx,w,h};
}
function geometry() {const r=top.getBoundingClientRect(),scale=Math.min((r.width-70)/3.66,(r.height-40)/3.66);return {scale,ox:(r.width-3.66*scale)/2,oy:(r.height+3.66*scale)/2};}
function drawTop(now) {
  const {ctx:c,w,h}=canvasContext(top),g=geometry();
  const project=(x,y)=>[g.ox+x*g.scale,g.oy-y*g.scale];
  c.lineWidth=1;c.font='10px system-ui';
  for(let i=0;i<=6;i++){const p=i*3.66/6,[x,y]=project(p,p);c.strokeStyle='#242c37';c.beginPath();c.moveTo(x,g.oy);c.lineTo(x,g.oy-3.66*g.scale);c.moveTo(g.ox,y);c.lineTo(g.ox+3.66*g.scale,y);c.stroke();c.fillStyle='#627085';c.fillText(p.toFixed(2),x-9,g.oy+15);c.fillText(p.toFixed(2),g.ox-28,y+3);}
  c.strokeStyle='#536277';c.strokeRect(g.ox,g.oy-3.66*g.scale,3.66*g.scale,3.66*g.scale);
  c.strokeStyle='#ff852e80';c.lineWidth=2;c.beginPath();state.trail.forEach((p,i)=>{const q=project(...p);i?c.lineTo(...q):c.moveTo(...q);});c.stroke();
  const corner=(x,y)=>{const r=rotate(x,y,state.yaw);return project(state.x+r[0],state.y+r[1]);};
  c.beginPath();[[-1,-1],[1,-1],[1,1],[-1,1]].forEach(([x,y],i)=>{const p=corner(x*state.width/2,y*state.length/2);i?c.lineTo(...p):c.moveTo(...p);});c.closePath();c.fillStyle='#ff852e20';c.fill();c.strokeStyle='#ff852e';c.stroke();
  const center=project(state.x,state.y),front=corner(0,state.length*.6);c.beginPath();c.moveTo(...center);c.lineTo(...front);c.stroke();c.fillStyle='#fff';c.beginPath();c.arc(...center,3,0,Math.PI*2);c.fill();
  for(const sign of [-1,1]){const a=corner(sign*state.width/2,-state.length*.24),b=corner(sign*state.width/2,state.length*.24);c.strokeStyle='#d6dfeb';c.lineWidth=5;c.beginPath();c.moveTo(...a);c.lineTo(...b);c.stroke();}
  const v=launchValues();if(!v.valid)return;
  const origin=launcher(state,v.lateral,v.forward),start=project(...origin),target=project(v.tx,v.ty),dir=rotate(0,1,state.yaw+v.azimuth),f=flight(v.height,v.speed,v.elevation),end=project(origin[0]+dir[0]*f.range,origin[1]+dir[1]*f.range);
  c.lineWidth=1;c.setLineDash([5,5]);c.strokeStyle='#6b7584';c.beginPath();c.moveTo(...start);c.lineTo(...target);c.stroke();c.strokeStyle='#51d1bb';c.beginPath();c.moveTo(...start);c.lineTo(...end);c.stroke();c.setLineDash([]);
  c.fillStyle='#51d1bb';c.beginPath();c.arc(...start,4,0,Math.PI*2);c.fill();c.strokeStyle='#91a8ff';c.lineWidth=2;c.beginPath();c.arc(...target,9,0,Math.PI*2);c.moveTo(target[0]-14,target[1]);c.lineTo(target[0]+14,target[1]);c.moveTo(target[0],target[1]-14);c.lineTo(target[0],target[1]+14);c.stroke();
  if(shot){const elapsed=(now-shot.start)/1000,d=rotate(0,1,shot.yaw+shot.azimuth),ff=flight(shot.height,shot.speed,shot.elevation);if(elapsed<=ff.time){const distance=shot.speed*Math.cos(shot.elevation*Math.PI/180)*elapsed,q=project(shot.origin[0]+d[0]*distance,shot.origin[1]+d[1]*distance);c.fillStyle='#fff4a9';c.beginPath();c.arc(...q,5,0,Math.PI*2);c.fill();}}
  $('bounds').textContent=state.x<0||state.x>3.66||state.y<0||state.y>3.66?'Centro fora do campo · sem colisão':'Frente +Y · yaw positivo ↶';
}
top.addEventListener('click',event=>{const rect=top.getBoundingClientRect(),g=geometry();$('target-x').value=((event.clientX-rect.left-g.ox)/g.scale).toFixed(3);$('target-y').value=((g.oy-event.clientY+rect.top)/g.scale).toFixed(3);persist();});
function drawSide(now) {
  const {ctx:c,w,h}=canvasContext(side),v=shot||launchValues();if(!v.valid)return;
  const origin=shot?.origin||launcher(state,v.lateral,v.forward),yaw=shot?.yaw??state.yaw,dir=rotate(0,1,yaw+v.azimuth),dx=v.tx-origin[0],dy=v.ty-origin[1],along=dx*dir[0]+dy*dir[1],cross=Math.abs(dx*dir[1]-dy*dir[0]),f=flight(v.height,v.speed,v.elevation);
  const a=v.azimuth*Math.PI/180,half=(Math.abs(Math.sin(a))*state.width+Math.abs(Math.cos(a))*state.length)/2,offset=v.lateral*(-Math.sin(a))+v.forward*Math.cos(a),left=-offset-half,right=-offset+half;
  const minX=Math.min(-.6,left-.1,along-.1),maxX=Math.max(3.66,f.range+.3,along+.3,right+.1),peak=v.height+(v.speed*Math.sin(v.elevation*Math.PI/180))**2/(2*9.81),maxH=Math.max(1.5,peak+.3,v.th+.3),sx=(w-55)/(maxX-minX),sy=(h-35)/maxH;
  const point=(x,y)=>[40+(x-minX)*sx,h-25-y*sy];c.font='10px system-ui';c.lineWidth=1;
  for(let i=0;i<=4;i++){const value=i*maxH/4,p=point(0,value);c.strokeStyle='#242c37';c.beginPath();c.moveTo(...p);c.lineTo(w-10,p[1]);c.stroke();c.fillStyle='#627085';c.fillText(value.toFixed(1)+'m',5,p[1]+3);}
  c.strokeStyle='#536277';c.beginPath();c.moveTo(...point(0,0));c.lineTo(...point(maxX,0));c.stroke();
  // Projeção do chassi no eixo do tiro; o lançador está em x=0.
  c.fillStyle='#ff852e20';c.strokeStyle='#ff852e';const p=point(left,.30);c.fillRect(p[0],p[1],(right-left)*sx,.30*sy);c.strokeRect(p[0],p[1],(right-left)*sx,.30*sy);
  c.strokeStyle='#51d1bb';c.lineWidth=2;c.beginPath();for(let i=0;i<=120;i++){const x=f.range*i/120,q=point(x,Math.max(0,heightAt(x,v.height,v.speed,v.elevation)));i?c.lineTo(...q):c.moveTo(...q);}c.stroke();
  const target=point(along,v.th);c.fillStyle='#91a8ff';c.fillRect(target[0]-5,target[1]-5,10,10);
  if(shot){const elapsed=(now-shot.start)/1000;if(elapsed<=f.time){const x=v.speed*Math.cos(v.elevation*Math.PI/180)*elapsed,q=point(x,heightAt(x,v.height,v.speed,v.elevation));c.fillStyle='#fff4a9';c.beginPath();c.arc(...q,5,0,Math.PI*2);c.fill();}}
  const at=along>=0&&along<=f.range?heightAt(along,v.height,v.speed,v.elevation):null;
  $('shot-info').textContent=`Prévia manual · alcance ${f.range.toFixed(2)} m · desvio lateral do alvo ${cross.toFixed(2)} m · ${at===null?'alvo fora do alcance frontal':`altura na distância do alvo ${at.toFixed(2)} m (alvo ${v.th.toFixed(2)} m)`}.`;
}
function readouts() {
  $('status').textContent=state.status;$('clock').textContent=state.time.toFixed(2).padStart(5,'0')+' s';
  $('px').textContent=state.x.toFixed(3)+' m';$('py').textContent=state.y.toFixed(3)+' m';$('yaw').textContent=state.yaw.toFixed(1)+'°';
  $('start').disabled=state.status!=='inicializando';$('compile').disabled=['compilando','inicializando','executando'].includes(state.status);
  $('telemetry').textContent=state.telemetry||'Aguardando telemetry.update()';$('logs').textContent=state.logs||'Nenhum erro de compilação registrado.';
  $('motors').replaceChildren();Object.entries(state.motors).forEach(([name,m])=>{const node=document.createElement('div');node.className='motor';const label=document.createElement('strong');label.textContent=name;const value=document.createElement('span');value.textContent=`P ${m.power.toFixed(2)} · ${m.encoder} ticks`;const bar=document.createElement('div');bar.className='bar';const fill=document.createElement('i');fill.style.width=Math.abs(m.power)*100+'%';bar.append(fill);node.append(label,value,bar);$('motors').append(node);});
}
async function poll() {
  if(polling)return;polling=true;
  try{const response=await fetch('/api/state');if(!response.ok)throw Error('Falha ao consultar simulação');state=await response.json();readouts();}
  catch(e){$('status').textContent='Servidor desconectado';notice('Não foi possível acessar o servidor local. Confira o terminal do Python.');}
  finally{polling=false;}
}
const controls=['w','s','a','d','b','x','y',' ','ArrowUp','ArrowDown','ArrowLeft','ArrowRight'];
document.addEventListener('keydown',e=>{if(['INPUT','TEXTAREA','SELECT'].includes(e.target.tagName))return;const key=e.key.length===1?e.key.toLowerCase():e.key;if(controls.includes(key)){e.preventDefault();keys.add(key);}});
document.addEventListener('keyup',e=>keys.delete(e.key.length===1?e.key.toLowerCase():e.key));
window.addEventListener('blur',()=>{keys.clear();api('gamepad').catch(()=>{});});
document.addEventListener('visibilitychange',()=>{if(document.hidden){keys.clear();api('stop').catch(()=>{});}});
async function sendPads(now) {
  if(now-lastPad<40||padBusy)return;lastPad=now;padBusy=true;
  try{const pads=navigator.getGamepads?Array.from(navigator.getGamepads()).filter(Boolean):[],p1=gamepadValues(pads[0]),p2=gamepadValues(pads[1]);
    if(!pads[0]){p1[1]=Number(keys.has('s'))-Number(keys.has('w'));p1[2]=Number(keys.has('d'))-Number(keys.has('a'));p1[6]=Number(keys.has(' '));['b','x','y'].forEach((k,i)=>p1[7+i]=Number(keys.has(k)));['ArrowUp','ArrowDown','ArrowLeft','ArrowRight'].forEach((k,i)=>p1[16+i]=Number(keys.has(k)));}
    $('controller').textContent=pads[0]?(pads[0].mapping==='standard'?`Controle 1 conectado${pads[1]?' · controle 2 conectado':''}`:'Controle sem mapeamento padrão: não suportado nesta versão'):'Controle: pressione um botão · teclado W/A/S/D disponível';
    await api('gamepad',{'1':p1,'2':p2});
  }catch{}finally{padBusy=false;}
}
function frame(now){if(shot&&(now-shot.start)/1000>flight(shot.height,shot.speed,shot.elevation).time+.8)shot=null;drawTop(now);drawSide(now);if(!document.hidden)sendPads(now);requestAnimationFrame(frame);}
async function init() {
  try{const response=await fetch('/examples');examples=await response.json();Object.keys(examples).forEach(name=>{const option=document.createElement('option');option.value=name;option.textContent=name;$('example').append(option);});
    const saved=JSON.parse(localStorage.getItem('meca-sim-v01')||'null');if(saved){$('source').value=saved.source||'';Object.entries(saved.values||{}).forEach(([id,value])=>{if($(id))$(id).type==='checkbox'?$(id).checked=Boolean(value):$(id).value=value;});}else{$('source').value=Object.values(examples)[0];}
  }catch(e){notice('Não foi possível carregar exemplos ou configurações: '+e.message);}
  await poll();setInterval(poll,100);requestAnimationFrame(frame);
}
init();
