# Meca Tiger - Autonomo FTC

Repositorio do codigo autonomo da equipe Meca Tiger FTC #32578.

## Versao atual

`Autonomo 3.1`

O codigo atual executa:

1. Virada relativa de -50 graus.
2. Captura do rumo pelo IMU.
3. Coleta durante um deslocamento de 2000 mm.
4. Virada relativa de +100 graus.
5. Lancamento de 3 pecas por temporizacao.

O controle de tracao usa encoders e IMU em `RUN_WITHOUT_ENCODER`, com PID
calculado manualmente pelo programa.

## Codigo

O arquivo principal esta em:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/autonomo.java`

Para usar no projeto FTC SDK, coloque esse arquivo no mesmo caminho dentro do
modulo `TeamCode`.

## Hardware configurado

| Nome | Funcao |
| --- | --- |
| `MD` | Motor direito da tracao |
| `ME` | Motor esquerdo da tracao |
| `MI` | Intake/alimentador |
| `MS` | Lancador |
| `imu` | IMU do Control Hub |

## Aviso de teste

A logica foi simulada e a sintaxe foi verificada com interfaces equivalentes
as chamadas do FTC SDK. A validacao final deve ser feita no Android Studio e
no robo, conferindo direcoes dos motores e orientacao fisica do Control Hub.
