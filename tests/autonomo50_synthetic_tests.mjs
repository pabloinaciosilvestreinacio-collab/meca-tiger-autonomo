// Deterministic synthetic tests for Autonomo 5.0. Node.js >=18.
// This model does not use or test FTC SDK, hardware or true field geometry.
function runV50Tests() {
  const failures=[];let assertions=0;
  function check(cond, msg){assertions++;if(!cond)failures.push(msg);}
  function nrm(x){if(!Number.isFinite(x))return NaN;let y=x%360;if(y>180)y-=360;else if(y< -180)y+=360;return y;}
  function delta(a,b){return ((a-b+540)%360)-180;}
  function cluster(a,s){return a==="RED"?(s==="SCORING"?"RED SCORING":"RED AUDIENCE"):(s==="SCORING"?"BLUE SCORING":"BLUE AUDIENCE");}
  function watchdog(errors,times){let best=Infinity, lastImprove=0;for(let i=0;i<errors.length;i++){let e=errors[i],t=times[i];if(Math.abs(e)+1<best){best=Math.abs(e);lastImprove=t;}if(Math.abs(e)>10&&t-lastImprove>1.2)return t;}return null;}
  function rng(seed){let v=seed>>>0;return ()=>{v=(Math.imul(v,1664525)+1013904223)>>>0;return v/4294967296;};}
  const bounds=[4.5,9.5,1.5,4.5,2.5,1.6,2.45,0.35];
  const total=bounds.reduce((a,b)=>a+b,0);
  check(total<29,"worst-case temporal budget must fit into 29s");
  check(Math.abs(total-26.9)<1e-10,"expected budget 26.9");
  for(const a of ["RED","BLUE"])for(const s of ["SCORING","AUDIENCE"]){check(cluster(a,s)===a+" "+s,"alliance/side target "+a+" "+s);}
  for(const [x,v] of [[181,-179],[-181,179],[360,0],[540,180],[-540,-180]])check(nrm(x)===v,"normalization "+x);
  check(Number.isNaN(nrm(Infinity)),"nan guard");
  check(delta(-179,179)===2,"yaw derivative positive wrap");
  check(delta(179,-179)===-2,"yaw derivative negative wrap");
  check(watchdog([50,52,53,55,57],[0,0.3,0.6,1.0,1.3])!==null,"wrong direction watchdog");
  check(watchdog([50,45,40,35,25,18,12,5],[0,.3,.6,.9,1.2,1.5,1.8,2.1])===null,"correct direction no watchdog");
  check(watchdog([50,50,50,50,50],[0,0.3,0.6,1.0,1.3])!==null,"blocked motor watchdog");
  const ticksPerMm=28*(76/21)*(68/13)/(Math.PI*90);
  check(ticksPerMm>1.87&&ticksPerMm<1.88,"ticks per mm exact reduction");
  const shots=0.8+3*.45+2*.15;
  check(Math.abs(shots-2.45)<1e-9,"shot time total");
  function canShoot(now){return (29-now)>shots+.35;}
  check(canShoot(25.0),"time reservation adequate");
  check(!canShoot(26.3),"time reservation prevents late shot");
  const r=rng(20261012);
  let validTimelines=0;const n=10000;
  for(let i=0;i<n;i++){const times=bounds.map(x=>x*r()); const t=times.reduce((a,b)=>a+b,0);if(t<=total+1e-10)validTimelines++;}
  check(validTimelines===n,"stochastic timeline bound");
  let successes=0, stopped=0, failuresOfSim=0;
  for(let i=0;i<n;i++){
    const tar=(r()-.5)*190;
    const gain=140+160*r(),tau=.06+.18*r(),friction=.015+.07*r();
    let yaw=0, omega=0, stable=-1, stoppedReason=null;
    let best=Infinity, lastImprove=0;
    for(let step=0;step<450;step++){
      const t=step*.01,err=nrm(tar-yaw);
      if(Math.abs(err)+1<best){best=Math.abs(err);lastImprove=t;}
      if(Math.abs(err)>10&&t-lastImprove>1.2){stoppedReason="watchdog";break;}
      if(Math.abs(err)<=2&&Math.abs(omega)<=8){if(stable<0)stable=t;if(t-stable>=.12){stoppedReason="done";break;}}else stable=-1;
      let power=Math.max(-.5,Math.min(.5,err*.012));
      if(Math.abs(err)>2&&Math.abs(power)<.1)power=Math.sign(err)*.1;
      const physical=Math.abs(power)<friction?0:power;
      omega += ((physical*gain)-omega)*(.01/tau);
      yaw=nrm(yaw+omega*.01);
    }
    if(stoppedReason==="done")successes++;else if(stoppedReason==="watchdog")stopped++;else failuresOfSim++;
  }
  const result={assertions,passed:failures.length===0,failures,temporalWorstCase:total,ticksPerMm,scenarios:n,validTimelines,turnSimulation:{successes,watchdog:stopped,timeouts:failuresOfSim},model:"yaw first-order, variable gain/inertia/friction. No FTC SDK/physical hardware."};
  if(!result.passed)throw new Error(JSON.stringify(result)); return result;
}
console.log(JSON.stringify(runV50Tests(), null, 2));
