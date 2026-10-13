# MANUAL GERAL DE CALIBRAÇÃO E TESTES — MECA TIGER FTC #32578

**Temporada:** BIOBUZZ 2026–2027  
**Revisão:** 12/10/2026  
**Abrangência:** versões 2, 3, 3.1–3.5 (sem câmera) e 4.0–4.2, 5.0–5.1 (com câmera).

Este é um manual de campo para equipe de programação, pilotos, elétrica, mecânica e mentores. O objetivo é transformar medições em decisões de código sem perder o histórico. **Não é um laudo de testes físicos realizados.**

Documentos relacionados: [Relatório geral de validação](RELATORIO_GERAL_VALIDACAO_SOFTWARE.md), [Manual detalhado 3.x sem câmera](GUIA_AUTONOMO_SEM_CAMERA.md), [Ficha preenchível de ensaio](MODELO_REGISTRO_ENSAIO_AUTONOMO.md), [Históricos individuais](historico/).

## 1. Mapa das versões

| Versão | Família | O que se deve testar |
| --- | --- | --- |
| 2 | Sem câmera, histórica | Referência do PID e da rotina antiga, não OpMode candidata |
| 3 | Sem câmera, histórica | Evolução de controle e tarefas, comparação com 3.1 |
| 3.1 | Sem câmera | Base oficial preservada |
| 3.2 | Sem câmera | Derivada angular, distância por média, watchdog, reserva de lançamento |
| 3.3 | Sem câmera | Validação prévia, desvio máximo de yaw e encoders, timeouts |
| 3.4 | Sem câmera | Teste isolado de giro de +20° e −20°, teste de 350 mm ou rotina completa |
| **3.5** | **Sem câmera** | Além de 3.4, teste isolado MI/MS e tempo de cada etapa |
| 4.0 | Com C270 | Alinhamento inicial via AprilTag Cluster, fallbacks e parâmetros da câmera |
| 4.1 | Com C270 | Quadros novos, perda de alvo, derivada corrigida, razão de dentes |
| 4.2 | Com C270 | Watchdog de rumo e reserva de tempo para visão e lançamento |
| 5.0 | Com C270 | Escolha de RED/BLUE e SCORING/AUDIENCE no INIT |
| **5.1** | **Com C270** | Confirmação de estratégia e seleção da CELL pontuável da própria aliança |

Códigos de 3.1–5.1: pasta TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/. Versões 2 e 3: apenas arquivos de histórico em historico/. As versões 3.3–3.5 estão na linha de desenvolvimento sem câmera; a linha 4.x–5.x já existe na main. **Não sobrescrever versões anteriores para calibrar a próxima.**

## 2. Inventário de hardware e riscos

| Dispositivo | Configuração do código | Pendente de aferição |
| --- | --- | --- |
| Tração | Diferencial MD (direito), ME (esquerdo), sem mecanum | Sinal físico, transmissão, atrito e estado dos motores |
| Direções | MD REVERSE, ME FORWARD | Confirmar que potência positiva faz o robô avançar |
| IMU | Nome imu, orientação LEFT/UP | Confirmar instalação e yaw positivo em giro anti-horário |
| Encoders | 28 ticks/rev do motor | Confirmar motor, contagem e rotação positiva |
| Rodas | Diâmetro nominal 90 mm | Diâmetro efetivo e derrapagem sob carga |
| MI e MS | Coleta/alimentação e lançador | Rotação, travamento, RPM e tempo necessário |
| Câmera | Logitech C270, Webcam 1, 640 × 480 | USB, alimentação, ângulo de montagem, FPS, exposição |
| Redução 3.x | 20,0:1 | Hipótese do código; NÃO é medição |
| Redução 4.0 | 3,61 × 5,23 = 18,8803:1 | Confirmar cartuchos/transmissão |
| Redução 4.1–5.1 | (76/21) × (68/13) × transmissão externa | Confirmar combinação realmente instalada |

**Atenção:** a transmissão real do robô precisa ser única para ambos os programas. Os valores atuais de 20:1 e 18,93:1 são diferentes; sem equalizar mecanicamente os parâmetros, comparar distância entre as versões não será um experimento justo.

## 3. Segurança e preparação obrigatórias

