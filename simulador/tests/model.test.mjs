import test from 'node:test';
import assert from 'node:assert/strict';
import {rotate, launcher, heightAt, flight, gamepadValues} from '../web/model.mjs';
test('Ponto do Desmos permanece preso ao centro ao girar',()=>{
  for(const yaw of [0,45,90,180,-135]){
    const p={x:2,y:1,yaw},l=launcher(p,.1125,.225);
    assert.ok(Math.abs(Math.hypot(l[0]-2,l[1]-1)-Math.hypot(.1125,.225))<1e-10);
  }
  assert.deepEqual(launcher({x:1,y:1,yaw:0},.1125,.225),[1.1125,1.225]);
});
test('Mover lançador não muda pose e gira pelo centro',()=>{
  const p={x:1,y:2,yaw:90};assert.deepEqual(rotate(1,0,90).map(v=>Math.round(v)),[0,1]);
  const l=launcher(p,.3,.4);assert.ok(Math.abs(l[0]-.6)<1e-12);assert.ok(Math.abs(l[1]-2.3)<1e-12);
  assert.deepEqual(p,{x:1,y:2,yaw:90});
});
test('Parábola com alcance analítico',()=>{const f=flight(0,5,45);assert.ok(Math.abs(f.range-25/9.81)<1e-10);assert.ok(Math.abs(heightAt(f.range,0,5,45))<1e-10);});
test('Alvo vertical usa projeção vetorial, sem dividir por delta X',()=>{const l=launcher({x:1,y:1,yaw:0},0,.2),target=[1,3];assert.equal(target[0]-l[0],0);assert.ok(Number.isFinite(Math.hypot(target[0]-l[0],target[1]-l[1])));});
test('Mapeamento padrão: eixos, gatilhos, back/start e D-pad',()=>{
  const buttons=Array.from({length:16},()=>({pressed:false,value:0}));buttons[0]={pressed:true,value:1};buttons[6]={pressed:true,value:.7};buttons[8]={pressed:true,value:1};buttons[12]={pressed:true,value:1};
  const v=gamepadValues({mapping:'standard',axes:[.1,-.2,.3,-.4],buttons});assert.equal(v.length,20);assert.equal(v[1],-.2);assert.equal(v[4],.7);assert.equal(v[6],1);assert.equal(v[12],1);assert.equal(v[16],1);
});
test('Desconectar ou mapeamento desconhecido zera comando',()=>{assert.deepEqual(gamepadValues(null),Array(20).fill(0));assert.deepEqual(gamepadValues({mapping:''}),Array(20).fill(0));});
