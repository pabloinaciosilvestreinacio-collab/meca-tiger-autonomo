# Autônomo 3.4 — Modos de teste e execução SEM câmera
Data: 2026-10-12
Fonte: `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo34.java`.
Cópia histórica integral: `historico/Autonomo_3_4.txt`.
Base: versão 3.3 preservada em arquivo distinto.

## Motivação e mudanças exatas da 3.3
A 3.3 tem apenas a estratégia completa. Para ensaiar um subsistema, a equipe teria que editar constantes ou executar a sequência toda. A 3.4 torna o diagnóstico selecionável **antes do START**, mantendo a matemática de controle da 3.3.

- `@Autonomous(name="Autonomo 3.4 - Testes")`; nome e classe distintos. **Não substitui** `Autonomo 3.3`.
- Novo `enum ModoExecucao`: `GIRO_CURTO`, `DESLOCAMENTO_CURTO`, `COMPLETO`.
- Durante INIT: B escolhe giro curto, X deslocamento curto, Y sequência completa. **A confirma a escolha**. Mudar o modo invalida a confirmação anterior. Se START ocorrer sem confirmação, não há movimento.
- Modo **GIRO_CURTO (padrão):** `+20°` e depois `-20°`, controlados pelo IMU, sem alimentação/intake/lançador.
- Modo **DESLOCAMENTO_CURTO:** `350 mm` em potência máxima de deslocamento `0.22`, com controle PID/IMU e encoders, sem alimentação/intake/lançador.
- Modo **COMPLETO:** mesma estratégia de 3.3, sem alteração: `-50°`, coleta 2 m, `+100°`, três ciclos de alimentação com lançador em potência temporizada.
- Telemetria do modo selecionado, confirmação e configuração no INIT e modo executado no fim.
- Validação pré-START, watchdog, corte por tempo, divergência dos dois encoders, PID manual e `finally` da 3.3 foram preservados.

## Como usar no Driver Station
1. Selecionar OpMode `Autonomo 3.4 - Testes` e pressionar INIT.
2. Deixar robô com área livre, mecanismo desobstruído, operador pronto para STOP.
3. Para ensaio curto, apertar B (+20°/-20°) **ou** X (350 mm). Para estratégia completa, apertar Y.
4. Conferir no Driver Station o modo e `Configuracao=true`, `IMU=true`.
5. Apertar A para confirmar; conferir `Confirmado=true`.
6. Somente então pressionar START. Para mudar seleção, apertar outro botão de modo e confirmar novamente com A.
7. Registrar yaw inicial/final, ticks de cada roda, tempo, motivo e vídeo de bancada.

## Evidências
- A lógica de confirmação foi coberta por duas assertions no teste `tests/autonomo_sem_camera_3x.mjs`.
- O mesmo arquivo modela PID, deslocamento, timeouts e quatro condições de falha em 5.000 cenários.
- Linter `tests/auditoria_autonomo_3x.mjs` verifica nomes e backup histórico, ausência de dependências de câmera, estrutura balanceada, `RUN_WITHOUT_ENCODER` e limites de segurança.
- **Compilação real via FTC SDK e ensaio físico: PENDENTES.** A verificação de texto não comprova compilação.

## Segurança
- Mesmo um teste curto movimenta motores. O teste de giros pode falhar por direção de motor ou IMU invertidos; não pressupor convergência real. Primeiro verificar fisicamente os sinais com `DiagnosticoSinais.java`, disponível na `main`.
- No modo `COMPLETO`, MI e MS podem lançar objetos: executar apenas no espaço preparado e supervisionado.
- `MIN_POTENCIA_VIRADA=0.08` e `POTENCIA_TESTE_DESLOCAMENTO=0.22` são parâmetros iniciais ainda sem medição de atrito real.
- B/X/Y/A/START são **entradas do gamepad durante INIT**, não sensores adicionais.

## Critério para avançar
Confirmação de build, motores com polaridade correta, ensaios curtos repetíveis dentro das tolerâncias definidas, 2 m calibrados, launcher verificado, teste completo sem falhas.
