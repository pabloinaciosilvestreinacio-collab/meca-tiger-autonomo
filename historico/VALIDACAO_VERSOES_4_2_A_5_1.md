# Auditoria profissional FTC Meca Tiger — versões 4.2, 5.0 e 5.1
Data: 12 de outubro de 2026.
Escopo: evolução do Autônomo 4.1. Não altera nem substitui 3.1, 4.0 ou 4.1.
Branch: `revisao/autonomo-4.1-validacao`.

## Fontes de referência
- Skill `.agents/skills/ftc-engineer/SKILL.md`, `references/hardware.md` e `references/validation.md`.
- FTC SDK 12.0 (20260907-090034): https://github.com/FIRST-Tech-Challenge/FtcRobotController
- AprilTag Clusters: https://ftc-docs.firstinspires.org/en/latest/tech_tips/tech-tips/tech-tip-apriltag-clusters/tech-tip-apriltag-clusters.html
- AprilTag fresh: https://javadoc.io/static/org.firstinspires.ftc/Vision/11.1.0/org/firstinspires/ftc/vision/apriltag/AprilTagProcessor.html
- REV: https://docs.revrobotics.com/rev-crossover-products/ultraplanetary/cartridge-details
- Temporização oficial BIOBUZZ: https://ftc-resources.firstinspires.org/ftc/archive/2027/game

## Histórico versionado
| Versão | Código novo | Alteração efetiva | Evidência |
| --- | --- | --- | --- |
| 4.2 | `Autonomo42.java` | Timeouts de giro 4,50 s e deslocamento 9,50 s, watchdog do erro de rumo (1,20 s sem melhora >= 1°), guarda de tempo de visão e reserva de lançamento | Análise de orçamento 26,90 s < 29 s utilizáveis, modelo sintético 5.0 |
| 5.0 | `Autonomo50.java` | Pré-seleção de aliança (D-pad esquerda/direita) e lado (D-pad cima/baixo) no INIT, sem editar Java a cada rodada; manter seleção de cluster válida | Teste sintético `tests/autonomo50_synthetic_tests.mjs` |
| 5.1 | `Autonomo51.java` | Exigir A para confirmar estratégia; sem confirmação não movimenta. Seleção dinâmica de CELL pontuável da própria aliança pelo `abs(ftcPose.roll)<90`, priorizando menor erro bearing mesmo se outro lado estiver pontuável | Teste `tests/autonomo51_cell_tests.mjs` |

### Vantagem no BIOBUZZ
Os dois clusters de uma aliança estão associados a CELLS cuja orientação muda. Fixar `RED SCORING` ignoraria `RED AUDIENCE` quando esta fosse pontuável. O 5.1 mantém quatro nomes oficiais, filtra RED/BLUE e a posição scorable pelo roll, e escolhe a candidata admissível mais próxima da mira óptica. A escolha SCORING/AUDIENCE no INIT ainda informa a preferência de lado de partida; **o código NÃO espelha automaticamente a trajetória em função dessa seleção**. Ângulos de -50°, deslocamento de 2.000 mm e giro +100° continuam os planejados para a rota-base e precisam ser verificados na geometria de campo e no ponto de partida.

### Orçamento detalhado 4.2+
- Giro para coleta: no máximo 4,50 s.
- Deslocamento: até 9,50 s + possível correção angular final até 1,50 s.
- Giro de lançamento: no máximo 4,50 s.
- Busca/alinhamento: até 2,50 s + possível retorno IMU 1,60 s.
- Alimentação/lançamento de três elementos: 0,80 + 3 × 0,45 + 2 × 0,15 = 2,45 s.
- Margem extra da rotina: 0,35 s.
- **Total teórico pessimista de ações e margem: 26,90 s**, menor do que o limite de operação do código (29 s, preservando 1 s do AUTO de 30 s).
- Esta soma não é uma garantia de conclusão: se uma etapa não convergir ou o sistema sofrer interrupção, o código aborta, e atrasos do scheduler / JVM / mecanismos reais não podem ser provados por análise.
- A verificação `validarConfiguracao()` impede constantes com soma de limites incompatíveis com o AUTO. A visão reavalia o saldo de tempo antes de atuar e devolve prioridade ao tiro/fallback.

## Testes reproduzíveis efetivamente executados no ambiente de análise
1. **Autônomo 5.0 (modelo de controle e cronograma)**: 22 assertions aprovadas; 10.000 cenários de linha do tempo dentro dos limites formais; 10.000 cenários simples de giro (todos convergiram no modelo simplificado).
2. **Autônomo 5.1 (detecções sintéticas)**: 22.656 assertions aprovadas; 10.000 listas aleatórias; 7.549 continham um cluster elegível escolhido. Sem detecção elegível, retorno null e fallback apropriado. Verificação de aliança, orientação, bearing e offset.
3. **Autônomo 5.1 (PID com P/I/D real do código, anti-windup e filtro)**: 10.000 simulações de -50° e 10.000 de +100°; 20.000 convergências no modelo, sem timeout nem watchdog. Ruído de 0,06–0,26° e ganho/inércia/atrito sintéticos. Pior tempo bem-sucedido 3,0 s; pior erro real na convergência ~2,134° (tolerância do código aplicada à leitura ruidosa de 2°).
4. **Inspeção 5.1 e arquivos históricos**: conferir hashes de arquivo por GitHub SHA antes de merge; atualização de cópias `historico/Autonomo_4_2.txt`, `Autonomo_5_0.txt`, `Autonomo_5_1.txt`.
5. **Testes anteriores 4.1**: relatório existente; suas cifras só devem ser tomadas como evidência quando reproduzidas pelo comando indicado.

