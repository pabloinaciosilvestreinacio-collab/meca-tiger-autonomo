import assert from "node:assert/strict";

const clip = (v, min, max) => Math.max(min, Math.min(max, v));
const wrap = v => { if (!Number.isFinite(v)) return NaN;
  v %= 360; if (v>180) v-=360; if(v< -180)v+=360; return v; };

let assertions = 0;
function ok(condition,message){assert.ok(condition,message);assertions++;}

function headingControl({back,signSelected,confirmed,x,y,yaw,target,hold,banned}){
  let active = false;
  let xOut = x;
  const valid = Number.isFinite(yaw);
  if (!back) banned=false;
  const enabled=back&&confirmed&&signSelected!==0&&valid&&!banned&&
    Math.abs(x)<=0.08&&Math.abs(y)>0.12;
  if(enabled){
    if(!hold){target=yaw;hold=true;}
    const error=wrap(target-yaw);
    if(Math.abs(error)>12){hold=false;banned=true;}
    else {xOut=clip(signSelected*0.012*error,-0.15,0.15);active=true;}
  } else hold=false;
  if(!valid){hold=false;banned=back;}
  return {active,xOut,target,hold,banned};
}
function mechanisms(s,btn,dt) {
  const noCommands=!btn.lb&&!btn.rb&&!btn.a&&!btn.b;
  if(btn.x)s.armed=false;
  else if(btn.y&&!s.yprev&&noCommands)s.armed=true;
  s.yprev=btn.y;
  if(!s.armed||!btn.rb)s.warmTime=0;
  else s.warmTime+=dt;
  const shooter=s.armed&&btn.rb;
  const ready=shooter&&s.warmTime>=0.80;
  let intake=0;
  if(s.armed){
    if(btn.b)intake=-.35;
    else if(btn.a)intake=ready?.6:0;
    else if(btn.lb)intake=.6;
  }
  return {intake,shooter:shooter?.7:0,ready,armed:s.armed};
}
function neutral(){return {lb:false,rb:false,a:false,b:false,x:false,y:false};}
for(const v of [-720,-360,-181,-180,-1,0,1,180,181,360,540]){
  const expected=((v+180)%360+360)%360-180;
  ok(Math.abs(wrap(v))===Math.abs(expected) ||
   (wrap(v)===180&&expected===-180),"angle normalization "+v);
}
ok(Number.isNaN(wrap(NaN)),"reject invalid heading");
ok(Number.isNaN(wrap(Infinity)),"reject infinite heading");

let simulated=0;
for(const sign of [-1,1]){
  for(let initial=1;initial<=300;initial++){
    let yaw=-(initial%7+1);
    let state={back:true,signSelected:sign,confirmed:true,x:0,y:.5,
      yaw:0,target:0,hold:false,banned:false};
    state={...state,...headingControl(state)}; // anchor sem perder gamepad e configuracao
    const startMagnitude=Math.abs(yaw);
    let final=state;
    for(let t=0;t<220;t++){
      state.yaw=yaw;final=headingControl(state);
      yaw+=sign*final.xOut*115*.02;
      state={...state,target:final.target,hold:final.hold,banned:final.banned};
      simulated++;
      ok(Number.isFinite(yaw)&&Math.abs(final.xOut)<=.15+1e-12,"bounded IMU correction");
    }
    ok(Math.abs(yaw)<startMagnitude*.08,"hold converges for sign "+sign);
  }
}
let a=headingControl({back:true,signSelected:0,confirmed:false,x:0,y:.5,yaw:12,target:0,hold:false,banned:false});
ok(!a.active && a.xOut===0,"IMU disabled without signed selection");
a=headingControl({back:true,signSelected:1,confirmed:true,x:.4,y:.5,yaw:12,target:0,hold:true,banned:false});
ok(!a.active&&a.xOut===.4,"pilot rotation overrides assist");
a=headingControl({back:true,signSelected:1,confirmed:true,x:0,y:0,yaw:12,target:0,hold:false,banned:false});
ok(!a.active,"no assistance at rest");
a=headingControl({back:true,signSelected:1,confirmed:true,x:0,y:.5,yaw:NaN,target:0,hold:true,banned:false});
ok(!a.active&&a.banned,"invalid yaw latches disabled until BACK released");
a=headingControl({back:false,signSelected:1,confirmed:true,x:0,y:.5,yaw:0,target:0,hold:false,banned:true});
ok(!a.banned,"BACK release resets protection");
a=headingControl({back:true,signSelected:1,confirmed:true,x:0,y:.5,yaw:30,target:0,hold:true,banned:false});
ok(a.banned&&!a.active&&a.xOut===0,"large yaw error stops assistance");