1. Identificar técnico/mentor responsável, operador do STOP, estado da bateria e condições do piso.
2. Isolar a área e checar fixação das rodas, motores, Control Hub, câmera e cabeamento.
3. Confirmar que a configuração de hardware foi salva e selecionada no Driver Station.
4. Não deixar ninguém na frente do lançador; usar proteção e retenção de peças antes dos modos MI/MS.
5. Para sinais, começar pelo OpMode DiagnosticoSinais **sem acionamento de motores**; para visão, usar DiagnosticoAprilTag **sem movimento motorizado**.
6. Quando o sentido de giro ou do encoder estiver errado, **não executar** automaticamente a sequência: STOP, registrar a causa e corrigir em versão nova.
7. Não tratar watchdog, timeout ou fallback como garantia de ausência de colisão.

## 4. Etapas de calibração, na ordem recomendada

### Etapa A — Configuração, sinais e referência do IMU

**Versões aplicáveis:** todas, especialmente 3.5 e 5.1.

- Com DiagnosticoSinais, movimentar MANUALMENTE o chassi no sentido anti-horário e anotar se o yaw cresce; movimentar ambas as rodas à mão no sentido de avanço e anotar os ticks MD e ME. A telemetria e a orientação LEFT/UP devem ser consistentes.
- Fazer três leituras de yaw em repouso, anotando oscilação e eventual descontinuidade angular próxima a ±180°. Verificar se as leituras permanecem finitas.
- Corrigir polaridade/instalação erradas **antes** de ajustar Kp/Ki/Kd. Um motor invertido não é corrigido de forma segura aumentando ganhos.

**Registro mínimo:** fotografia do Hub, yaw em rotação manual, ticks MD/ME e nomes usados na configuração. **Critério:** os sentidos concordam com a convenção usada pelo Java.

### Etapa B — Giros curtos e ajuste do PID

**Versão prática:** Autonomo 3.5 - Bancada, modo **B = giro +20° e −20°**. Durante INIT: escolher B, soltar A se estiver pressionado, apertar A para confirmar e depois START.

- Executar com área livre e STOP acessível. Este modo **realmente energiza os motores**, e a potência máxima de giro configurada é 0,50.
- Registrar direção observada, yaw/ângulo real, erro final, oscilações e tempo até a parada. Repetir sob condições comparáveis; só avançar para ±50°/100° após passar no curto.
- Ajuste orientado a medição: conferir primeiro direção e potência mínima para vencer atrito; depois **Kp** (resposta), **Kd** (amortecimento) e, apenas se necessário, **Ki** (erro residual). Alterar um parâmetro por rodada.
- Uma tentativa que estoura timeout ou para no watchdog é **falha registrada**, não um resultado aproveitável para “calibrar por média”.

**Critério:** tolerância angular e repetibilidade definidas pela equipe ANTES do ensaio, sem sinal de divergência do IMU.

### Etapa C — Redução, roda, encoder e trajetória curta

Equação usada pelas versões:

\[
\mathrm{ticks/mm} =
\frac{\mathrm{ticks/rev\ do\ motor}\times G_{\mathrm{total}}}
{\pi D_{\mathrm{efetivo\ (mm)}}}
\times F_{\mathrm{calibração}}
\]

Para diâmetro assumido de 90 mm e fator 1,0:

| Versão | Redução presumida | Ticks/mm | Alvo 350 mm | Alvo 2.000 mm |
| --- | ---: | ---: | ---: | ---: |
| 3.1–3.5 | 20,0 | 1,980595 | 693 | 3.961 |
| 4.0 | 18,8803 | 1,869711 | 654 | 3.739 |
| 4.1–5.1 | 18,930403 × transmissão externa | 1,874673 × transmissão externa | ~656* | ~3.749* |

*Com transmissão externa = 1,0; números **teóricos**, não aferidos.*

**Procedimento:**

