import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import { createHash } from "node:crypto";

const root = "TeamCode/src/main/java/org/firstinspires/ftc/teamcode/teleOp/";
const versions = [
  ["TeleOp10.java", "historico_teleop/TeleOp_1_0.txt", "TeleOp10", "TeleOp 1.0 - Chassi"],
  ["TeleOp11.java", "historico_teleop/TeleOp_1_1.txt", "TeleOp11", "TeleOp 1.1 - Manual Assistido"]
];
let assertions = 0;
function verify(x, message) {
  assert.ok(x, message);
  assertions++;
}
for (const [file, history, className, opMode] of versions) {
  const java = readFileSync(root + file, "utf8");
  const copy = readFileSync(history, "utf8");

  verify(java === copy, file + " differs from backup");
  verify(java.includes("public class " + className + " extends LinearOpMode"),
    file + " wrong class");
  verify(java.includes('@TeleOp(name = "' + opMode + '"'), file + " wrong annotation");
  verify(java.includes('hardwareMap.get(DcMotor.class, "MD")'), file + " missing MD");
  verify(java.includes('hardwareMap.get(DcMotor.class, "ME")'), file + " missing ME");
  verify(java.includes("} finally {"), file + " missing finally block");
  verify(!/VisionPortal|WebcamName|AprilTag|\.vision\./.test(java),
    file + " must not depend on camera");
  verify(!/\bIMU\b|\.getYaw\(/.test(java),
    file + " is intentionally manual only");
  verify(java.includes("MD.setPower(0);") && java.includes("ME.setPower(0);"),
    file + " missing zero-power exit");
  verify(java.includes("double potenciaMD = -x - y;")
    || java.includes("double potenciaMD = Range.clip(-x - y, -1.0, 1.0);"),
    file + " must preserve requested mapping");
  const sha = createHash("sha1").update("blob " + Buffer.byteLength(java) + "\0" + java).digest("hex");
  console.log(file + " OK / Git blob " + sha);
}
const newer = readFileSync(root + "TeleOp11.java", "utf8");
verify(newer.includes("startAtual && !startAnterior"), "START must toggle on edge");
verify(newer.includes("boolean invertendo"), "ramp must handle reversals");
verify(newer.includes("Math.max(Math.abs(potenciaMD), Math.abs(potenciaME))"),
  "power normalization required");
verify(newer.includes("if (gamepad1.left_bumper)"),
  "precision must have priority");
verify(newer.includes("} else if (gamepad1.right_bumper)"), "turbo fallback missing");
console.log(JSON.stringify({passed: true, assertions, versions: 2,
  limitation: "Text and hash audit only; not FTC compilation or physical test."}, null, 2));