let s={armed:false,yprev:false,warmTime:0};
let out=mechanisms(s,{...neutral(),a:true,rb:true},.02);
ok(out.intake===0&&out.shooter===0,"disarmed machine cannot feed or launch");
mechanisms(s,neutral(),.02);
out=mechanisms(s,{...neutral(),y:true},.02);
ok(out.armed,"operator arms with Y edge and buttons released");
out=mechanisms(s,{...neutral(),y:true},.02);
ok(out.armed,"Y held remains armed");
out=mechanisms(s,{...neutral(),rb:true,a:true},.02);
ok(out.shooter===.7&&out.intake===0&&!out.ready,"feed interlocked while MS accelerates");
for(let i=0;i<40;i++)out=mechanisms(s,{...neutral(),rb:true,a:true},.02);
ok(out.ready&&out.intake===.6,"feed enabled only after 800ms");
out=mechanisms(s,{...neutral(),a:true},.02);
ok(out.intake===0&&out.shooter===0&&!out.ready,"releasing shooter disables feeder immediately");
out=mechanisms(s,{...neutral(),b:true,lb:true},.02);
ok(out.intake===-.35,"reverse overrides intake");
out=mechanisms(s,{...neutral(),x:true,rb:true,a:true},.02);
ok(!out.armed&&out.intake===0&&out.shooter===0,"X forces output zero");
out=mechanisms(s,{...neutral(),y:true,rb:true},.02);
ok(!out.armed,"cannot arm with shooter held");
mechanisms(s,neutral(),.02);
out=mechanisms(s,{...neutral(),y:true},.02);
ok(out.armed,"Y edge may deliberately re-arm after X");

let seed=20261012;
function rnd(){seed=(Math.imul(seed,1664525)+1013904223)>>>0;return seed/4294967296;}
let randomFrames=25000,blocked=0,feedReady=0;
for(let i=0;i<randomFrames;i++){
 const btn={lb:rnd()>.6,rb:rnd()>.6,a:rnd()>.6,b:rnd()>.75,x:rnd()>.94,y:rnd()>.94};
 const res=mechanisms(s,btn,.005);
 ok(Math.abs(res.intake)<=.6&&res.shooter<=.7&&res.shooter>=0,"outputs bounded "+i);
 ok(res.intake!==.6||res.armed,"cannot run MI unarmed "+i);
 ok(!btn.x||(!res.armed&&res.intake===0&&res.shooter===0),"X safety "+i);
 ok(res.shooter!==.7||res.armed&&btn.rb,"shooter hold-to-run "+i);
 ok(!(btn.a&&btn.rb&&!res.ready)||res.intake!==.6,"cannot feed before warmup "+i);
 if(!res.armed)blocked++;
 if(res.ready&&btn.a)feedReady++;
}
console.log(JSON.stringify({passed:true,assertions,simulatedHeadingFrames:simulated,
  mechanismRandomFrames:randomFrames,blockedMechanismFrames:blocked,
  feedFramesAfterWarmup:feedReady,
  scope:"Modelos matematicos em JS; NAO sao leituras reais de hardware ou compilacao de Java."},null,2));
