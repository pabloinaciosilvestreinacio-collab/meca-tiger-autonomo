package org.firstinspires.ftc.teamcode.Autonomo;

import android.util.Size;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagClusterDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;

@TeleOp(name = "Diagnostico AprilTag BIOBUZZ", group = "Teste")
public class DiagnosticoAprilTag extends LinearOpMode {

    @Override
    public void runOpMode() {
        VisionPortal portal = null;

        try {
            AprilTagProcessor processor = new AprilTagProcessor.Builder().build();
            processor.setDecimation(2);

            portal = new VisionPortal.Builder()
                    .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))
                    .setCameraResolution(new Size(640, 480))
                    .addProcessor(processor)
                    .build();

            telemetry.addLine("Diagnostico de camera - sem motores");
            telemetry.addLine("Inicie para observar novos quadros");
            telemetry.update();

            waitForStart();

            while (opModeIsActive()) {
                telemetry.addData("Camera", portal.getCameraState());

                List<AprilTagDetection> deteccoes = processor.getFreshDetections();

                if (deteccoes == null) {
                    telemetry.addLine("Nenhum quadro novo processado");
                } else if (deteccoes.isEmpty()) {
                    telemetry.addLine("Quadro novo sem alvo");
                } else {
                    for (AprilTagDetection det : deteccoes) {
                        if (!(det instanceof AprilTagClusterDetection)) {
                            continue;
                        }

                        AprilTagClusterDetection cluster =
                                (AprilTagClusterDetection) det;

                        if (cluster.metadata == null || cluster.ftcPose == null) {
                            continue;
                        }

                        String nome = cluster.metadata.shortName;

                        if (nome == null || nome.isEmpty()) {
                            nome = cluster.metadata.name;
                        }

                        telemetry.addData("Cluster", nome);
                        telemetry.addData("Bearing", "%.2f graus",
                                cluster.ftcPose.bearing);
                        telemetry.addData("Roll", "%.2f graus",
                                cluster.ftcPose.roll);
                        telemetry.addData("Distancia", "%.2f pol",
                                cluster.ftcPose.range);
                        telemetry.addData("Orientacao", Math.abs(
                                cluster.ftcPose.roll) < 90.0
                                ? "Pontuavel" : "Invertida");
                    }
                }

                telemetry.update();
                sleep(50);
            }
        } catch (RuntimeException e) {
            telemetry.addData("Falha da camera",
                    e.getClass().getSimpleName());
            telemetry.update();
        } finally {
            if (portal != null) {
                portal.close();
            }
        }
    }
}
