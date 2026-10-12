# RELATÓRIO GERAL DE VALIDAÇÃO DE SOFTWARE — MECA TIGER FTC #32578

**Data:** 12/10/2026  
**Temporada:** BIOBUZZ 2026–2027  
**Escopo:** 12 versões históricas/ativas, das linhas sem câmera e com C270.  
**Objetivo:** apresentar o nível de evidência real para cada versão, preservar a rastreabilidade de código e fornecer uma base confiável para aprovação de ensaios no robô.

Leia com o [Manual Geral de Calibração e Testes](MANUAL_GERAL_CALIBRACAO_TESTES.md). Este relatório **não é um laudo de desempenho físico**.

## 1. Legenda de evidências — não misturar categorias

| Nível | O que realmente demonstra | O que NÃO demonstra |
| --- | --- | --- |
| E0 — Histórico de código | O conteúdo existe e seu hash pode ser conferido | Que compila ou move corretamente |
| E1 — Revisão estática | Estrutura, condições, sinais e limites foram inspecionados | Ausência de bugs ou conformidade com hardware |
| E2 — Teste automatizado matemático | Assertions verificadas por execução de script | Simulação física real |
| E3 — Simulação dinâmica | Comportamento dentro de hipóteses de modelo | Taxa real de êxito, precisão no piso |
| E4 — Compilação FTC SDK | Java e dependências compilados no FTC SDK usado no teste | Mecânica, câmera instalada ou estratégia correta |
| E5 — Teste físico | Dados coletados no robô montado e em condições registradas | Desempenho em todas as condições ou homologação permanente |

**Termos do relatório:** “confirmado” exige log e referência auditáveis. “Relatado” significa que consta em documentação histórica, mas a execução original pode não ser reproduzível aqui. “Pendente” não significa falha — significa ausência de prova correspondente. Nenhuma versão possui, até a revisão deste relatório, um **registro de teste físico do robô real** com identificação de ensaio, telemetria e aceite vinculados ao código exato.

## 2. Inventário de todas as versões e alterações

| Versão | Fonte auditável | Câmera | Mudança em relação à anterior | Classificação |
| --- | --- | --- | --- | --- |
| **2** | historico/Autonomo_2.txt | Não | Referência histórica da sequência/PID; contém classe antiga autonomo | E0; execução física não documentada |
| **3** | historico/Autonomo_3.txt | Não | Evolução registrada de controle e sequência; versão anterior preservada | E0; execução física não documentada |
| **3.1** | Autonomo/autonomo.java e historico/Autonomo_3_1.txt | Não | PID manual P/I/D, proteção contra saturação, filtro de derivada, watchdog de encoder, rampas, diagnósticos | **E4 confirmado em conjunto 3.1–3.5** |
| **3.2** | Autonomo/Autonomo32.java | Não | Distância concluída pela média dos dois encoders + giro final; derivada angular contínua no wrap; proteção temporal | **E4 confirmado** |
| **3.3** | Autonomo/Autonomo33.java | Não | Validação de parâmetros, soma temporal 24,30 s, limites de diferença de encoders (220 ticks) e desvio de yaw (35°) | **E4 confirmado** |
| **3.4** | Autonomo/Autonomo34.java | Não | Testes separados B (giro ±20°), X (350 mm) e Y (completo), confirmação A | **E4 confirmado** |
| **3.5** | Autonomo/Autonomo35.java | Não | D-pad cima testa lançador MI/MS sem tração; telemetria por etapa; confirmação por novo toque em A | **E4 confirmado; candidata à bancada** |
| **4.0** | Autonomo/Autonomo4.java | Sim | VisionPortal, C270 640 × 480, filtro de clusters e roll, alinhamento visual e fallback | **E4 confirmado**; simulações antigas nem todas verificáveis |
| **4.1** | Autonomo/Autonomo41.java | Sim | getFreshDetections, interrupção por falta de frames, derivada angular contínua, relação pelos dentes da caixa | **E2/E3 em CI + E4 confirmado** |
| **4.2** | Autonomo/Autonomo42.java | Sim | Watchdog de giro, timeouts mais compatíveis com 30 s, reserva de tempo para o lançador | **E2/E3 no conjunto + E4 confirmado** |
| **5.0** | Autonomo/Autonomo50.java | Sim | Aliança/lado selecionáveis no INIT, sem editar constantes a cada partida | **E2/E3 em CI + E4 confirmado** |
| **5.1** | Autonomo/Autonomo51.java | Sim | Confirmação A no INIT, busca de CELL pontuável dentro da aliança correta | **E2/E3 em CI + E4 confirmado; sem ensaio físico** |

