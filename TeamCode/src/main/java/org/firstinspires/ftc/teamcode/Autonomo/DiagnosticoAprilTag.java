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

    // Este OpMode e o PRIMEIRO teste da C270. Ele nao procura MD, ME, MI,
    // MS ou IMU e, portanto, nao consegue energizar nenhum motor do robo.
    // Execute-o antes do Autonomo 5.1 para confirmar camera, imagem e tags.
    private static final String NOME_WEBCAM = "Webcam 1";
    private static final int CAMERA_LARGURA = 640;
    private static final int CAMERA_ALTURA = 480;
    private static final int DECIMACAO_APRILTAG = 2;

    @Override
    public void runOpMode() {
        VisionPortal portal = null;

        try {
            AprilTagProcessor processor = new AprilTagProcessor.Builder().build();
            // Decimacao 2 privilegia deteccao mais confiavel no primeiro teste.
            // Nao alterar este numero antes de obter uma leitura estavel.
            processor.setDecimation(DECIMACAO_APRILTAG);

            portal = new VisionPortal.Builder()
                    .setCamera(hardwareMap.get(WebcamName.class, NOME_WEBCAM))
                    .setCameraResolution(new Size(CAMERA_LARGURA, CAMERA_ALTURA))
                    .addProcessor(processor)
                    .build();

            telemetry.addLine("C270: diagnostico seguro - SEM MOTORES");
            telemetry.addLine("1) Confirme STREAMING; 2) mostre uma AprilTag;");
            telemetry.addLine("3) mova o robo e confira bearing/range/yaw.");
            telemetry.addData("Webcam configurada", NOME_WEBCAM);
            telemetry.addData("Resolucao", CAMERA_LARGURA + "x" + CAMERA_ALTURA);
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
                    telemetry.addData("Deteccoes no quadro", deteccoes.size());
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
                        telemetry.addData("Yaw", "%.2f graus",
                                cluster.ftcPose.yaw);
                        telemetry.addData("Roll", "%.2f graus",
                                cluster.ftcPose.roll);
                        telemetry.addData("Distancia", "%.2f pol",
                                cluster.ftcPose.range);
                        telemetry.addData("Orientacao", Math.abs(
                                cluster.ftcPose.roll) < 90.0
                                ? "Pontuavel" : "Invertida");
                        telemetry.addLine("Bearing: direita/esquerda deve mudar"
                                + " com o deslocamento lateral do robo.");
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
