import assert from "node:assert/strict";
import fs from "node:fs";
import path from "node:path";
import { execFileSync } from "node:child_process";

let checks=0;
const assertIt=(ok,message)=>{assert.ok(ok,message);checks++;};
const root="TeamCode/src/main/java/org/firstinspires/ftc/teamcode/";
const codeVersions=[
 ["Autonomo/autonomo.java","historico/Autonomo_3_1.txt","autonomo"],
 ["Autonomo/Autonomo32.java","historico/Autonomo_3_2.txt","Autonomo32"],
 ["Autonomo/Autonomo33.java","historico/Autonomo_3_3.txt","Autonomo33"],
 ["Autonomo/Autonomo34.java","historico/Autonomo_3_4.txt","Autonomo34"],
 ["Autonomo/Autonomo35.java","historico/Autonomo_3_5.txt","Autonomo35"],
 ["Autonomo/Autonomo4.java","historico/Autonomo_4_0.txt","Autonomo4"],
 ["Autonomo/Autonomo41.java","historico/Autonomo_4_1.txt","Autonomo41"],
 ["Autonomo/Autonomo42.java","historico/Autonomo_4_2.txt","Autonomo42"],
 ["Autonomo/Autonomo50.java","historico/Autonomo_5_0.txt","Autonomo50"],
 ["Autonomo/Autonomo51.java","historico/Autonomo_5_1.txt","Autonomo51"],
 ["teleOp/TeleOp10.java","historico_teleop/TeleOp_1_0.txt","TeleOp10"],
 ["teleOp/TeleOp11.java","historico_teleop/TeleOp_1_1.txt","TeleOp11"],
 ["teleOp/TeleOp12.java","historico_teleop/TeleOp_1_2.txt","TeleOp12"],
 ["teleOp/TeleOp13.java","historico_teleop/TeleOp_1_3.txt","TeleOp13"]
];
for(const [java,backup,clazz] of codeVersions){
 const a=fs.readFileSync(root+java);
 const h=fs.readFileSync(backup);
 assertIt(a.equals(h),"Backup nao corresponde ao Java: "+java);
 assertIt(a.toString().includes("public class "+clazz+" extends LinearOpMode"),
   "Classe/arquivo incoerentes "+java);
 assertIt(a.toString().includes("finally {"),"STOP finally ausente "+java);
}
for(const file of ["historico/Autonomo_2.txt","historico/Autonomo_3.txt"])
 assertIt(fs.statSync(file).size>0,"Historico antigo ausente: "+file);

const necessary=[
 "README.md","CONTRIBUTING.md","SECURITY.md","CHANGELOG.md",
 ".gitignore",".gitattributes",
 "docs/README.md","docs/ARQUITETURA.md","docs/TESTES.md",
 "docs/PUBLICACAO.md","docs/RELATORIO_REORGANIZACAO.md",
 "assets/meca-tiger-banner.svg","assets/teleop-gamepads.svg",
 "teleop/MANUAL_COMPLETO.md","teleop/ESTUDO_ASSISTENCIA_IMU.md",
 "DOSSIER_TECNICO_COMPLETO_AUTONOMO.md",
 "RELATORIO_GERAL_VALIDACAO_SOFTWARE.md"
];
for(const file of necessary)assertIt(fs.existsSync(file),"Arquivo obrigatorio ausente: "+file);

for(const file of ["README.md","docs/README.md","docs/ARQUITETURA.md",
"docs/TESTES.md","docs/PUBLICACAO.md","docs/RELATORIO_REORGANIZACAO.md",
"teleop/MANUAL_COMPLETO.md","CONTRIBUTING.md","SECURITY.md"]){
 const s=fs.readFileSync(file,"utf8");
 for(const m of s.matchAll(/\]\(([^)]+)\)/g)){
   const target=m[1].trim().split("#")[0].split("?")[0];
   if(!target||/^(https?:|mailto:|#)/i.test(target))continue;
   const dest=path.resolve(path.dirname(file),decodeURIComponent(target));
   assertIt(fs.existsSync(dest),"Link local quebrado "+file+" -> "+target);
 }
}
const mapping=fs.readFileSync(root+"teleOp/TeleOp13.java","utf8");
for(const value of ["gamepad1.back","gamepad1.dpad_right","gamepad1.dpad_left",
  "gamepad2.x","gamepad2.y","gamepad2.a","gamepad2.b",
  "gamepad2.right_bumper","gamepad2.left_bumper","TEMPO_AQUECIMENTO_MS"])
 assertIt(mapping.includes(value),"Manual aponta comando nao implementado: "+value);
assertIt(mapping.includes("MI.setPower(0);")&&mapping.includes("MS.setPower(0);"),
 "Falha de desligamento de mecanismos");
const manual=fs.readFileSync("teleop/MANUAL_COMPLETO.md","utf8");
assertIt(manual.includes("../assets/teleop-gamepads.svg"),"Imagem do controle ausente");
assertIt(manual.includes("0,80 s")&&manual.includes("BACK"),"Manual incompleto");

// Detecta padroes obvios em arquivos atuais. NAO audita historico remoto ou segredos desconhecidos.
const tracked=execFileSync("git",["ls-files","--cached","--others","--exclude-standard","-z"],
{encoding:"utf8"}).split("\0").filter(Boolean);
let scanned=0;
const risky=[
  {name:"GitHub token",pattern:new RegExp("gh"+"p_[A-Za-z0-9]{25,}|github_"+"pat_[A-Za-z0-9_]{30,}")},
  {name:"AWS access key",pattern:new RegExp("AK"+"IA[A-Z0-9]{16}")},
  {name:"Private key",pattern:new RegExp("-----BEGIN (?:RSA |OPENSSH |EC )?PRIVATE KEY-----")},
  {name:"Credential assignment",pattern:new RegExp("(?:password|client_secret|api_key)\\s*[=:]\\s*[\"'][A-Za-z0-9+/=_-]{18,}[\"']","i")}
];
for(const f of tracked){
 if(!fs.existsSync(f)||!fs.statSync(f).isFile())continue;
 const ext=path.extname(f).toLowerCase();
 if(![".java",".md",".yml",".yaml",".mjs",".txt",".svg",".json",".properties",".gradle"].includes(ext))continue;
 const s=fs.readFileSync(f,"utf8");
 scanned++;
 for(const {name,pattern} of risky){
   assertIt(!pattern.test(s),"Possivel "+name+" em "+f+"; revisar antes de publicar");
 }
}
console.log(JSON.stringify({passed:true,versionsCompared:codeVersions.length,
 checks,currentTextFilesScanned:scanned,
 warning:"Verificacao de arquivos atuais nao substitui auditoria de todo historico Git nem testes fisicos"},null,2));
