// Synthetic model of current Java PID constants; no FTC SDK/hardware.
function runActualPidModel(){
 function norm(x){let y=x%360;if(y>180)y-=360;else if(y< -180)y+=360;return y;}
 function rng(seed){let s=seed>>>0;return ()=>{s=(Math.imul(s,1664525)+1013904223)>>>0;return s/4294967296;};}
 function sim(seed,target){
  const random=rng(seed);
  const kp=.012,ki=.00012,kd=.0015,dt=.01,tau=.07+.23*random(),maxOmega=145+165*random(),friction=.03+.06*random();
  const sensorNoise=.06+.20*random();
  let heading=0,omega=0,integral=0,lastError=0,filteredD=0,first=true,best=Infinity,lastImprove=0,stableStart=-1;
  let maxAbs=0;
  for(let i=0;i<450;i++){
   const time=i*.01,measurement=norm(heading+(random()-.5)*2*sensorNoise),error=norm(target-measurement);
   if(Math.abs(error)+1<best){best=Math.abs(error);lastImprove=time;}
   if(Math.abs(error)>10&&time-lastImprove>1.2)return {outcome:"watchdog",time,final:heading};
   if(first){lastError=error;first=false;}
   const derivative=(((error-lastError+540)%360)-180)/dt;
   const alpha=dt/(.08+dt);
   filteredD+=alpha*(derivative-filteredD);
   let intPrevious=integral;
   integral=Math.max(-250,Math.min(250,integral+error*dt));
   const raw=kp*error+ki*integral+kd*filteredD;
   let cmd=Math.max(-.5,Math.min(.5,raw));
   if(cmd!==raw&&Math.sign(raw)===Math.sign(error))integral=intPrevious;
   lastError=error;
   const estYaw=-filteredD;
   if(Math.abs(error)<=2 && Math.abs(estYaw)<=8){
     if(stableStart<0) stableStart=time;
     if(time-stableStart>=.12)return {outcome:"success",time,final:heading};
   }else stableStart=-1;
   if(Math.abs(error)>2&&Math.abs(cmd)<.1)cmd=Math.sign(error)*.1;
   const phys=Math.abs(cmd)<friction?0:cmd;
   omega+=(maxOmega*phys-omega)*dt/tau;
   heading=norm(heading+omega*dt);
   maxAbs=Math.max(maxAbs,Math.abs(error));
  }
  return {outcome:"timeout",time:4.5,final:heading,maxAbs};
 }
 let results={minus50:{success:0,timeout:0,watchdog:0},plus100:{success:0,timeout:0,watchdog:0}};let worstTime=0,worstError=0;
 for(let j=0;j<10000;j++){
  for(const [target,key] of [[-50,"minus50"],[100,"plus100"]]){
   const o=sim(j+20261012,target);
   results[key][o.outcome]++; if(o.outcome==="success"){worstTime=Math.max(worstTime,o.time);worstError=Math.max(worstError,Math.abs(norm(target-o.final)));}
  }
 }
 const totals=Object.values(results).map(x=>x.success+x.timeout+x.watchdog);
 if(totals.some(x=>x!==10000))throw Error("count mismatch");
 return {model:"Discrete Java-equivalent PID with derivative filtering, anti-windup, 10ms step; nonlinear inertia/friction, yaw-noise. Not FTC SDK.",results,worstSuccessfulTime:worstTime,worstSuccessfulFinalError:worstError};
}
console.log(JSON.stringify(runActualPidModel(),null,2));
