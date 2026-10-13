# Historico de alteracoes

## 12/10/2026 — Meca Sim 0.1 (protótipo local, branch própria)

- Criado `simulador/`: vista de cima/lateral, editor Java, modelo diferencial, encoders, IMU ideal, gamepads e balística manual.
- Execução Java real em JVM local contra camada FTC parcial; INIT/START/STOP, console e limite de duração.
- Incluídos planejamento, documentação, exemplos, testes Python/JavaScript e workflow específico. Não modifica OpModes ou históricos.
- Modelo sem colisão/atrito/câmera; gamepad físico e revisão visual no navegador pendentes. Não equivale a build Android FTC nem calibração física.

## 12–13/10/2026 — Reorganização integrada do repositório e TeleOp 1.2–1.3

**Objetivo:** projeto mais intuitivo e preparado para revisão pública futura, sem apagar históricos.

- **README** refeito em português; banner SVG original em grafite/laranja; portal `docs/README.md` e mapa da arquitetura; links para os manuais do autônomo e TeleOp.
- **Históricos integrados:** autônomo 2–5.1 (com e sem câmera) e TeleOp 1.0/1.1, preservados com fontes e backups idênticos.
- **TeleOp 1.2** (classe nova): preserva o chassi manual da 1.1, integra IMU e BACK como correção **proporcional** de rumo enquanto há translação; requer seleção e confirmação da polaridade do yaw no INIT (D-pad ESQ/DIR + A). Sem IMU válido, sem confirmação ou com erro de rumo maior que 12°, a assistência não atua.
- **TeleOp 1.3** (classe nova): integra MI/MS sob gamepad2 com intertravamento inicial desarmado, **Y arma**, **X desarma**, **LB intake**, **B reversão que desliga MS**, **RB MS enquanto pressionado**, **A alimenta apenas após aquecimento de RB por 0,80 s**. STOP zera MD/ME/MI/MS, sem garantir parada mecânica instantânea do MS em FLOAT.
- **Manual de gamepads** com desenho vetorial editável `assets/teleop-gamepads.svg`; mapeamentos novos explicitamente experimentais até validação física e aprovação dos pilotos.
- **Engenharia reprodutível:** scripts `tests/teleop_12_13_logic.mjs` e `tests/repositorio_integridade.mjs`; workflow `.github/workflows/ftc-projeto.yml` para build das classes em FTC SDK **v12.0** e regressão das duas linhas.
- **Publicabilidade:** CONTRIBUTING, SECURITY, modelos para issues/PRs, .gitignore, .gitattributes e checklist `docs/PUBLICACAO.md`. Repositório ainda privado, sem licença escolhida pela equipe e sem publicização automática.
- **Ajustes encontrados na revisão:** corrigido simulador que perdia campos do gamepad, corrigida detecção de texto na auditoria estática, checado retorno booleano de `IMU.initialize()` e impedido MS ligado durante reversão MI. Todas as correções foram registradas em commits separados.
- **Validação física:** nenhuma execução desta linha TeleOp 1.2/1.3 no Control Hub; sinais, transmissão e mecanismos ainda precisam de testes seguros. O resultado final dos jobs de GitHub Actions deve ser registrado no `docs/RELATORIO_REORGANIZACAO.md` após conclusão.

## 12/10/2026 — Dossiê integral de toda a programação FTC (documentação)

- Criado **DOSSIER_TECNICO_COMPLETO_AUTONOMO.md** (~600 linhas): histórico técnico de todas as 12 versões (2–5.1), justificativas de cada melhoria, arquitetura do Java, runOpMode, tarefas, PID manual, encoder, IMU, lançamento, câmera C270, AprilTag Clusters, fallback e orçamentos.
- Criado **INDICE_COMPLETO_CODIGOS_AUTONOMO.md**: links dos 12 códigos completos, backups, hashes de blob, métodos-chave, diagnósticos e instruções de comparação.
- Dossiê inclui manual progressivo de calibração/ensaios e integra links aos relatórios e fichas existentes, sem duplicar milhares de linhas dos fontes Java.
- Status de evidência preservado: **build FTC conjunto aprovado no Actions #38321732887; nenhum teste físico concluído**.
- Arquivos Java originais e históricos não são alterados nesta entrega; mudanças documentais mantidas na branch PR #4 e sem merge em main.

