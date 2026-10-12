import assert from "node:assert/strict";
import { readFileSync } from "node:fs";

const root = "TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/";
const cases = [
    ["autonomo.java", "Autonomo_3_1.txt", "autonomo", "Autonomo 3.1"],
    ["Autonomo32.java", "Autonomo_3_2.txt", "Autonomo32", "Autonomo 3.2"],
    ["Autonomo33.java", "Autonomo_3_3.txt", "Autonomo33", "Autonomo 3.3"],
    ["Autonomo34.java", "Autonomo_3_4.txt", "Autonomo34", "Autonomo 3.4 - Testes"]
];

let checks = 0;
function verify(cond, label) {
    assert.ok(cond, label);
    checks++;
}

function balancedJava(src) {
    const stack = [];
    let mode = "code";
    for (let i=0; i<src.length; i++) {
        const c=src[i], next=src[i+1] || "";
        if(mode==="line") { if(c==="\n") mode="code"; continue; }
        if(mode==="block") { if(c==="*" && next==="/") { mode="code"; i++; } continue; }
        if(mode==="string" || mode==="char") {
            if(c==="\\") { i++; continue; }
            if((mode==="string" && c==='"') || (mode==="char" && c==="'")) mode="code";
            continue;
        }
        if(c==="/" && next==="/") { mode="line"; i++; continue; }
        if(c==="/" && next==="*") { mode="block"; i++; continue; }
        if(c==='"') {mode="string"; continue;}
        if(c==="'") {mode="char"; continue;}
        if("({[".includes(c)) stack.push(c);
        if(")}]".includes(c)) {
            const matching={")":"(","]":"[","}":"{"}[c];
            if(stack.pop()!==matching) return false;
        }
    }
    return mode!=="string" && mode!=="char" && mode!=="block" && stack.length===0;
}

for(const [source, backup, name, label] of cases) {
    const content=readFileSync(root+source,"utf8");
    const history=readFileSync("historico/"+backup,"utf8");
    verify(content === history, source+" != history "+backup);
    verify(content.includes('public class '+name+' extends LinearOpMode'), source+" class name");
    verify(content.includes('@Autonomous(name = "'+label+'"'), source+" OpMode label");
    verify(!/(VisionPortal|AprilTag|WebcamName|org\.firstinspires\.ftc\.vision)/.test(content), source+" vision dependency");
    verify(!content.includes("RUN_TO_POSITION"), source+" unexpected RUN_TO_POSITION");
    verify(content.includes("} finally {") || content.includes("finally {"), source+" safe finally");
    verify(balancedJava(content), source+" unbalanced structure");
}

for (const name of ["Autonomo33.java","Autonomo34.java"]) {
    const c=readFileSync(root+name,"utf8");
    verify(c.includes("LIMITE_DIFERENCA_ENCODER_TICKS"),name+" wheel divergence guard");
    verify(c.includes("tempoPiorCaso()"),name+" timing budget");
    verify(c.includes("LIMITE_DESVIO_RUMO_GRAUS"),name+" IMU heading guard");
    verify(c.includes("tempoDisponivelMovimento()"),name+" movement reserve");
    verify(c.includes("ControladorPID"),name+" manual PID");
    verify(c.includes("Ciclos alimentacao comandados"),name+" honest shot telemetry");
}
console.log("AUDITORIA ESTATICA OK — "+checks+" verificacoes; NAO e compilacao Java");
