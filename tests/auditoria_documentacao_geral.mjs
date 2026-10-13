import assert from "node:assert/strict";
import fs from "node:fs";
import path from "node:path";
import crypto from "node:crypto";

const report = fs.readFileSync("RELATORIO_GERAL_VALIDACAO_SOFTWARE.md", "utf8");
const manual = fs.readFileSync("MANUAL_GERAL_CALIBRACAO_TESTES.md", "utf8");
const readme = fs.readFileSync("README.md", "utf8");
const dossier = fs.readFileSync("DOSSIER_TECNICO_COMPLETO_AUTONOMO.md", "utf8");
const index = fs.readFileSync("INDICE_COMPLETO_CODIGOS_AUTONOMO.md", "utf8");
const versions = [
  ["2", "historico/Autonomo_2.txt", null, "65072b9b18a42438df625deca4722596126b006d"],
  ["3", "historico/Autonomo_3.txt", null, "91c6d4ebadcf974df24bd5a9eafeaf502c0c2bba"],
  ["3.1", "historico/Autonomo_3_1.txt", "autonomo.java", "cccc44f9ddfd8b90b06fb826d35e21b19bdc4fcb"],
  ["3.2", "historico/Autonomo_3_2.txt", "Autonomo32.java", "ac3d183421553b9adeba620e6299cdb1504baeb2"],
  ["3.3", "historico/Autonomo_3_3.txt", "Autonomo33.java", "7103e0b86180def8967814960995637ea136e3c8"],
  ["3.4", "historico/Autonomo_3_4.txt", "Autonomo34.java", "62228bf535c382505328a386d70cda85a67b8b2d"],
  ["3.5", "historico/Autonomo_3_5.txt", "Autonomo35.java", "37cc2ac051f014487d45702958e46f0bc4e3455b"],
  ["4.0", "historico/Autonomo_4_0.txt", "Autonomo4.java", "d4a984f7d0f4b680ec1f16ab6d8426b325e2715b"],
  ["4.1", "historico/Autonomo_4_1.txt", "Autonomo41.java", "df6c87f201687018c9138ce4eeb905a07eaa6378"],
  ["4.2", "historico/Autonomo_4_2.txt", "Autonomo42.java", "dfd441704ea442aa7776fbe8e64dea9220e16ecb"],
  ["5.0", "historico/Autonomo_5_0.txt", "Autonomo50.java", "bffe2ce8e23a905a76301854300270a2c5232970"],
  ["5.1", "historico/Autonomo_5_1.txt", "Autonomo51.java", "91081923a507cec0379b8c5a98d41e0817b4b76c"]
];
let count = 0;
function verify(v, message) { assert.ok(v, message); count++; }
function blobHash(buffer) {
  const prefix = Buffer.from("blob " + buffer.length + "\0");
  return crypto.createHash("sha1").update(prefix).update(buffer).digest("hex");
}
const codeRoot = "TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo";
for (const [version, history, source, hash] of versions) {
  const h = fs.readFileSync(history);
  verify(blobHash(h) === hash, version + " history hash mismatch");
  verify(report.includes(hash), version + " SHA absent from report");
  verify(index.includes(hash), version + " SHA absent from code index");
  verify(dossier.includes(history), version + " history not linked in dossier");
  verify(report.includes("| " + version + " |") ||
         report.includes("| **" + version + "** |"), version + " row absent");
  if(source) {
    const js = fs.readFileSync(path.join(codeRoot, source));
    verify(js.equals(h), version + " code differs from history");
    verify(blobHash(js) === hash, version + " Java hash mismatch");
  }
}
for (const doc of [
  "MANUAL_GERAL_CALIBRACAO_TESTES.md",
  "RELATORIO_GERAL_VALIDACAO_SOFTWARE.md",
  "MODELO_REGISTRO_ENSAIO_AUTONOMO.md",
  "GUIA_AUTONOMO_SEM_CAMERA.md",
  "DOSSIER_TECNICO_COMPLETO_AUTONOMO.md",
  "INDICE_COMPLETO_CODIGOS_AUTONOMO.md"
]) {
  verify(readme.includes(doc), doc + " not indexed by README");
}
for(const file of ["README.md","MANUAL_GERAL_CALIBRACAO_TESTES.md","RELATORIO_GERAL_VALIDACAO_SOFTWARE.md","DOSSIER_TECNICO_COMPLETO_AUTONOMO.md","INDICE_COMPLETO_CODIGOS_AUTONOMO.md"]) {
  const data=fs.readFileSync(file,"utf8");
  const rx=/\]\(([^)]+)\)/g;
  for(const m of data.matchAll(rx)) {
    const url=m[1].trim().split("#")[0].split("?")[0];
    if(!url || /^(https?:|mailto:|#)/i.test(url))continue;
    verify(fs.existsSync(path.resolve(path.dirname(file),decodeURIComponent(url))),
      file + ": broken local link " + url);
  }
}
verify(manual.includes("3.5") && manual.includes("5.1"), "both families required");
verify(dossier.includes("ControladorPID.calcular") && dossier.includes("getFreshDetections()"),
  "dossier must describe Java PID and fresh camera detections");
verify(dossier.includes("3.5 sem câmera vs. 5.1 com câmera"),
  "dossier must explain physical comparison");
verify(dossier.includes("P0 — Bloqueios antes de competir"),
  "dossier must retain outstanding risks");
verify(report.includes("BUILD SUCCESSFUL"), "build evidence missing");
verify(report.includes("Sem registro vinculado") && report.includes("Nenhuma versão foi homologada fisicamente"),
  "non-validated physical results must remain explicitly stated");
console.log(JSON.stringify({status:"PASS",versions:versions.length,checks:count,
    note:"Verificacao estatica documental; nao compila Java nem testa hardware"},null,2));