As versões 2 e 3 estão em arquivos históricos; ambas usam classe Java chamada autonomo. **Não copiar versões antigas com classe homônima para o mesmo pacote ao mesmo tempo.** A 3.1 existente usa igualmente a classe autonomo. Os arquivos Java 3.2+ possuem classes distintas.

**Observação histórica de PID:** a versão 2 registrava constantes P/I/D; a versão 3 intermediária usava parâmetros de giro proporcionais/derivativos sem os ganhos integrais da 3.1; a **3.1** estabeleceu novamente PID manual completo com integrador limitado e filtragem da derivada. Não inferir que todas as 12 versões usam exatamente o mesmo controlador ou a mesma calibração.

O registro histórico tem 12 versões: 2, 3, 3.1, 3.2, 3.3, 3.4, 3.5, 4.0, 4.1, 4.2, 5.0 e 5.1. Cada versão 3.1–5.1 possui cópia de texto em historico/ com conteúdo equivalente ao Java versionado; os arquivos 2/3 são as próprias cópias históricas.

## 3. Proveniência dos arquivos — hashes dos blobs Git

| Versão | SHA de conteúdo Git (blob) |
| --- | --- |
| 2 | 65072b9b18a42438df625deca4722596126b006d |
| 3 | 91c6d4ebadcf974df24bd5a9eafeaf502c0c2bba |
| 3.1 | cccc44f9ddfd8b90b06fb826d35e21b19bdc4fcb |
| 3.2 | ac3d183421553b9adeba620e6299cdb1504baeb2 |
| 3.3 | 7103e0b86180def8967814960995637ea136e3c8 |
| 3.4 | 62228bf535c382505328a386d70cda85a67b8b2d |
| 3.5 | 37cc2ac051f014487d45702958e46f0bc4e3455b |
| 4.0 | d4a984f7d0f4b680ec1f16ab6d8426b325e2715b |
| 4.1 | df6c87f201687018c9138ce4eeb905a07eaa6378 |
| 4.2 | dfd441704ea442aa7776fbe8e64dea9220e16ecb |
| 5.0 | bffe2ce8e23a905a76301854300270a2c5232970 |
| 5.1 | 91081923a507cec0379b8c5a98d41e0817b4b76c |

**Observação:** SHA de blob identifica o conteúdo de um arquivo, não o commit inteiro. Alterações posteriores dos arquivos exigem atualizar esta tabela e registrar novo build. Os hashes foram conferidos nos arquivos remotos do repositório em 12/10/2026.

## 4. Evidência REAL confirmada — compilação da linha sem câmera

