# Historico de alteracoes

## Autonomo 3.2 — sem camera

- Nova classe `Autonomo32` independente, baseada na 3.1 preservada.
- Sequencia original inalterada: -50°, coletar 2000 mm, +100°, lancar 3 tentativas temporizadas.
- Corrige a ultrapassagem de distancia: parada pela media dos encoders, depois correcao final de rumo girando parado.
- Corrige derivada angular do PID na passagem +180/-180.
- Watchdog de giro sem progresso e parada ao entrar na tolerancia angular.
- Reserva 2,45 s + margem para inicio do lancamento, com abort seguro se nao houver tempo.
- Tratamento de falhas do IMU/runtime, validacao de parametros e modo sem encoder nos motores MI/MS.
- Preserva `REDUCAO=20.0` como hipotese da 3.1; `FATOR_CALIBRACAO_DISTANCIA=1.0` explicitamente nao aferido.
- Testes: `tests/autonomo32_logic_tests.mjs` e `tests/autonomo32_travel_model.mjs`.
- Evidencias: `historico/VALIDACAO_Autonomo_3_2.md`; compilacao FTC/Gradle nao confirmada nem teste fisico executado.

## Autonomo 5.1
- Exige confirmacao com A no INIT para impedir movimentos com a alianca/rota selecionadas por engano.
- Busca dinamicamente CELL pontuavel da propria alianca; `ftcPose.roll` filtra CELL invertida e `bearing` ajustado escolhe mira.
- Mantem os quatro nomes de cluster oficiais e a preferencia inicial do D-pad.
- Validacao: `tests/autonomo51_cell_tests.mjs` (22.656 assertions, 10.000 listas aleatorias no modelo) e `tests/autonomo51_pid_model.mjs` (10.000 + 10.000 giros em modelo PID P/I/D).
- Acrescentado `DiagnosticoSinais.java` para yaw/encoders sem energizar motores.
- Falta compilar no SDK FTC real e realizar testes fisicos.

## Autonomo 5.0
- Selecionar alianca e lado por gamepad no INIT, sem recompilar constantes.
- Testes: `tests/autonomo50_synthetic_tests.mjs`, 22 assertions e 10.000 cenarios sinteticos.
- Rota de deslocamento e angulos permanecem comuns aos lados e precisam validacao geometrica.

## Autonomo 4.2
- Tempos limitados: giro ate 4,5 s, deslocamento ate 9,5 s e correcao final ate 1,5 s.
- Inclui watchdog de giro sem reducao de erro por 1,2 s, e reserva de tempo durante a visao.
- Limite teorico combinado das etapas, retorno visual, tiro e margem = 26,90 s < 29 s utilizaveis.

### Relatorio complementar
`historico/VALIDACAO_VERSOES_4_2_A_5_1.md`. Nenhum merge na main.

## Autonomo 4.1 — candidato, branch de revisao

- Criado `Autonomo41.java` sem alterar o 3.1 nem o 4.0.
- Video com `getFreshDetections()` para evitar reutilizar frames antigos.
- Comando visual interrompido quando o frame fica mais velho que 80 ms; confirmacao zerada apos 180 ms e fallback apos 350 ms sem alvo depois de iniciar movimento.
- Derivada do PID angular corrigida para continuidade perto de +/-180 graus.
- Reducao teorica calculada com dentes reais 76/21 e 68/13 (18,9304:1); inclui fator de eventual transmissao adicional nas rodas.
- OpMode `DiagnosticoAprilTag` sem motores para verificar a C270 e clusters do BIOBUZZ.
- Testes reproduziveis em `tests/autonomo41_logic_tests.mjs`: 7 assertions e 7.500 cenarios de visao executados (modelo simplificado).
- Detectada limitacao estrutural do orcamento total: soma dos timeouts pode exceder 29 s.
- Evidencias/pendencias em `historico/VALIDACAO_Autonomo_4_1.md`.
- Sem build do projeto FTC completo, sem validacao de hardware, sem merge em `main`.

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
