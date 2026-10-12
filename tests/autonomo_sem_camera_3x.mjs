// Reproduzivel: node tests/autonomo_sem_camera_3x.mjs
// Simulacao SINTETICA (nao e compilacao FTC nem ensaio fisico).
function runSemCameraRegression(){
 const clamp=(v,a,b)=>Math.max(a,Math.min(b,v));
 const wrap=a=>{if(!Number.isFinite(a))return NaN;let v=a%360;if(v>180)v-=360;else if(v< -180)v+=360;return v};
 const random=seed=>{let s=seed>>>0;return ()=>{s=(Math.imul(s,1664525)+1013904223)>>>0;return s/4294967296;}};
 const shots=0.8+3*.45+2*.15;
 const budget=5*2+10+1.5+shots+.35;
 const ticksMm=28*20/(Math.PI*90);
 let assertions=0;
 const expect=(bool,msg)=>{assertions++;if(!bool)throw new Error(msg)};
 expect(budget<29,"sum of stage limits + shooter > 29s");
 expect(Math.abs(budget-24.3)<1e-8,"expected stage budget");
 expect(Math.abs(ticksMm-1.98059)<.0001,"ticks/mm");
 for(const [v,target] of [[181,-179],[-181,179],[540,180],[-540,-180],[359,-1]])expect(wrap(v)===target,"wrap "+v);
 expect(!Number.isFinite(wrap(Infinity)),"infinite input");
 expect(((-179-179+540)%360)-180===2,"derivative wrap");
 function controller(kp,ki,kd,hi){
  let sum=0,previous=0,filtered=0,first=true;
  return err=>{
   if(first){previous=err;first=false;}
   const rawD=((err-previous+540)%360-180)/.01;
   filtered+=(rawD-filtered)*.01/(.08+.01);
   const prevSum=sum;sum=clamp(sum+err*.01,-250,250);
   const raw=kp*err+ki*sum+kd*filtered;
   const output=clamp(raw,-hi,hi);
   if(output!==raw&&Math.sign(raw)===Math.sign(err))sum=prevSum;
   previous=err;
   return {power:output,angularVelocity:-filtered};
  }
 }
 function model(seed,variant){
  const rnd=random(seed),speed=900+650*rnd(),inertia=.07+.13*rnd(),bias=(rnd()-.5)*.12,noise=.05+.12*rnd();
  let right=0,left=0,velR=0,velL=0,yaw=0,time=0;
  const sample=()=>variant==="bad_imu"&&time>1?NaN:wrap(yaw*(variant==="reverse_imu"?-1:1)+(rnd()-.5)*noise);
  const plant=(pr,pl)=>{
   const desiredR=(variant==="stalled_right"?0:pr*speed*(1+bias));
   const desiredL=pl*speed*(1-bias);
   velR+=(desiredR-velR)*.01/inertia;velL+=(desiredL-velL)*.01/inertia;
   right+=velR*.01;left+=velL*.01;
   yaw=wrap(yaw+(velR-velL)/450*.01*180/Math.PI);time+=.01;
  };
  const turn=(offset,timeout)=>{
   const start=sample();if(!Number.isFinite(start))return "sensor_invalid";
   const target=wrap(start+offset),pid=controller(.012,.00012,.0015,.5);
   let best=Infinity,progress=0,stable=-1;
   for(let i=0;i<timeout*100;i++){
    const y=sample();if(!Number.isFinite(y))return "sensor_invalid";
    const e=wrap(target-y),ans=pid(e);
    if(Math.abs(e)+1<best){best=Math.abs(e);progress=time;}
    if(Math.abs(e)>10&&time-progress>1.2)return "watchdog";
    if(Math.abs(e)<=2&&Math.abs(ans.angularVelocity)<=8){
      if(stable<0)stable=time;
      if(time-stable>=.12)return "ok";
    }else stable=-1;
    let cmd=ans.power;
    if(Math.abs(e)<=2)cmd=0;
    else if(Math.abs(cmd)<.08)cmd=Math.sign(e)*.08;
    plant(cmd,-cmd);
   }
   return "timeout";
  };
  let status=turn(-50,5);if(status!=="ok")return "firstTurn:"+status;
  const heading=sample();const target=2000*ticksMm;
  const pid=controller(.018,.00015,.0012,.25);
  const r0=right,l0=left,t0=time;
  let progR=0,progL=0,progressRt=time,progressLt=time;
  let drive="timeout";
  for(let i=0;i<1000;i++){
   if(!Number.isFinite(sample()))return "drive:sensor_invalid";
   let tr=Math.round((right-r0)*ticksMm),tl=Math.round((left-l0)*ticksMm);
   if(variant==="frozen_encoder")tr=0;
   if(tr< -10||tl< -10)return "drive:negative";
   const delta=tr-tl,average=(Math.max(0,tr)+Math.max(0,tl))/2;
   const e=wrap(heading-sample());
   if(Math.abs(delta)>220)return "drive:encoder_divergence";
   if(Math.abs(e)>35)return "drive:yaw_divergence";
   if(average>=target-25){drive=Math.abs(e)<=5?"ok":"yaw_adjust";break;}
   if(tr>=progR+6){progR=tr;progressRt=time;}else if(time-progressRt>1.2)return "drive:stall_right";
   if(tl>=progL+6){progL=tl;progressLt=time;}else if(time-progressLt>1.2)return "drive:stall_left";
   const base=Math.max(.16,.35*Math.min(clamp((time-t0)/.4,0,1),clamp((target-average)/(target*.2),0,1)));
   const limit=Math.min(.25,Math.max(0,base-.08));
   const correction=clamp(pid(e).power-clamp(.0002*delta,-.05,.05),-limit,limit);
   plant(clamp(base+correction,0,1),clamp(base-correction,0,1));
  }
  if(drive==="timeout")return "drive:timeout";
  if(drive==="yaw_adjust"){
   const error=wrap(heading-sample());status=turn(error,1.5);if(status!=="ok")return "endYaw:"+status;
  }
  status=turn(100,5);if(status!=="ok")return "secondTurn:"+status;
  if(time+shots+.35>=29)return "launch:budget";
  return "complete";
 }
 const modes={};let total=0;
 for(const variant of ["normal","reverse_imu","stalled_right","frozen_encoder","bad_imu"]){
   const distribution={};for(let i=0;i<1000;i++){const outcome=model(20261012+i,variant);distribution[outcome]=(distribution[outcome]??0)+1;total++;}
   modes[variant]=distribution;
 }
 expect(modes.normal.complete===1000,"nominal convergence in simplified model");
 expect((modes.reverse_imu["firstTurn:watchdog"]??0)>950,"wrong IMU sign detection");
 expect((modes.stalled_right["drive:encoder_divergence"]??0)>950,"stalled right detection");
 expect((modes.frozen_encoder["drive:encoder_divergence"]??0)>950,"frozen encoder detection");
 expect((modes.bad_imu["firstTurn:sensor_invalid"]??0)>950,"sensor invalid detection");
 let selection={mode:"GIRO_CURTO",confirmed:false};
 function choose(next){if(selection.mode!==next)selection.confirmed=false;selection.mode=next;}
 choose("COMPLETO");expect(!selection.confirmed,"mode change removes confirmation");
 selection.confirmed=true;choose("DESLOCAMENTO_CURTO");expect(!selection.confirmed,"new mode requires explicit A");
 const result={assertions,totalSimulated:total,physicalValidation:false,units:"millimeters/degrees/seconds",ticksMm,nominalMaxStageBudget:budget,distribution:modes,modelDescription:"simplified sampled differential drive + discrete PID; signs, camera absent; not FTC SDK"};
 return result;
}
console.log(JSON.stringify(runSemCameraRegression(),null,2));
