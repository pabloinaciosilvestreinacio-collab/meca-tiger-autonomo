# Arquitetura de software e hardware

## 1. Escopo

Equipe **Meca Tiger FTC #32578**, robô **Mequinha**, temporada **BIOBUZZ 2026–2027**, SESI SENAI Maracanaú/CE.

O repositório contém classes Java do `TeamCode`, simulações JavaScript e documentação. **Não é um fork completo do FTC SDK.** O usuário deve copiar classes para um projeto `FtcRobotController` compatível para compilar/instalar na escola.

## 2. Dispositivos do hardwareMap

| Nome | Subsistema | Função |
|---|---|---|
| `MD` | Tração | Motor direito, direção Java REVERSE nas classes atuais |
| `ME` | Tração | Motor esquerdo, direção Java FORWARD |
| `MI` | Intake/Outtake | Coleta e alimentação de peças |
| `MS` | Shooter | Lançador |
| `imu` | Sensores | IMU do Control Hub, orientação presumida logo LEFT / USB UP |
| `Webcam 1` | Visão | C270 USB nas classes de autônomo com câmera |

Os valores são **configuração no código**, não calibração física. O chassi é diferencial de dois motores, não mecanum; o software não comanda strafing lateral.

## 3. Fluxo de código

```text
Driver Station + gamepad1/gamepad2
             |
       LinearOpMode (FTC)
        /           \
 TeleOp10...13   Autonomo 3.1...5.1
   |        |        |          |
 chassi    MI/MS  encoders/IMU  Camera + AprilTag
   \        /        \          /
         MD, ME, MI, MS (hardwareMap)
                     |
                REV Control Hub
```

**TeleOp 1.0–1.1:** controle manual. **TeleOp 1.2:** assistência de rumo P conservadora com botão BACK e sinal explicitamente configurado no INIT. **TeleOp 1.3:** integração proposta de intake/lançador, com interlock e armamento deliberado. **Autônomo 3.x:** giros e trajetória com PID manual e encoders. **Autônomo 4.x/5.x:** adiciona C270/AprilTags e fallback por IMU.

## 4. Estratégia de versionamento

- Cada Java é uma OpMode com nome e classe distintos; não sobrescrever fontes antigos.
- As versões 2 e 3 do autônomo são somente snapshots históricos: possuem classe `autonomo` e não podem coexistir instaladas com o `autonomo.java` da 3.1 no mesmo pacote.
- Para toda nova versão há um arquivo Java, snapshot integral no histórico e registro de alterações.
- A lógica do IMU em TeleOp **não** é a mesma implementação PID do Autônomo. Não confundir os ganhos ou copiar sinais sem aferição.
- Os documentos históricos mantêm seu caminho original para não quebrar referências.

## 5. Limites conhecidos

Não existe câmera no TeleOp 1.x. Não há sensor de peça nem RPM fechado em MS nas classes atuais. Os sinais físicos dos motores/IMU e a transmissão podem diferir dos presumidos. O alinhamento AprilTag indica direção relativa, mas não substitui localização absoluta ou compensação balística.

## 6. Links técnicos

[RTC SDK 12.0](https://github.com/FIRST-Tech-Challenge/FtcRobotController/releases/tag/v12.0) · [TeleOp e diagrama](../teleop/MANUAL_COMPLETO.md) · [Dossiê de PID e controles](../DOSSIER_TECNICO_COMPLETO_AUTONOMO.md) · [Testes](TESTES.md).
