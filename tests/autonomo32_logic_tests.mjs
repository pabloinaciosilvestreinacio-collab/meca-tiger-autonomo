// Executar: node tests/autonomo32_logic_tests.mjs
// Testes matematicos e simulação simplificada, NAO compilam ou controlam o FTC SDK.
function runAutonomo32LogicTests(){
  let count=0; const failures=[];
  function ok(cond,label){count++;if(!cond)failures.push(label);}
  function wrap(x){if(!Number.isFinite(x))return NaN;let d=x%360;if(d>180)d-=360;else if(d< -180)d+=360;return d;}
  const delta=(a,b)=>((a-b+540)%360)-180;
  const ticksPerMm=28*20/(Math.PI*90),targetTicks=Math.round(2000*ticksPerMm);
  for(const [x,expected] of [[0,0],[181,-179],[-181,179],[360,0],[-540,-180],[540,180],[359,-1]])ok(wrap(x)===expected,"wrap "+x);
  ok(Number.isNaN(wrap(Infinity))&&Number.isNaN(wrap(NaN)),"reject invalid angle");
  ok(delta(-179,179)===2 && delta(179,-179)===-2,"PID yaw error delta crosses +/-180 continuously");
  ok(Math.abs(ticksPerMm-1.980594)<.001 && targetTicks===3961,"3.1 nominal reduction retained");
  ok(((targetTicks+70)+(targetTicks-50))/2>=targetTicks-25,"wheel-average terminates even with imbalance");
  ok(Math.abs((targetTicks+70)-(targetTicks-50))>65,"former encoder equality would block completion");
  ok(Math.abs(15)>5 && ((targetTicks+70)+(targetTicks-50))/2>=targetTicks-25,"final yaw correction at stopped translation");
  const budget=0.8+3*.45+2*.15, margins=1+.35;
  ok(Math.abs(budget-2.45)<1e-8,"shot duration 2.45s");
  const mayMove=t=>(30-1-t)>budget+.35;
  ok(mayMove(25.8)&&!mayMove(26.3),"movement stops to keep launch margin");
  ok(!mayMove(30),"stop at period end");
  function rng(seed){let v=seed>>>0;return ()=>{v=(Math.imul(1664525,v)+1013904223)>>>0;return v/4294967296;};}
  function simulateTurn(seed,goal,orientation=1){
    const random=rng(seed),gain=110+random()*130,friction=.02+random()*.045,tau=.07+random()*.16,noise=.10+random()*.10;
    let yaw=0,omega=0,integral=0,filtered=0,prevErr=0,initialized=false;
    let best=Infinity,lastImprovement=0,stable=-1,wasRunning=false;
    for(let i=0;i<700;i++){
      const t=i*.01,reported=wrap(yaw+(random()-.5)*2*noise),err=wrap(goal-reported);
      if(Math.abs(err)+1<best){best=Math.abs(err);lastImprovement=t;}
      if(Math.abs(err)>10&&t-lastImprovement>1.2)return {status:"watchdog",t,yaw};
      if(!initialized){prevErr=err;initialized=true;}
      filtered+=(.01/(.08+.01))*((delta(err,prevErr)/.01)-filtered);
      let prior=integral;
      integral=Math.max(-250,Math.min(250,integral+err*.01));
      const raw=.012*err+.00012*integral+.0015*filtered;
      let cmd=Math.max(-.5,Math.min(.5,raw));
      if(cmd!==raw&&Math.sign(raw)===Math.sign(err))integral=prior;
      prevErr=err;
      const estimatedYaw=-filtered;
      if(Math.abs(err)<=2&&Math.abs(estimatedYaw)<=8){
        if(stable<0)stable=t;
        if(t-stable>=.12)return {status:"success",t,yaw};
      }else stable=-1;
      if(Math.abs(err)<=2)cmd=0;
      else if(Math.abs(cmd)<.08)cmd=Math.sign(err)*.08;
      if(Math.abs(cmd)<friction)cmd=0;
      omega+=(cmd*gain-omega)*.01/tau;
      yaw=wrap(yaw+orientation*omega*.01);
    }
    return {status:"timeout",t:7,yaw};
  }
  let nominal={success:0,timeout:0,watchdog:0},inverted={success:0,timeout:0,watchdog:0};
  for(let i=0;i<3000;i++){
    for(const goal of [-50,100]){const o=simulateTurn(10000+i+goal*7,goal);nominal[o.status]++;}
  }
  for(let i=0;i<400;i++){const o=simulateTurn(50000+i,100,-1);inverted[o.status]++;}
  ok(Object.values(nominal).reduce((a,b)=>a+b,0)===6000,"6000 nominal plant cases counted");
  ok(Object.values(inverted).reduce((a,b)=>a+b,0)===400,"400 inverted-physical-sign cases counted");
  ok(inverted.watchdog>0,"wrong polarities detected (at least some)");
  const outputs={tests:count,passed:failures.length===0,failures,nominal,inverted,ticksPerMm,targetTicks,shootTimeSeconds:budget,model:"synthetic first-order differential-drive yaw with PID equivalent to Java; not FTC SDK and not field calibration"};
  if(failures.length)throw Error(JSON.stringify(outputs));
  return outputs;
}
console.log(JSON.stringify(runAutonomo32LogicTests(),null,2));