### Integração conjunta FTC SDK — prova real com câmera (12/10/2026)
- [GitHub Actions run #38321732887](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/actions/runs/38321732887): **BUILD SUCCESSFUL in 2m 42s**; tarefas TeamCode:compileDebugJavaWithJavac e TeamCode:assembleDebug. Compilou todas as classes Java presentes, incluindo 4.0, 4.1, 4.2, 5.0, 5.1 e diagnósticos.
- Scripts Node de visão, PID e encoders também passaram no mesmo run. O [run #38321932005](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/actions/runs/38321932005) executou auditoria documental **12 versões / 87 verificações** com SUCCESS.
- Arquivos Java não foram modificados nesta atualização de documentação; montagem da C270, mecanismos e geometria continuam sem testes físicos comprovados.
- Referência consolidada: RELATORIO_GERAL_VALIDACAO_SOFTWARE.md.

## 2026-10-12 — Documentacao transversal de TODAS as versoes

- Criado MANUAL_GERAL_CALIBRACAO_TESTES.md: procedimento progressivo e seguro para motor/IMU/encoder, PID, distancia, MI/MS, C270, AprilTags, limites temporais e comparacao justa.
- Criado RELATORIO_GERAL_VALIDACAO_SOFTWARE.md: rastreabilidade de 12 versoes (2, 3, 3.1-3.5, 4.0-4.2, 5.0-5.1), SHA de blobs, alteracoes e status por nivel de evidencia.
- Criado MODELO_REGISTRO_ENSAIO_AUTONOMO.md: ficha reutilizavel com criterios de aceite, medidas e vinculo com versao/commit.
- Adicionado workflow de auditoria conjunta FTC (.github/workflows/ftc-auditoria-todas-versoes.yml), para tentar compilar todas as classes, com e sem camera, no projeto FTC oficial.
- Preservado o status real: FTC 3.1-3.5 compilado no Actions #38319766443; camera so sera considerada compilada mediante job aprovado; testes fisicos nao registrados.
- Corrigido README para incluir links de todos os novos documentos, sem alterar as classes Java existentes.

### Integração e compilação FTC SDK real — 12/10/2026
- GitHub Actions [run #38319766443](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/actions/runs/38319766443): **2/2 jobs aprovados**. Java/Gradle `compileDebugJavaWithJavac` e `assembleDebug` concluídos com `BUILD SUCCESSFUL`.
- Node: **21 assertions**, **5.000 simulações**; auditoria estática: **58 verificações** aprovadas.
- Código Java efetivamente compilado possui os mesmos hashes das versões 3.1–3.5 atuais. Sem movimento real do robô, pendente validação física.
- Detalhes: `historico/RELATORIO_CI_SEM_CAMERA_3X.md`.

## Autonomo 3.5 — Bancada (novo, sem camera)

- Adiciona modo `TESTE_LANCADOR` com D-pad para cima no INIT; continua exigindo confirmacao por A, seguido de START.
- Confirmacao pela **borda de subida** do botao A: trocar o modo com A ja pressionado nao confirma a nova escolha.
- O modo isolado de MI/MS desliga a tracao antes dos tres ciclos de alimentacao/lançamento por tempo, sem mover o robo.
- `sequencia()` registra tempo e resultado individual por etapa; excecoes identificam a etapa que falhou.
- Mantem os modos da 3.4, toda a logica de controle da 3.3 e todas as versoes anteriores preservadas.
- Novo `Autonomo35.java`, `historico/Autonomo_3_5.txt` e `historico/VALIDACAO_Autonomo_3_5.md`.
- Movimento nao validado fisicamente e contagem de pecas continua apenas temporal.

## Autonomo 3.4 (novo, sem camera)

- Classe separada `Autonomo34.java` e OpMode `Autonomo 3.4 - Testes`.
- Selecao no INIT: B = giros +20° e -20°; X = 350 mm a potencia 0,22; Y = ciclo completo de 3.3.
- Confirmacao obrigatoria pelo botao A; mudar modo remove confirmacao; START sem confirmar bloqueia movimentos.
- Nos dois modos de teste: sem ligar `MI` ou `MS` para coletar/alimentar/lancar.
- Backup `historico/Autonomo_3_4.txt` e documento `historico/VALIDACAO_Autonomo_3_4.md`.

## Autonomo 3.3 (novo, sem camera)

- Nova classe `Autonomo33.java` derivada exatamente de `Autonomo32.java`, mantendo a 3.2 inalterada.
- Timeout de giro: 7 -> 5 s; timeout de translacao: 12 -> 10 s. Correcao final de rumo 1,5 s preservada.
- Validacao preventiva de parametros, limites dos motores e prazo maximo: 24,3 s de etapas mais margem, inferior a 29 s de tempo seguro.
- Monitor de divergencia dos encoders: cancela se abs(MD-ME)>220 ticks. Monitor de erro de rumo: cancela se abs(erro)>35°.
- Telemetria mais explicita, incluindo diferenca dos encoders e 'ciclos de alimentacao comandados' sem afirmar que houve 3 lancamentos.
- Corrigida deteccao de numeros invalidos na configuracao (NaN, infinidade e potencias fora de [-1, 1]).
- Backups `historico/Autonomo_3_3.txt`, relatorio `historico/VALIDACAO_Autonomo_3_3.md`.
- Simulacao JS executada: 17 assertions; 5.000 cenarios em cinco condicoes (1000 normais concluindo, 4.000 falhas sinteticas interrompidas pelos respectivos guardas).
- Compilacao no SDK real nao confirmada; dados de atrito, sinais do IMU, encoders, reducao, RPM e lancamentos nao estao calibrados.

### Guia completo
[GUIA_AUTONOMO_SEM_CAMERA.md](GUIA_AUTONOMO_SEM_CAMERA.md). Nao inclui nem modifica a linha 4.x/5.x com camera.

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