**GitHub Actions:** [execução #38319766443](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/actions/runs/38319766443)  
**Origem:** branch desenvolvimento/autonomo-sem-camera-3x-20261012, PR #3 ainda em rascunho na data da conferência.  
**Ambiente:** runner Linux do GitHub, Temurin JDK 17, Android SDK do runner e clone do repositório público oficial FIRST-Tech-Challenge/FtcRobotController.

| Etapa real | Resultado |
| --- | --- |
| Testes sintéticos com Node.js | **PASS**: 21 assertions, 5.000 cenários |
| Auditoria automática de versões e backups | **PASS**: 58 verificações |
| Ausência de VisionPortal/AprilTag na linha 3.x | **PASS** |
| Tarefa TeamCode:compileDebugJavaWithJavac | **PASS**, encontrada no log |
| Tarefa TeamCode:assembleDebug | **PASS**, encontrada no log |
| Mensagem final do Gradle | **BUILD SUCCESSFUL in 2m 16s** |

**As cinco classes Java efetivamente copiadas e compiladas foram 3.1, 3.2, 3.3, 3.4 e 3.5.** Seus hashes de conteúdo no commit compilado foram conferidos idênticos aos hashes acima. Isso confirma **compatibilidade de compilação nesse ambiente**, não instalação no Control Hub nem comportamento dos motores.

Distribuição dos 5.000 casos sintéticos com planta diferencial simplificada, não medição do robô:

| Cenário artificial | Execuções | Resposta do modelo |
| --- | ---: | --- |
| Sem falha simulada | 1.000 | Rotina completada em 1.000 |
| Sinal do yaw invertido | 1.000 | 1.000 abortadas no watchdog do primeiro giro |
| Motor direito travado | 1.000 | 1.000 abortadas por divergência dos encoders |
| Encoder direito congelado | 1.000 | 1.000 abortadas por divergência dos encoders |
| IMU inválido | 1.000 | 1.000 abortadas por leitura inválida |

Os scripts executam equações em JavaScript (não FTC SDK). A taxa de sucesso **não pode ser extrapolada** para a pista real.

**Tentativas anteriores de CI:** o build falhou duas vezes por infraestrutura de Android SDK (pacote legado “tools” e sdkmanager fora do PATH), **antes** de compilar Java. A terceira execução (#38319766443) chegou ao Gradle e aprovou. Ver [relatório específico do CI sem câmera](historico/RELATORIO_CI_SEM_CAMERA_3X.md).

## 5. Evidência de câmera — separar histórico de confirmação atual

**4.0 — registro histórico:** [validação 4.0](historico/VALIDACAO_Autonomo_4_0.md) relata estudos de giros, deslocamentos e visão e registra alterações mecânicas/temporais. Algumas contagens históricas de simulação não vieram acompanhadas aqui de um log independente do script que as produziu. Por rigor, **não classificar essas contagens como um teste reproduzido neste relatório**.

**4.1 — script e modelo registrados:** [validação 4.1](historico/VALIDACAO_Autonomo_4_1.md), testes/autonomo41_logic_tests.mjs (pasta tests/). O documento relata 7 assertions e 7.500 cenários de visão artificial, incluindo perdas de quadros e sinal invertido. **Esses números não são testes de frames reais da C270.**

**4.2 / 5.0 / 5.1 — modelos registrados:** [validação 4.2 a 5.1](historico/VALIDACAO_VERSOES_4_2_A_5_1.md) e scripts existentes na pasta tests/:
- tests/autonomo50_synthetic_tests.mjs — cronograma, sinais e giros simplificados;
- tests/autonomo51_cell_tests.mjs — filtragem de clusters e orientações artificiais;
- tests/autonomo51_pid_model.mjs — PID P/I/D discretizado, ruído e inércia simulados.

O documento histórico relata para 5.0: 22 assertions e 10.000 cenários; para 5.1, 22.656 verificações de escolha de CELL e 20.000 giros PID simulados. **Conferir logs de execução antes de tratar resultados antigos como evidência de CI.**

**Compilação REAL da câmera — CONFIRMADA:** o workflow complementar [.github/workflows/ftc-auditoria-todas-versoes.yml](.github/workflows/ftc-auditoria-todas-versoes.yml) executou com sucesso no [GitHub Actions #38321732887](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/actions/runs/38321732887). O job **FTC SDK Java - todas as 3.1-5.1** copiou todos os arquivos Java da pasta Autonomo (incluindo 4.0, 4.1, 4.2, 5.0, 5.1, DiagnosticoAprilTag e DiagnosticoSinais) para o clone do projeto oficial FIRST-Tech-Challenge/FtcRobotController e completou **TeamCode:compileDebugJavaWithJavac** e **TeamCode:assembleDebug** com **BUILD SUCCESSFUL in 2m 42s**. A compilação incluiu também as classes 3.1–3.5, sem conflitos de classe/importação. Isso comprova compatibilidade com **o checkout oficial do SDK utilizado no CI**, mas o workflow não fixa um SHA de upstream e não valida o SDK instalado no Control Hub da equipe.

**Execução REAL dos testes de software das duas linhas:** o job de testes do mesmo [run #38321732887](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/actions/runs/38321732887) também terminou com **SUCCESS** para todos os oito scripts Node. Foram reexecutados testes de 3.2/3.x, os sete checks matemáticos 4.1, 7.500 cenários sintéticos de visão, 22 assertions de 5.0, 10.000 listas de escolha de CELL da 5.1 e 20.000 giros com modelo PID P/I/D. A execução adicional [run #38321932005](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/actions/runs/38321932005) confirmou também o script de auditoria documental com **12 versões / 87 verificações** aprovadas. Todos estes testes continuam artificiais, **sem imagens reais da C270 ou acionamento de motores**.

**Distinção crítica:** o código 4.0 usa getDetections(), que pode reutilizar resultado antigo; a 4.1+ usa getFreshDetections() e limita a idade de comandos de visão. **Isso é uma melhoria de segurança, não uma calibração de câmera comprovada.**

## 6. Revisão de riscos por subsistema

| Subsistema | Risco observado/potencial | Bloqueio para aceite físico |
| --- | --- | --- |
| Direção de tração MD/ME | Sinal de giro diferente da hipótese Java | Teste passivo e giro curto com yaw físico |
| IMU | LEFT/UP pode não corresponder à montagem, NaN ou inversão | Teste da orientação e do sinal em bancada |
| Distância | 3.x assume 20:1; câmera 4.1+ assume ~18,9304:1 × transmissão externa | Medir transmissão e roda antes de comparar |
| Encoder | Um motor pode girar diferente por fixação, atrito, fiação | Avaliar ticks de ambas as rodas e avanço verdadeiro |
| PID | Kp/Ki/Kd simulados não significam resposta real estável | Ensaios curtos com métricas/timeout |
| Lançador | Ciclos temporizados e MS em potência fixa; não mede velocidade nem peças | Medir três peças efetivas e repetibilidade |
| AprilTag | fps e idade de frame, rótulos, roll e extrínsecos | DiagnosticoAprilTag com imagens reais |
| Alvo selecionado | CELL pontuável pode alternar, aliança/lado mal escolhidos | Ensaio em cada ponto de partida |
| Mira | Bearing zero da câmera pode não ser mira real do lançador | Calibração óptica vs. eixo de lançamento |
| Estratégia | Rota -50°, 2 m, +100° é fixa e pode não servir para todos os lados | Layout real/colisões/trajetória para cada posição |
| Tempo | 4.0–4.1 podem ultrapassar 29 s na soma; 3.3+ e 4.2+ são apenas orçamentos | Cronometrar etapas e disparos reais |
| Sem câmera | Não possui localização externa nem correção absoluta X/Y | Medir deriva e definir rota que tolere erro |
| Segurança | Timeout e STOP não equivalem a sensor de colisão | Área de ensaio, vigilância e interruptibilidade |

## 7. Matriz de aceite por versão

| Grupo de versões | Histórico | Testes de lógica | Build FTC | Teste físico | Estado |
| --- | --- | --- | --- | --- | --- |
| 2 e 3 | Arquivos de texto preservados | Não repetidos nesta auditoria | Não compiladas como classes atuais | Sem registro vinculado | **Arquivo histórico** |
| 3.1 | Arquivo original preservado | Referência do conjunto 3.x | **Aprovado** | Sem registro vinculado | Baseline compilável |
| 3.2 | Backup/documento individual | Scripts e relatórios | **Aprovado** | Sem registro vinculado | Candidata de bancada |
| 3.3 | Backup/documento individual | 21 assertions/5.000 cenários na auditoria 3.x | **Aprovado** | Sem registro vinculado | Candidata de bancada |
| 3.4 | Backup/documento individual | Auditoria e testes de modos | **Aprovado** | Sem registro vinculado | Testes de tração |
| **3.5** | Backup/documento individual | Auditoria e testes de modos | **Aprovado** | Sem registro vinculado | **Candidata preferida sem câmera** |
| 4.0 | Backup/documento individual | Registros históricos (resultados anteriores nem todos reproduzidos) | **Aprovado: CI #38321732887** | Sem registro vinculado | Histórico de visão |
| 4.1 | Backup/documento individual | Modelo/script 4.1 **executado no CI** | **Aprovado: CI #38321732887** | Sem registro vinculado | Revisão de visão |
| 4.2 | Backup/documento consolidado | Modelos de orçamento e regressões da linha executados no CI | **Aprovado: CI #38321732887** | Sem registro vinculado | Base temporal com câmera |
| 5.0 | Backup/documento consolidado | Scripts de escolha e tempo **executados no CI** | **Aprovado: CI #38321732887** | Sem registro vinculado | Interface no INIT |
| **5.1** | Backup/documento consolidado | Scripts de clusters e PID **executados no CI** | **Aprovado: CI #38321732887** | Sem registro vinculado | **Candidata com câmera, bancada pendente** |

**A coluna Build FTC para a câmera foi atualizada após verificar o log real do job #115051141045 (run #38321732887), com TeamCode:assembleDebug e BUILD SUCCESSFUL.** Continuam pendentes a calibração e os testes físicos de câmera, tração e mecanismos. É essencial registrar o commit exato quando o código for instalado no robô.

## 8. Reprodução e auditoria contínua

~~~bash
# Testes sem câmera
node tests/autonomo32_logic_tests.mjs
node tests/autonomo32_travel_model.mjs
node tests/autonomo_sem_camera_3x.mjs
node tests/auditoria_autonomo_3x.mjs

# Testes com câmera — dados sintéticos, sem frames reais
node tests/autonomo41_logic_tests.mjs
node tests/autonomo50_synthetic_tests.mjs
node tests/autonomo51_cell_tests.mjs
node tests/autonomo51_pid_model.mjs

# Build: projeto completo FTC SDK integrado
./gradlew :TeamCode:assembleDebug
~~~

Referência de build confirmada sem câmera: [run #38319766443](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/actions/runs/38319766443).  
Workflow conjunto: .github/workflows/ftc-auditoria-todas-versoes.yml.  
Arquivos individuais: historico/VALIDACAO_Autonomo_3_2.md, _3_3.md, _3_4.md, _3_5.md, _4_0.md, _4_1.md e historico/VALIDACAO_VERSOES_4_2_A_5_1.md.

## 9. Processo de liberação e comparação em campo

**Não existe ainda uma versão “homologada de competição” demonstrada por estes registros.** Recomenda-se fazer ensaios com **3.5** (sem câmera) e **5.1** (com câmera), após compilar ambas e documentar instalação, em fases:

1. Conferir configuração, polaridade e IMU passivamente.
2. Girar +20°/−20° e avançar 350 mm no modo seguro de bancada.
3. Medir transmissão e deslocamento; calibrar distância e orientação por medições.
4. Ensaiar MI/MS isoladamente com proteção e contar peças reais.
5. Ensaiar C270 isolada, validar clusters, bearing, roll e latência.
6. Verificar rota em cada posição inicial, tempo de 30 s e falhas.
7. Comparar ambas as variantes nas mesmas condições e registrar **todas** as tentativas.
8. Só promover release com código/commit identificados, build correspondente, parâmetros aferidos, relatório físico preenchido e aprovação da equipe.

Preencher [MODELO_REGISTRO_ENSAIO_AUTONOMO.md](MODELO_REGISTRO_ENSAIO_AUTONOMO.md) a cada tentativa, registrar a decisão de aceite e criar **nova versão** para ajustes. Não reescrever hashes ou resultados históricos.

---

**Fontes primárias:** código e relatórios do repositório, [AGENTS.md](AGENTS.md), [skill ftc-engineer](.agents/skills/ftc-engineer/SKILL.md), [especificação de validação](.agents/skills/ftc-engineer/references/validation.md); [FTC SDK](https://github.com/FIRST-Tech-Challenge/FtcRobotController).

**Conclusão atualizada:** código e histórico das 12 versões organizados; **compilação real de 3.1–3.5 e 4.0–5.1 confirmada no CI**, testes matemáticos de ambas as linhas aprovados e auditoria documental 12/12 aprovada. **Nenhuma versão foi homologada fisicamente; a C270 real, sensores, motores e lançamento ainda exigem medições.**