Comandos:
```sh
node tests/autonomo41_logic_tests.mjs
node tests/autonomo50_synthetic_tests.mjs
node tests/autonomo51_cell_tests.mjs
node tests/autonomo51_pid_model.mjs
```

O teste matemático não executa Java nem controla motores. Parâmetros sintéticos não estabelecem taxa de sucesso real.

## CI SDK verdadeiro
- Workflow `.github/workflows/ftc-validation.yml` adicionada para fazer checkout de FTC SDK oficial, copiar `Autonomo51.java`, `DiagnosticoAprilTag.java`, `DiagnosticoSinais.java` e executar `./gradlew :TeamCode:assembleDebug`.
- **Nenhum status de CI foi retornado pelo GitHub até a escrita deste relatório. Compilação FTC real: NÃO CONFIRMADA / PENDENTE.**
- Para validar: abrir GitHub Actions do repositório/branch e executar; se falhar, corrigir erros concretos e repetir antes de instalar.

## OpModes independentes para a bancada
- `DiagnosticoAprilTag.java`: captura sem mover motores, exibe o cluster, bearing, roll e range.
- `DiagnosticoSinais.java`: potência zero; movimentos manuais das rodas e chassi; telemetria de yaw/encoders. **Não controla motores automaticamente**.

## Riscos ainda existentes (não resolver por simulação inventada)
- **Geometria**: escolher lado na tela não muda a rota física; confirmar se -50°, 2 m, +100° são válidos e legais em cada posição de partida.
- **Mecânica**: cartuchos efetivamente instalados, relação externa, diâmetro efetivo, atrito mínimo e patinagem.
- **Sinais**: MD/ME, encoder positivo, Control Hub LEFT/UP e rotação positiva do IMU.
- **Câmera**: montagem C270, offset angular, latência/fps, intrínsecos, posição do projetor real do shooter e iluminação.
- **Lançador**: velocidade real sob carga, cadência, peças na entrada; contagem atual por tempo NÃO garante que três peças de fato tenham sido pontuadas.
- **Sistema**: build FTC/Gradle de verdade, USB/saturação térmica/CPU, comunicação, testes de parada em falhas de runtime.
- **Tempo real**: limites individuais no código não garantem conclusão de etapa mecânica; registro de logs reais indispensável.
- Nenhuma alteração de código foi instalada no Control Hub nem quaisquer motores foram acionados neste trabalho.

## Critério para promover uma versão 6.0 estável
Não criar número 6.0 apenas por estética. Requisito mínimo: build FTC 12.0 comprovado; diagnósticos de sinais e câmera executados; medições físicas de rumo/encoders e lançador; testes completos de pelo menos 3 execuções seguidas dentro de 29 s, trajetória legal, comportamento estável e resultado de pontuação registrado. Confirmar a estratégia para BLUE/RED e posições de partida antes de usar 5.1 em competição.

## Reversão
Nenhuma das versões 3.1, 4.0 ou 4.1 foi substituída. Todas as novas classes 4.2, 5.0, 5.1 e diagnósticos residem apenas na branch de revisão. Não houve merge em `main`.


---

## Atualização de evidência de compilação — 12/10/2026

**FTC SDK integrado: BUILD APROVADO.** O [GitHub Actions run #38321732887](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/actions/runs/38321732887) compilou conjuntamente **todas as classes Java de autônomo com e sem câmera**, incluindo esta versão 4.2 / 5.0 / 5.1, com `TeamCode:compileDebugJavaWithJavac`, `TeamCode:assembleDebug` e a mensagem **BUILD SUCCESSFUL in 2m 42s**.

O job de testes matemáticos do mesmo run terminou com **SUCCESS**. Os scripts tests/autonomo50_synthetic_tests.mjs, tests/autonomo51_cell_tests.mjs e tests/autonomo51_pid_model.mjs foram executados novamente em CI. Os cenários usam dados sintéticos, não capturas de AprilTags reais.

**Limite:** compilação do código no FTC SDK oficial obtido no CI NÃO significa montagem/calibração da C270, motores ou lançador validada fisicamente. Antes de instalar, conferir o projeto FTC SDK local, hardwareMap, sinais e campo. Consulta consolidada: [RELATORIO_GERAL_VALIDACAO_SOFTWARE.md](../RELATORIO_GERAL_VALIDACAO_SOFTWARE.md).
