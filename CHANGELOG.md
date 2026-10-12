# Historico de alteracoes

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
