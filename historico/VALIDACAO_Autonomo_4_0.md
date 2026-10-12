# Validacao do Autonomo 4.0

Data: 2026-10-12

## Base
- Derivado do Autonomo 3.1 sem substituir o arquivo ativo.
- Mantidos os parametros de estrategia, PID, encoder, rampas e segurancas do 3.1.
- Adicionada visao AprilTag/Cluster antes do lancamento.

## Compatibilidade de visao verificada
- VisionPortal + AprilTagProcessor.
- API atual com AprilTagDetection, AprilTagClusterDetection e AprilTagSingleDetection.
- Cluster tem prioridade; tag individual e fallback defensivo.
- Webcam configurada como "Webcam 1".

## Validacoes executadas
- Compilacao Java estatica com stubs das APIs FTC usadas: OK.
- Conferencia das assinaturas de visao com o sample oficial RobotAutoDriveToAprilTagTank: OK.
- Loops de movimento possuem condicoes de STOP/tempo; normalizacao angular e finita por entrada validada.
- Teste logico de giro em 1000 variacoes de planta: 1000/1000 convergiram dentro de 7 s.
- Tempo medio da simulacao de giro: ~2.18 s; pior caso observado: ~4.31 s.
- Alinhamento visual com limite ajustado para 2.5 s: 4999/5000 convergiram em simulacao ampla sem perda de frames.
- Simulacao de deslocamento com assimetria mecanica, ruido e taxas variadas: comportamento coerente; protecoes de encoder/rumo mantidas.

## Cenários de falha tratados
- Camera ausente: continua pelo fallback de IMU.
- Camera inicializada mas sem STREAMING: fallback de IMU.
- Nenhum alvo: nao move pela camera; mantem rumo do IMU.
- Alvo muda antes da confirmacao: reinicia confirmacao.
- Dados NaN/invalidos: deteccao ignorada.
- Alvo perdido apos movimento: para e retorna ao rumo anterior por IMU.
- Timeout durante alinhamento: retorna ao rumo por IMU.
- Pouco tempo restante: pula a etapa de visao para preservar o lancamento.
- IMU invalida: aborta etapa com diagnostico.
- Encoder sem progresso: aborta deslocamento com diagnostico.
- STOP/tempo global: todos os loops relevantes encerram.

## Pontos que ainda dependem do robo real
1. Sinal fisico do giro e do bearing da camera.
2. Posicao/orientacao real da Logitech C270 no robo.
3. Escolha do cluster desejado na estrategia de campo.
4. Ajuste fino de KP_ALINHAMENTO_VISAO, potencia minima/maxima e tolerancia.
5. Exposicao/ganho da C270 para iluminacao real.
6. Confirmacao de distancia por encoder e atrito do piso.

A simulacao reduz risco logico, mas nao substitui validacao fisica.
