# Historico de alteracoes

## Autonomo 4.0

- Nova linha com camera, mantendo o Autonomo 3.1 intacto.
- Compatibilidade com FTC SDK 12.0 e AprilTag Clusters do BIOBUZZ.
- Uso exclusivo dos quatro clusters da temporada: RED SCORING, RED AUDIENCE, BLUE SCORING e BLUE AUDIENCE.
- Selecao de alvo por alianca e lado do campo.
- Filtro de CELL invertida usando `ftcPose.roll`.
- Logitech C270 fixada em 640x480, resolucao com calibracao oficial no SDK.
- Adicionado `OFFSET_BEARING_CAMERA_GRAUS` para calibrar a montagem da camera.
- Visao fail-soft: ausencia, erro, timeout ou perda de alvo retornam ao controle por IMU.
- Reducao do drivetrain corrigida das relacoes nominais 4:1 × 5:1 para as relacoes reais 3,61:1 × 5,23:1.
- Adicionado `FATOR_CALIBRACAO_DISTANCIA` para ajuste de campo.
- Condicao final do deslocamento corrigida para impedir que o robo continue avancando apenas para igualar encoders.
- Correcao final de rumo feita por giro no proprio eixo depois de atingir a distancia.
- Potencia minima de giro e alinhamento visual ajustada para 0,10 apos testes de sensibilidade.
- Validacao preventiva das constantes mecanicas, de estrategia, camera, controle e tempo.
- Normalizacao angular sem loops potencialmente infinitos.
- Tratamento adicional de excecoes de IMU, camera e runtime.
- Todos os loops operacionais permanecem limitados por STOP, tempo global e/ou timeout.
- Relatorio detalhado em `historico/VALIDACAO_Autonomo_4_0.md`.

## Autonomo 3.1

- Parametros da estrategia reunidos em constantes.
- PID manual completo com `KP`, `KI` e `KD`.
- Integral limitada e protecao contra acumulacao durante saturacao.
- Derivada filtrada para reduzir ruido do IMU.
- Sequencia dividida em tarefas com diagnostico por etapa.
- Deteccao separada de falta de progresso dos encoders `MD` e `ME`.
- Aceleracao e desaceleracao progressivas no deslocamento.
- Timeout de virada ajustado para 7 segundos.
- Timeout de deslocamento ajustado para 12 segundos.
- Telemetria ampliada com yaw, encoders, potencia e motivo de falha.

## Versao anterior

A referencia anterior foi preservada em `historico/Autonomo_anterior.txt`.