1. Inspecionar toda a transmissão: motor, cartuchos, correntes/engrenagens intermediárias e roda. Registrar a relação total efetiva.
2. Marcar no chão uma linha reta, no mesmo piso da competição. Usar Autonomo 3.5, **modo X + A + START** (350 mm, potência-base 0,22). A correção de rumo pode alterar a potência individual de MD/ME.
3. Medir distância percorrida pelo **centro do robô**, desvio lateral e yaw final; anotar ambos os encoders. Repetir ao menos três vezes.
4. Após conferir sinais e redução, testar 500 mm ou 1.000 mm. Somente depois executar 2.000 mm no espaço apropriado.
5. Se a geometria já foi aferida e o desvio é sistemático, ajustar só o fator de calibração: **F_novo = F_atual × distância_comandada / distância_real**. Exemplo: pediu 500 mm, andou 470 mm: multiplicar o fator por **1,06383**. Revalidar em mais de uma distância.
6. **Não** mascarar encoder defeituoso, motor solto ou patinagem apenas com um fator global. Se a diferença entre lados crescer, resolver a causa antes do ajuste de PID.

**Critério:** erro linear/lateral dentro da tolerância definida, sem divergência anormal dos ticks e com repetibilidade.

### Etapa D — Mecanismos: MI, MS e quantidade REAL de peças

**Versão prática:** Autonomo 3.5, **D-pad CIMA → soltar A → A → START**, somente em área de disparo protegida. Este modo interrompe tração antes da sequência, mas **pode lançar peças**.

- Software atual: MS em potência 0,70, aquecimento **0,80 s**; MI em potência 0,60, alimentação **0,45 s** por ciclo, intervalo **0,15 s**; três ciclos totalizam **2,45 s** nominais.
- Registrar se **cada peça saiu de verdade**, estabilidade da saída, velocidade/tempo, travamentos, recuo do mecanismo, variação de bateria e temperatura.
- Não confundir “três ciclos de alimentação comandados” com três lançamentos bem-sucedidos: o programa não implementa confirmação de peça nem realimentação de RPM.
- Se for necessário evoluir para sensor de peça ou controle de velocidade do lançador, criar nova versão e primeiro testar o mecanismo isoladamente.

### Etapa E — Câmera C270 e AprilTag Clusters

**Somente para 4.0–5.1. Primeiro validar A–D e o build das versões com câmera.**

1. Executar DiagnosticoAprilTag e confirmar **Webcam 1**, resolução 640 × 480 e estado VisionPortal **STREAMING**.
2. Conferir em imagens reais os nomes: **RED SCORING, RED AUDIENCE, BLUE SCORING, BLUE AUDIENCE**; observar ftcPose.range (em **polegadas**), bearing (graus), yaw e roll. Não misturar range em polegadas com distância de rodagem em mm.
3. Verificar a orientação da CELL: o código considera pontuável quando **|roll| < 90°**. Confirmar com o arranjo oficial e registrar casos com a CELL invertida.
4. Girar o robô **manualmente** alguns graus e registrar a mudança do bearing, para determinar o sinal do controle visual antes de liberar giro motorizado assistido.
5. Medir quadros novos, latência e perda de alvo. A 4.1+ usa **getFreshDetections()** e limites de **0,08 s** (idade do comando), **0,18 s** (validade da confirmação) e **0,35 s** (perda de alvo). FPS insuficiente pode disparar as proteções.
6. Medir e ajustar o **OFFSET_BEARING_CAMERA_GRAUS** relativo ao eixo de lançamento real: câmera alinhada visualmente ao alvo não garante que o lançador esteja apontado para ele. Avaliar paralaxe e distância, sempre sem disparo durante a primeira medição.
7. Verificar o comportamento com câmera desligada, sem detecção, alvo trocado ou invertido e aliança oposta à escolhida. **Fallback IMU = sem correção visual confirmada**; não significa a mesma precisão do alinhamento com câmera.
8. Na **5.1**, escolher RED/BLUE pelo D-pad ESQUERDA/DIREITA, lado de preferência SCORING/AUDIENCE pelo D-pad CIMA/BAIXO e confirmar com A no INIT. A busca dinâmica seleciona CELL pontuável da **aliança escolhida**, mas **não espelha automaticamente a rota -50°, 2 m, +100°**. Testar a geometria individualmente em cada posição inicial.

**Limite de projeto:** bearing de AprilTag sozinho não fornece coordenadas globais confiáveis, detecção de obstáculos, controle de distância ideal de lançamento ou compensação balística.

### Etapa F — Execução integrada e comparação COM × SEM câmera

**Só após as etapas anteriores.** Usar mesma posição e rumo inicial, piso, quantidade de peças, estado comparável de bateria, transmissão REAL consistente e ângulos da estratégia compatíveis com o campo.

