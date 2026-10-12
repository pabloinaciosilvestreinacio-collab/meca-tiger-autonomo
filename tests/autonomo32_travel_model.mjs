// Node.js: node tests/autonomo32_travel_model.mjs
// Modelo simplificado, sem FTC SDK, não é teste físico.
function runAutonomo32TravelTests(){
 const PI=Math.PI, ticksMM=28*20/(PI*90),goalTicks=Math.round(2000*ticksMM);
 function rng(seed){let s=seed>>>0;return ()=>{s=(Math.imul(s,1664525)+1013904223)>>>0;return s/4294967296;};}
 function wrap(x){let v=x%360;if(v>180)v-=360;else if(v< -180)v+=360;return v;}
 function delta(e,p){return (e-p+540)%360-180;}
 function limit(x,a,b){return Math.max(a,Math.min(b,x));}
 function test(seed,scenario="normal"){
  const random=rng(seed);
  let realYaw=0, odR=0, odL=0, prevErr=0,first=true,integral=0,filtered=0;
  const maxSpeed=800+random()*850,gainR=.94+random()*.12,gainL=.94+random()*.12;
  const slipR=random()*.03,slipL=random()*.03;
  let tickR=0,tickL=0,lastProgressR=0,lastProgressL=0,lastMoveR=0,lastMoveL=0;
  let lastCommandR=0,lastCommandL=0,maxCenterTicks=0;
  for(let j=0;j<1200;j++){
    const t=j*.01;
    let pR=Math.floor(tickR),pL=Math.floor(tickL);
    if(pR< -10||pL< -10)return {status:"encoder-negative",t};
    const center=(Math.max(0,pR)+Math.max(0,pL))/2;
    maxCenterTicks=Math.max(center,maxCenterTicks);
    const heading=wrap(realYaw+(random()-.5)*.16),err=wrap(-heading);
    if(center>=goalTicks-25){return {status:"distance-reached",t,overshoot:Math.max(0,center-goalTicks),heading:realYaw,left:pL,right:pR,requiresFinalTurn:Math.abs(err)>5};}
    if(pR>=lastProgressR+6){lastProgressR=pR;lastMoveR=t;}else if(t-lastMoveR>1.2)return {status:"MD-stall",t};
    if(pL>=lastProgressL+6){lastProgressL=pL;lastMoveL=t;}else if(t-lastMoveL>1.2)return {status:"ME-stall",t};
    if(first){prevErr=err;first=false;}
    filtered+=(.01/(.08+.01))*((delta(err,prevErr)/.01)-filtered);
    let oldIntegral=integral;
    integral=limit(integral+err*.01,-250,250);
    const raw=.018*err+.00015*integral+.0012*filtered, yawCorr=limit(raw,-.25,.25);
    if(yawCorr!==raw&&Math.sign(raw)===Math.sign(err))integral=oldIntegral;
    prevErr=err;
    const encoderCorrection=limit(.0002*(pR-pL),-.05,.05);
    const remaining=Math.max(0,goalTicks-center);
    const factorAcceleration=limit(t/.4,0,1),factorDecel=limit(remaining/(goalTicks*.2),0,1);
    const base=Math.max(.16,.35*Math.min(factorAcceleration,factorDecel));
    const cap=Math.min(.25,Math.max(0,base-.08));
    const corr=limit(yawCorr-encoderCorrection,-cap,cap);
    const right=limit(base+corr,0,1),left=limit(base-corr,0,1);
    const speedR=(scenario==="right-stall"?0:right*maxSpeed*gainR),speedL=left*maxSpeed*gainL;
    tickR+=speedR*.01*ticksMM;tickL+=speedL*.01*ticksMM;
    const velR=speedR*(1-slipR),velL=speedL*(1-slipL);
    realYaw=wrap(realYaw+(velR-velL)/450*(180/PI)*.01);
  }
  return {status:"timeout",t:12};
 }
 const normal={reached:0,timeout:0,stall:0,headingFix:0,maxOvershoot:0,maxTime:0};
 for(let k=0;k<3000;k++){
  const r=test(500+k);
  if(r.status==="distance-reached"){normal.reached++;normal.maxOvershoot=Math.max(normal.maxOvershoot,r.overshoot);normal.maxTime=Math.max(normal.maxTime,r.t);if(r.requiresFinalTurn)normal.headingFix++;}
  else if(r.status.includes("stall"))normal.stall++;
  else normal.timeout++;
 }
 const injected={stall:0,timeout:0,unexpected:0};
 for(let k=0;k<300;k++){
  const r=test(6000+k,"right-stall");
  if(r.status==="MD-stall")injected.stall++;else if(r.status==="timeout")injected.timeout++;else injected.unexpected++;
 }
 const assertions={
  normalCount:normal.reached+normal.timeout+normal.stall===3000,
  injectedCount:injected.stall+injected.timeout+injected.unexpected===300,
  rightStallDetected:injected.stall===300,
  allNormalReached:normal.reached===3000,
  noEncoderOvershootBeyond30Ticks:normal.maxOvershoot<=30
 };
 const result={assertions,normal,injected,model:"Cinematica diferencial por passos de 10 ms. Encoder por giro simulado das rodas, IMU com ruído, velocidades sintéticas de 800-1650mm/s por potencia plena, distancia entre rodas 450mm. NAO prova hardware."};
 if(Object.values(assertions).some(x=>!x))throw Error(JSON.stringify(result));
 return result;
}
console.log(JSON.stringify(runAutonomo32TravelTests(),null,2));
