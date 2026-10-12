import assert from 'node:assert/strict';

const clamp = (x, a, b) => Math.max(a, Math.min(b, x));
const norm = x => Number.isFinite(x) ? ((x % 360) > 180 ? (x % 360) - 360 : (x % 360) < -180 ? (x % 360) + 360 : x % 360) : NaN;
const angularDelta = (now, old) => ((now - old + 540) % 360) - 180;
const ticksMm = 28 * (76 / 21) * (68 / 13) / (Math.PI * 90);
const alvoTicks = Math.round(ticksMm * 2000);
const tests = [];
function test(name, fn) { fn(); tests.push(name); }

test('angles ±180 and invalids', () => {
  for (const [a, b] of [[0,0],[181,-179],[-181,179],[540,180],[-540,-180],[359,-1]]) assert.equal(norm(a),b);
  assert.ok(Number.isNaN(norm(Infinity)));
  assert.ok(Number.isNaN(norm(NaN)));
});
test('PID derivative stays continuous at angular wrap', () => {
  assert.equal(angularDelta(-179,179),2);
  assert.equal(angularDelta(179,-179),-2);
  assert.equal(angularDelta(10,9),1);
  assert.equal(angularDelta(-10,10),-20);
});
test('gear tooth ratios and wheel target', () => {
  assert.ok(ticksMm>1.87 && ticksMm<1.88);
  assert.ok(alvoTicks>=3740 && alvoTicks<=3770);
});
test('distance completion uses mean wheel ticks, not equality', () => {
  let right=alvoTicks+60,left=alvoTicks-50;
  assert.ok((right+left)/2 >= alvoTicks-25);
  assert.ok(Math.abs(right-left)>65);
  const yawError=12;
  const stage=(right+left)/2>=alvoTicks-25 ? Math.abs(yawError)<=5?'done':'correctYawWithoutTranslation':'drive';
  assert.equal(stage,'correctYawWithoutTranslation');
});
test('visual stale frames stop and fallback bounded', () => {
  const latestFrame=0.50;
  const actuationPower=0.13;
  const outputAt=t=>t-latestFrame>0.08?0:actuationPower;
  assert.equal(outputAt(0.59),0);
  assert.ok(0.86-latestFrame>0.35);
});
test('target sign is positive yaw for positive bearing in assumed differential drive', () => {
  for (const b of [-15,-3,3,15]) {
    let power=clamp(0.01*b,-0.25,0.25);
    if(Math.abs(power)<0.1)power=Math.sign(b)*0.1;
    let powerR=power,powerL=-power;
    assert.equal(Math.sign(powerR-powerL),Math.sign(b));
  }
});
test('global AUTO shooting budget', () => {
  const shoot=0.80+3*0.45+2*0.15;
  assert.ok(Math.abs(shoot-2.45)<1e-10);
  const remainingAt=t=>Math.max(0,30-1-t);
  assert.ok(remainingAt(26.0)>shoot);
  assert.ok(!(remainingAt(27.0)>shoot));
  const worstCase = 7.0 + 12.0 + 1.50 + 7.0 + 2.50 + 1.60 + shoot;
  assert.ok(worstCase > 29, 'Timeout por etapa nao garante concluir em 29s');
});

function seedRand(seed) {
  let s=seed>>>0;
  return ()=>{s=(Math.imul(s,1664525)+1013904223)>>>0;return s/4294967296;};
}
// Approximate differential-drive yaw dynamics, fresh camera frames and 10 ms command loop.
function simulateVision(seed, opts={}){
  const rand=seedRand(seed);
  const speed=95+rand()*110; // yaw degrees/second for motor power 1.0
  const timeConstant=0.05+rand()*0.13;
  const interval=opts.interval ?? (0.03+rand()*0.03);
  const dropout=opts.dropout ?? (0.03+rand()*0.07);
  const target= (rand()*2-1)*15;
  let yaw=0, vel=0, power=0, prevFrame=-1, nextFrame=0, startConfirm=-1, stable=-1, seen=false;
  for(let i=0;i<250;i++){
    const t=i*0.01;
    let gotNewFrame=false;
    if(t+1e-8>=nextFrame){nextFrame+=interval;gotNewFrame=rand()>dropout;}
    if(gotNewFrame){
      if(prevFrame>=0 && t-prevFrame>0.18){startConfirm=-1;stable=-1;}
      prevFrame=t;
      const bearing=target-yaw+(rand()-.5)*0.5;
      if(startConfirm<0)startConfirm=t;
      if(t-startConfirm>=0.18){
        seen=true;
        if(Math.abs(bearing)<=2){
          power=0;
          if(stable<0)stable=t;
          if(t-stable>=0.20)return {success:true,elapsed:t,error:target-yaw};
        }else{
          stable=-1;
          power=clamp(bearing*0.01,-0.25,0.25);
          if(Math.abs(power)<0.1)power=Math.sign(bearing)*0.1;
        }
      }
    }else if(prevFrame<0 || t-prevFrame>0.08){power=0;}
    if(prevFrame>=0 && t-prevFrame>0.35&&seen)return {success:false,reason:'fallback-lost'};
    vel+=(power*speed-vel)*(0.01/timeConstant);
    yaw+=(opts.sign ?? 1)*vel*0.01;
  }
  return {success:false,reason:seen?'fallback-timeout':'fallback-no-target'};
}
const n=5000, data={success:0,timeout:0,noTarget:0,lost:0};let maxError=0;
for(let i=0;i<n;i++){
 const x=simulateVision(20261012+i);
 if(x.success){data.success++; maxError=Math.max(maxError,Math.abs(x.error));}
 else if(x.reason==='fallback-timeout')data.timeout++;
 else if(x.reason==='fallback-no-target')data.noTarget++;
 else data.lost++;
}
assert.equal(Object.values(data).reduce((a,b)=>a+b,0),n);
assert.ok(maxError<=2.5,'success implies target inside ±2deg tolerance with noise allowance');
const stressed={success:0,timeout:0,noTarget:0,lost:0};
for(let i=0;i<2000;i++){
  const x=simulateVision(100000+i, {dropout:0.45,interval:0.07});
  if(x.success)stressed.success++;
  else if(x.reason==='fallback-timeout')stressed.timeout++;
  else if(x.reason==='fallback-no-target')stressed.noTarget++;
  else stressed.lost++;
}
assert.equal(Object.values(stressed).reduce((a,b)=>a+b,0),2000);
const wrongSign={success:0,timeout:0,noTarget:0,lost:0};
for(let i=0;i<500;i++){
  const x=simulateVision(200000+i, {sign:-1});
  if(x.success)wrongSign.success++;
  else if(x.reason==='fallback-timeout')wrongSign.timeout++;
  else if(x.reason==='fallback-no-target')wrongSign.noTarget++;
  else wrongSign.lost++;
}
assert.equal(Object.values(wrongSign).reduce((a,b)=>a+b,0),500);
console.log(JSON.stringify({testsPassed:tests.length,tests,gearRatio:(76/21)*(68/13),ticksMm,ticks2000:alvoTicks,simulations:n,data,stress2000:stressed,wrongSign500:wrongSign,maxHeadingErrorSuccessful:maxError,simulationNote:'Modelo simplificado, não FTC SDK nem hardware'},null,2));
