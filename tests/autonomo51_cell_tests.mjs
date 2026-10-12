// Synthetic selection logic; does not use FTC SDK or physical camera.
function runDynamicCellTests(){
  const targets=["RED SCORING","RED AUDIENCE","BLUE SCORING","BLUE AUDIENCE"];
  function selectDetections(dets,alliance,prefer,offset=0){
    let best=null;
    for(const d of dets){
      if(!d || !targets.includes(d.name) || !d.name.startsWith(alliance+" "))continue;
      if(!Number.isFinite(d.roll)||Math.abs(d.roll)>=90)continue;
      if(!Number.isFinite(d.bearing)||Math.abs(d.bearing)>180||!Number.isFinite(d.range)||d.range<=0)continue;
      if(!best || Math.abs(d.bearing-offset)<Math.abs(best.bearing-offset))best=d;
    }
    return best;
  }
  function assert(v,msg){if(!v)throw Error(msg);}
  let asserts=0;function chk(v,msg){assert(v,msg);asserts++;}
  for(const a of ["RED","BLUE"]){
    const scored=a+" SCORING",aud=a+" AUDIENCE",wrong=(a==="RED"?"BLUE":"RED")+" SCORING";
    let dets=[{name:scored,roll:140,bearing:1,range:80},{name:aud,roll:20,bearing:20,range:80},{name:wrong,roll:0,bearing:0,range:10}];
    chk(selectDetections(dets,a,scored).name===aud,a+" flips scoring=>audience");
    dets=[{name:scored,roll:0,bearing:-5,range:80},{name:aud,roll:15,bearing:15,range:80},{name:wrong,roll:0,bearing:0,range:10}];
    chk(selectDetections(dets,a,aud).name===scored,a+" chooses closer bearing even if preferred other");
    dets=[{name:scored,roll:90,bearing:0,range:80},{name:aud,roll:-100,bearing:0,range:80}];
    chk(selectDetections(dets,a,scored)===null,a+" inverted all means no target");
    dets=[{name:scored,roll:0,bearing:10,range:80},{name:aud,roll:0,bearing:3,range:80}];
    chk(selectDetections(dets,a,scored,8).name===scored,a+" offset changes nearest target");
  }
  function rng(seed){let x=seed>>>0;return ()=>{x=(Math.imul(x,1664525)+1013904223)>>>0;return x/4294967296;};}
  const r=rng(345678);let n=10000,valid=0;
  for(let i=0;i<n;i++){
    const alliance=r()>.5?"RED":"BLUE",offset=r()*20-10;
    const detections=targets.map(name=>({name,roll:r()*360-180,bearing:r()*360-180,range:12+r()*150}));
    const chosen=selectDetections(detections,alliance,"",offset);
    if(chosen) {
      chk(chosen.name.startsWith(alliance+" "),"chosen wrong alliance");
      chk(Math.abs(chosen.roll)<90,"chosen inverted target");
      const all=detections.filter(d=>d.name.startsWith(alliance+" ")&&Math.abs(d.roll)<90);
      chk(all.every(d=>Math.abs(chosen.bearing-offset)<=Math.abs(d.bearing-offset)+1e-12),"chosen not best bearing");
      valid++;
    }
  }
  chk(valid>1000,"no valid detections randomized");
  return {passed:true,asserts,randomSets:n,validSelections:valid,limitations:"Tests use model equivalent to selection filter, not camera pixels or Java SDK."};
}
console.log(JSON.stringify(runDynamicCellTests(), null, 2));