| Medição por tentativa | 3.5 sem câmera | 5.1 com câmera |
| --- | --- | --- |
| Tempo total e por etapa | Telemetria de etapa | Cronômetro + telemetria disponível |
| Giro e erro angular | IMU e medição externa | IMU, medição externa e bearing |
| Distância do centro do robô | Fita métrica + ticks | Fita métrica + ticks |
| Deriva lateral | Marca no piso | Marca no piso |
| Detecção/estado do cluster | Não se aplica | Nome, roll, bearing, perda, STREAMING |
| Peças realmente lançadas | Contagem observada | Contagem observada |
| Pontuação real | Observação do campo | Observação do campo |
| Falha de sensor/timeout/STOP | Motivo e etapa | Motivo, etapa e fallback visual |

Definir tolerâncias previamente, guardar as tentativas falhas, repetir em condições comparáveis e reportar taxas reais **somente com amostra física**.

## 5. Tempo de autônomo e limites das versões

- **3.3–3.5:** soma teórica dos limites, incluindo margem: **5 + 10 + 1,5 + 5 + 2,45 + 0,35 = 24,30 s**; janela de execução de 29 s (1 s de reserva do AUTO de 30 s).
- **4.0–4.1:** pior soma de limites registrada para 4.1 = **34,05 s**, acima da janela segura; não confundir limite global com garantia de concluir os disparos.
- **4.2–5.1:** soma planejada **4,5 + 9,5 + 1,5 + 4,5 + 2,5 + 1,6 + 2,45 + 0,35 = 26,90 s**, dentro de 29 s de operação nominal. Atrasos reais e falhas mecânicas permanecem possíveis.
- Um **abort por timeout** faz a rotina parar; não garante que as peças serão lançadas depois. Não continuar cegamente uma manobra quando falhar a localização.

## 6. Como reproduzir testes e compilar

A partir de um checkout que contenha as duas linhas:

~~~bash
node tests/autonomo32_logic_tests.mjs
node tests/autonomo32_travel_model.mjs
node tests/autonomo_sem_camera_3x.mjs
node tests/auditoria_autonomo_3x.mjs

node tests/autonomo41_logic_tests.mjs
node tests/autonomo50_synthetic_tests.mjs
node tests/autonomo51_cell_tests.mjs
node tests/autonomo51_pid_model.mjs
~~~

Os testes JavaScript são **modelos de lógica e sensores artificiais**, não executam FTC SDK ou motores.

Para compilação real, integrar os Java à árvore completa do [FTC SDK oficial](https://github.com/FIRST-Tech-Challenge/FtcRobotController) e executar **./gradlew :TeamCode:assembleDebug** (Linux) ou **gradlew.bat :TeamCode:assembleDebug** (Windows).

- **Build das versões 3.1–3.5 confirmado:** [GitHub Actions run #38319766443](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/actions/runs/38319766443). Isso não comprova testes físicos.
- **Build 4.0–5.1 CONFIRMADO:** [GitHub Actions run #38321732887](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/actions/runs/38321732887): todas as classes Java com e sem câmera compilaram juntas no TeamCode oficial (BUILD SUCCESSFUL em 2m 42s). **Não equivale a testá-las no robô nem valida C270 física.**

## 7. Padronização do registro e controle de versões

Preencher a [ficha de ensaio](MODELO_REGISTRO_ENSAIO_AUTONOMO.md) **em cada tentativa**, inclusive em falhas. Anexar telemetria, fotos, ângulos medidos, tempo, valores reais das transmissões e vídeos de campo.

Ao corrigir código: **não editar históricos**; criar nova versão/commit, descrever mudança antiga/nova, justificativa medida, arquivo afetado, resultados de testes, limitações e procedimento de reversão. Usar o CHANGELOG e relatórios existentes como referência.

---

**Referências de engenharia:** [.agents/skills/ftc-engineer/SKILL.md](.agents/skills/ftc-engineer/SKILL.md), [hardware](.agents/skills/ftc-engineer/references/hardware.md), [validação](.agents/skills/ftc-engineer/references/validation.md); [documentação FTC](https://ftc-docs.firstinspires.org/), [REV UltraPlanetary](https://docs.revrobotics.com/rev-crossover-products/ultraplanetary/cartridge-details).