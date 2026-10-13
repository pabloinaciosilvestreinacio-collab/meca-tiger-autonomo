// Testes de regressao sinteticos do TeleOp 1.0/1.1 da Meca Tiger.
// Executar: node tests/teleop_1x_logic.mjs
// Nao simula o FTC SDK, mecanica ou sinais fisicos dos motores.
function runTeleOpTests() {
  const clip=(v,l,h)=>Math.max(l,Math.min(h,v));
  const dead=v=>!Number.isFinite(v)||Math.abs(v)<=0.08?0:clip(v,-1,1);
  const quadratic=v=>Math.sign(v)*v*v;
  function mix(y,x,quadraticOn,speed) {
    y=dead(y); x=dead(x);
    if(quadraticOn){y=quadratic(y);x=quadratic(x);}
    let md=-x-y,me=-x+y,divider=Math.max(1,Math.abs(md),Math.abs(me));
    return [clip(md/divider*speed,-1,1),clip(me/divider*speed,-1,1)];
  }
  function ramp(current,target,dt){
    const reversing=current!==0&&target!==0&&Math.sign(current)!==Math.sign(target);
    const destination=reversing?0:target;
    const accelerating=current===0&&destination!==0||
      Math.sign(current)===Math.sign(destination)&&Math.abs(destination)>Math.abs(current);
    const rate=accelerating?2:4;
    const step=rate*clip(dt,0,.10);
    return clip(destination,current-step,current+step);
  }
  let assertions=0;
  function test(c,msg){assertions++;if(!c)throw Error(`Test ${assertions} failed: ${msg}`);}
  const eq=(a,b,tol=1e-10)=>Math.abs(a-b)<tol;
  test(dead(0.08)===0,"deadzone upper boundary");
  test(dead(-0.08)===0,"deadzone lower boundary");
  test(dead(0.09)===.09,"input outside deadzone");
  test(dead(Infinity)===0,"invalid infinity");
  test(dead(NaN)===0,"invalid NaN");
  test(quadratic(-.5)===-.25,"signed-square negative");
  test(quadratic(.5)===.25,"signed-square positive");
  test(mix(-1,0,false,.35)[0]===.35,"historical forward MD formula");
  test(mix(-1,0,false,.35)[1]===-.35,"historical forward ME formula");
  test(mix(0,1,false,.35)[0]===-.35&&mix(0,1,false,.35)[1]===-.35,
    "historical right-stick turn commands same sign (physical yaw still unknown)");
  test(mix(-1,1,false,1)[0]===0 && mix(-1,1,false,1)[1]===-1,
    "combined input proportional normalization");
  test(mix(-.5,0,true,1)[0]===.25&&mix(-.5,0,true,1)[1]===-.25,"quadratic in drive");
  test(ramp(0,1,.1)===.2,"acceleration from rest");
  test(eq(ramp(.6,0,.1),.2),"braking faster than acceleration");
  test(eq(ramp(.2,-1,.1),0),"reversal stop-at-zero");
  test(ramp(0,-1,.1)===-.2,"start opposite after zero");
  test(ramp(0,1,10)===.2,"dt clamped at 100ms");
  test(ramp(0,1,0)===0,"dt zero no power jump");
  let held=false,toggle=false;
  for(const state of [false,true,true,true,false,true]){
     if(state&&!held)toggle=!toggle;
     held=state;
  }
  test(toggle===false,"start toggles only on rising edge");
  function speed(lb,rb){return lb?.35:rb?1:.7;}
  test(speed(false,false)===.7,"normal speed");
  test(speed(true,false)===.35,"precision speed");
  test(speed(false,true)===1,"turbo speed");
  test(speed(true,true)===.35,"precision has safe priority");
  let seed=20261012;
  function rand(){seed=(Math.imul(seed,1664525)+1013904223)>>>0;return seed/4294967296;}
  let simulated=25000, bounds=0, reversals=0, acceleration=0;
  let actualMD=0,actualME=0;
  for(let i=0;i<simulated;i++){
    const y=2*rand()-1,x=2*rand()-1;
    const scale=[.35,.7,1][Math.floor(rand()*3)];
    const [right,left]=mix(y,x,rand()>.5,scale);
    test(Math.abs(right)<=scale+1e-10 && Math.abs(left)<=scale+1e-10,
        "normalization never exceeds selected limit "+i);
    const dt=rand()*.2;
    const md2=ramp(actualMD,right,dt),me2=ramp(actualME,left,dt);
    for(const [initial,result,target] of [[actualMD,md2,right],[actualME,me2,left]]){
      test(Number.isFinite(result)&&Math.abs(result)<=1+1e-12,"finite bounded power");
      if(Math.sign(initial)!==0&&Math.sign(target)!==0&&Math.sign(initial)!==Math.sign(target)){
        test(Math.sign(result)===Math.sign(initial)||result===0,
          "cannot instantly reverse sign");
        reversals++;
      }
      if(Math.abs(result)>Math.abs(initial)+1e-12&&Math.sign(initial)===Math.sign(result)){
        test(Math.abs(result-initial)<=2*Math.min(dt,.1)+1e-10,"acceleration slew");
        acceleration++;
      }
      bounds++;
    }
    actualMD=md2;actualME=me2;
  }
  return {assertions,simulatedControlFrames:simulated,verifiedPowerOutputs:bounds,
          checkedReversals:reversals,checkedAccelerations:acceleration,
          passed:true,scope:"Synthetic algebraic tests of joystick mix, normalization, priority, toggle, ramp; physical rotation and actual robot not tested."};
}
console.log(JSON.stringify(runTeleOpTests(),null,2));
