# Mequinha FTC #32578 — CATÁLOGO AUDITADO DOS 8 ARQUIVOS AUTÔNOMOS

Data: 03/11/2026. Base: fontes Java consultadas diretamente na branch principal do repositório.

> **Contagem exata:** há **7 estratégias numeradas** (L1-01, L1-02, L1-03, L1-04, L1-05, L2-01, L2-02) e **8 arquivos de referência**, porque L1-04 possui as versões v1 e v2. O legado autônomo 5.3 agressivo é uma referência histórica separada, fora desta contagem. L2-03 foi excluído por decisão do operador. Não confundir número de arquivos com número de estratégias validadas.

## 1. Os oito arquivos com objetivo e estado real

| Nº | Código | Trajeto pretendido | Status |
|---|---|---|---|
| 1 | [L1-01](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/blob/main/historico/meca_sim_l1_2026_10_29/AutonomoL101V11.java) | L1 (4) → PARK | Concluído segundo operador no Meca Sim. ARQUIVO RECONSTRUÍDO do diálogo: não é cópia byte a byte certificada. |
| 2 | [L1-02](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/blob/main/historico/meca_sim_l1_2026_10_29/AutonomoL102GardenParkRPM.java) | L1 (4) → Garden → PARK | Concluído segundo operador no Meca Sim; falta validação física. |
| 3 | [L1-03](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/blob/main/historico/meca_sim_l1_2026_10_29/AutonomoL103GardenGuiadoPark4L2RPM.java) | L1 (4) → Garden → L2 (4) → PARK | Concluído segundo operador no Meca Sim; falta validação física. |
| 4 | [L1-04 v1](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/blob/main/historico/meca_sim_l1_2026_10_29/AutonomoL104GardenL2FlowerParkRPM.java) | L1 (4) → Garden → L2 (4) → Flower mal posicionada → tiro extra → PARK | Experimental NÃO aprovado. Y_FLOWER_COLETA=0.278 é erro de lado do campo; NÃO reutilizar a geometria. |
| 5 | [L1-04 v2](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/blob/main/historico/meca_sim_l1_2026_10_29/AutonomoL104FlowerSuperiorRPMv2.java) | L1 (4) → Garden → L2 (4) → Flower superior → L2 (até 4) → PARK | Experimental NÃO aprovado; corrige a localização da Flower, mas ainda pode omitir terceira série por tempo. |
| 6 | [L1-05 v17](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/blob/main/historico/meca_sim_l1_2026_10_29/AutonomoL105DoisTipsParadaL2RPMv17.java) | L1 (4) → Garden → L2 (4) → Flower → L2 (até 4) → PARAR EM L2 | Compilado com stubs, sem validação integral demonstrada de 12 ejeções/TIPs; sem PARK. |
| 7 | [L2-01 v1](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/blob/main/historico/meca_sim_l2_2026_11_03/AutonomoL201L2QuatroParkRPMv1.java) | L2 (4) → PARK | Experimento compilado, sem execução integral comprovada; spawn de L2 provisório. |
| 8 | [L2-02 v2](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/blob/main/historico/meca_sim_l2_2026_11_03/AutonomoL202Flower8PollenSemCameraRPMv2.java) | L2 (4) → Flower → L2 (até 4) → PARK condicional | Experimento compilado, sem execução integral comprovada; SEM câmera. |

**Omissão corrigida:** L1-04 **não havia sido apagado**. Ficou fora de resumo anterior por ser experimental, mas essa omissão prejudicou a rastreabilidade. **v1 tem Y_FLOWER_COLETA=0.278 (lado errado), v2 corrige para Y_FLOWER_COLETA=3.398**; v2 mantém terceiros tiros condicionais ao prazo. Nenhuma das duas versões equivale a execução validada. No L1-01, o arquivo GitHub é uma reconstrução do diálogo, não garantia de integridade do aprovado original.

## 2. Componentes e convenções comuns (todos os oito)

| Item | Como aparece nos códigos | Atenção na calibração |
|---|---|---|
| Tração diferencial 2WD | ME (esquerda), MD (direita) | MD em REVERSE; ambos RUN_WITHOUT_ENCODER + BRAKE; não existe strafe |
| Coleta/alimentação | MI (intake) | Power 1.0 durante coleta e/ou feed, dependendo do posto; com MS ligado pode ejetar no Meca Sim |
| Lançador | MS (shooter) | Base 0.60; encoder próprio, NÃO usar constantes de ticks da roda como se fossem do MS |
| Servo | SA (alimentador) | Fechado 0.20; aberto 0.70; tempo aberto não é igual à posição |
| Orientação | imu | REV hub orientação LOGO RIGHT / USB UP; yaw mapa 0=+Y, +90=-X; L1 resetYaw e L2 offset |
| Encoders | ME e MD | Rodas Ø90mm, 560 ticks/volta **do conjunto roda/redução**, distância = Δticks × π × 0.09/560 |
| Cinemática | andar/girar/segmento, Bézier nos mais novos | IMU + encoders; risco de derrapagem e colisão mesmo com geometria teórica correta |
| Tempo | ElapsedTime, LIM_GLOBAL/LIMITE_TOTAL | Limite do código não é promessa de concluir antes dos 30 s |
| Segurança | watchdog, limitar potência Range.clip, parar em finally, checagem de pose | Nem todos os códigos têm os mesmos intertravamentos; não assumir paridade |
| Visão | Nenhum destes oito códigos selecionados importa API de câmera | Módulo L2-01 v2 com AprilTags é **arquivo separado arquivado**, fora do uso atual |
| Evidência de lançamento | Contador de pulsos MI/SA | Não comprova POLLEN ejetado, HIVE acertado ou TIP oficial |

## 3. Inventário EXAUSTIVO das constantes declaradas em cada fonte Java

Valores transcritos das declarações dos arquivos. **Mesma constante com valores diferentes é intencional**: não nivelar tudo ao extrair classes sem teste A/B. Para mudar um parâmetro, editar o perfil correspondente, nunca mudar oito rotas de uma só vez.

### L1-01 — 1135 linhas, OpMode `MECA L1-01 V1.1 4 POLLEN PARK`

Fonte: [Java exato](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/blob/main/historico/meca_sim_l1_2026_10_29/AutonomoL101V11.java) · Blob Git SHA `dffdfe89d79aca0a3d53421392a2eed258b39a77`.

| Constante original | Valor ou expressão original | Para que serve |
|---|---|---|
| `TICKS_M` | `560.0 / (Math.PI * 0.090)` | Conversão encoder/distance da tração (ticks por metro). |
| `X_START` | `1.678` | X inicial assumido no mapa; não detectado fisicamente. |
| `Y_START` | `0.399` | Y inicial assumido no mapa; não detectado fisicamente. |
| `X_CORREDOR_PARK` | `0.550` | Coordenada X do corredor de PARK. |
| `Y_ENTRADA_CORREDOR` | `0.550` | Coordenada Y da entrada do corredor. |
| `X_PARK` | `0.350` | Centro X do PARK pretendido. |
| `Y_PARK` | `2.820` | Centro Y do PARK pretendido. |
| `LIM_GLOBAL` | `29.3` | Tempo global interno do autônomo, segundos. |
| `TOL_GRAUS` | `1.5` | Tolerância do giro estacionário, graus. |
| `MS_SHOT` | `0.60` | Power base do shooter na simulação. |
| `MS_POWER_MAX` | `0.72` | Limite superior de power do shooter. |
| `MI_INTAKE` | `1.00` | Power do intake na coleta. |
| `MI_FEED` | `1.00` | Power do intake ao alimentar. |
| `SA_FECHADO` | `0.20` | Posição normalizada do servo fechado. |
| `SA_ABERTO` | `0.70` | Posição normalizada do servo aberto. |
| `TICKS_POR_VOLTA_MS` | `560.0` | Ticks/revolução do encoder do shooter: APENAS parâmetro de Meca Sim. |
| `RPM_ALVO_MS_SIM` | `127.0` | RPM alvo Meca Sim; NÃO valor físico medido. |
| `RPM_MIN_MS` | `RPM_ALVO_MS_SIM * 0.88` | Limite RPM mínimo calculado da faixa. |
| `RPM_MAX_MS` | `RPM_ALVO_MS_SIM * 1.10` | Limite RPM máximo calculado da faixa. |
| `KP_RPM_MS` | `0.0015` | Ganho proporcional de correção RPM. |
| `JANELA_RPM` | `0.080` | Intervalo entre leituras RPM, segundos. |
| `AMOSTRAS_RPM_ESTAVEL` | `3` | Número de amostras consecutivas válidas. |
| `TIMEOUT_ENCODER_MS` | `0.60` | Tempo para detectar ausência de mudança do encoder shooter, segundos. |
| `INTERVALO_TIROS` | `0.82` | Tempo mínimo entre inícios de tiros, segundos. |
| `PULSO_ALIMENTADOR` | `0.14` | Tempo do servo na posição aberta para tiros normais. |
| `POLLENS_POR_POSTO` | `4` | Número desejado de pulsos por posição. |
| `ARENA` | `3.660` | Comprimento assumido do lado da arena, metros. |
| `CHASSI` | `0.450` | Lado do quadrado do chassi, metros. |

**Métodos declarados na fonte:** `runOpMode()`, `dispararQuatro()`, `aguardarRpmEIntervalo()`, `esperarAte()`, `segmento()`, `deslocarSemGirar()`, `girar()`, `andar()`, `atualizarOdom()`, `prazoEsgotado()`, `yaw()`, `norm()`, `parar()`, `falha()`, `telemetria()`.

### L1-02 — 1363 linhas, OpMode `TESTE L1-02 4 L1 GARDEN PARK RPM`

Fonte: [Java exato](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/blob/main/historico/meca_sim_l1_2026_10_29/AutonomoL102GardenParkRPM.java) · Blob Git SHA `812938f2dbbe51f2a321bcdbd0117c7c3cf047b1`.

| Constante original | Valor ou expressão original | Para que serve |
|---|---|---|
| `TICKS_M` | `560.0 / (Math.PI * 0.090)` | Conversão encoder/distance da tração (ticks por metro). |
| `X_START` | `1.530` | X inicial assumido no mapa; não detectado fisicamente. |
| `Y_START` | `0.255` | Y inicial assumido no mapa; não detectado fisicamente. |
| `X_CORREDOR_PARK` | `0.550` | Coordenada X do corredor de PARK. |
| `Y_ENTRADA_CORREDOR` | `0.550` | Coordenada Y da entrada do corredor. |
| `X_PARK` | `0.255` | Centro X do PARK pretendido. |
| `Y_PARK` | `2.380` | Centro Y do PARK pretendido. |
| `Y_PREPARACAO_GARDEN` | `0.450` | Linha de preparação antes da aproximação ao Garden. |
| `Y_GARDEN_COLETA` | `0.262` | Y do centro do chassi ao coletar no Garden. |
| `X_GARDEN_FIM` | `0.285` | X final da varredura Garden. |
| `X_GARDEN_RETORNO` | `1.420` | X ao terminar o recuo Garden. |
| `COMPRIMENTO_RAMPA_GARDEN` | `1.050` | Comprimento geométrico de transição de Garden. |
| `FOLGA_MIN_GARDEN` | `0.012` | Folga mínima calculada de parede no Garden. |
| `LIM_GLOBAL` | `29.3` | Tempo global interno do autônomo, segundos. |
| `TOL_GRAUS` | `1.5` | Tolerância do giro estacionário, graus. |
| `MS_SHOT` | `0.60` | Power base do shooter na simulação. |
| `MS_POWER_MAX` | `0.72` | Limite superior de power do shooter. |
| `MI_INTAKE` | `1.00` | Power do intake na coleta. |
| `MI_FEED` | `1.00` | Power do intake ao alimentar. |
| `SA_FECHADO` | `0.20` | Posição normalizada do servo fechado. |
| `SA_ABERTO` | `0.70` | Posição normalizada do servo aberto. |
| `TICKS_POR_VOLTA_MS` | `560.0` | Ticks/revolução do encoder do shooter: APENAS parâmetro de Meca Sim. |
| `RPM_ALVO_MS_SIM` | `127.0` | RPM alvo Meca Sim; NÃO valor físico medido. |
| `RPM_MIN_MS` | `RPM_ALVO_MS_SIM * 0.88` | Limite RPM mínimo calculado da faixa. |
| `RPM_MAX_MS` | `RPM_ALVO_MS_SIM * 1.10` | Limite RPM máximo calculado da faixa. |
| `KP_RPM_MS` | `0.0015` | Ganho proporcional de correção RPM. |
| `JANELA_RPM` | `0.080` | Intervalo entre leituras RPM, segundos. |
| `AMOSTRAS_RPM_ESTAVEL` | `3` | Número de amostras consecutivas válidas. |
| `TIMEOUT_ENCODER_MS` | `0.60` | Tempo para detectar ausência de mudança do encoder shooter, segundos. |
| `INTERVALO_TIROS` | `0.82` | Tempo mínimo entre inícios de tiros, segundos. |
| `PULSO_ALIMENTADOR` | `0.14` | Tempo do servo na posição aberta para tiros normais. |
| `POLLENS_POR_POSTO` | `4` | Número desejado de pulsos por posição. |
| `ARENA` | `3.660` | Comprimento assumido do lado da arena, metros. |
| `CHASSI` | `0.450` | Lado do quadrado do chassi, metros. |

**Métodos declarados na fonte:** `runOpMode()`, `dispararQuatro()`, `aguardarRpmEIntervalo()`, `esperarAte()`, `segmento()`, `deslocarSemGirar()`, `linhaGardenY()`, `folgaInferior()`, `folgaEsquerda()`, `percorrerGardenGuiado()`, `girar()`, `andar()`, `atualizarOdom()`, `prazoEsgotado()`, `yaw()`, `norm()`, `parar()`, `falha()`, `telemetria()`.

### L1-03 — 1655 linhas, OpMode `TESTE L1-03 Garden PARK L2 4 tiros`

Fonte: [Java exato](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/blob/main/historico/meca_sim_l1_2026_10_29/AutonomoL103GardenGuiadoPark4L2RPM.java) · Blob Git SHA `963a3074d07450dc5296e3ec9545795868b37c71`.

| Constante original | Valor ou expressão original | Para que serve |
|---|---|---|
| `TICKS_M` | `560.0 / (Math.PI * 0.090)` | Conversão encoder/distance da tração (ticks por metro). |
| `X_START` | `1.530` | X inicial assumido no mapa; não detectado fisicamente. |
| `Y_START` | `0.255` | Y inicial assumido no mapa; não detectado fisicamente. |
| `X_PRE_PARK` | `0.520` | Coordenada X de preparação do PARK. |
| `Y_PRE_PARK` | `2.380` | Coordenada Y de preparação do PARK. |
| `X_PARK` | `0.255` | Centro X do PARK pretendido. |
| `Y_PARK` | `2.380` | Centro Y do PARK pretendido. |
| `Y_PREPARACAO_GARDEN` | `0.450` | Linha de preparação antes da aproximação ao Garden. |
| `Y_GARDEN_COLETA` | `0.262` | Y do centro do chassi ao coletar no Garden. |
| `X_GARDEN_FIM` | `0.285` | X final da varredura Garden. |
| `X_GARDEN_RETORNO` | `1.420` | X ao terminar o recuo Garden. |
| `COMPRIMENTO_RAMPA_GARDEN` | `1.050` | Comprimento geométrico de transição de Garden. |
| `FOLGA_MIN_GARDEN` | `0.012` | Folga mínima calculada de parede no Garden. |
| `LIM_GLOBAL` | `29.3` | Tempo global interno do autônomo, segundos. |
| `TOL_GRAUS` | `1.5` | Tolerância do giro estacionário, graus. |
| `MS_SHOT` | `0.60` | Power base do shooter na simulação. |
| `MS_STANDBY` | `0.30` | Power em espera do shooter; cuidado durante coleta. |
| `MS_POWER_MAX` | `0.72` | Limite superior de power do shooter. |
| `MI_INTAKE` | `1.00` | Power do intake na coleta. |
| `MI_FEED` | `1.00` | Power do intake ao alimentar. |
| `SA_FECHADO` | `0.20` | Posição normalizada do servo fechado. |
| `SA_ABERTO` | `0.70` | Posição normalizada do servo aberto. |
| `TICKS_POR_VOLTA_MS` | `560.0` | Ticks/revolução do encoder do shooter: APENAS parâmetro de Meca Sim. |
| `RPM_ALVO_MS_SIM` | `127.0` | RPM alvo Meca Sim; NÃO valor físico medido. |
| `RPM_MIN_MS` | `RPM_ALVO_MS_SIM * 0.88` | Limite RPM mínimo calculado da faixa. |
| `RPM_MAX_MS` | `RPM_ALVO_MS_SIM * 1.10` | Limite RPM máximo calculado da faixa. |
| `KP_RPM_MS` | `0.0015` | Ganho proporcional de correção RPM. |
| `JANELA_RPM` | `0.080` | Intervalo entre leituras RPM, segundos. |
| `AMOSTRAS_RPM_ESTAVEL` | `3` | Número de amostras consecutivas válidas. |
| `TIMEOUT_ENCODER_MS` | `0.60` | Tempo para detectar ausência de mudança do encoder shooter, segundos. |
| `INTERVALO_TIROS` | `0.82` | Tempo mínimo entre inícios de tiros, segundos. |
| `INTERVALO_TIROS_L2` | `0.70` | Cadência específica do posto L2, segundos. |
| `PULSO_ALIMENTADOR` | `0.14` | Tempo do servo na posição aberta para tiros normais. |
| `PULSO_ULTIMO_L2` | `0.25` | Pulso mais longo do último tiro L2/terceiro. |
| `PRE_ALIMENTACAO_L2` | `0.07` | Tempo MI pré-servo em L2. |
| `TEMPO_LIMPEZA_FINAL_L2` | `0.12` | Espera após quarto pulso antes de encerrar. |
| `POLLENS_POR_POSTO` | `4` | Número desejado de pulsos por posição. |
| `ARENA` | `3.660` | Comprimento assumido do lado da arena, metros. |
| `CHASSI` | `0.450` | Lado do quadrado do chassi, metros. |
| `FOLGA_L2` | `0.015` | Folga objetivo entre chassi e parede superior. |

**Métodos declarados na fonte:** `runOpMode()`, `dispararQuatro()`, `aguardarRpmEIntervalo()`, `esperarAte()`, `segmento()`, `deslocarSemGirar()`, `linhaGardenY()`, `folgaInferior()`, `folgaEsquerda()`, `percorrerGardenGuiado()`, `aproximarL2()`, `girar()`, `andar()`, `atualizarOdom()`, `prazoEsgotado()`, `yaw()`, `norm()`, `parar()`, `falha()`, `telemetria()`.

### L1-04 v1 — 1785 linhas, OpMode `TESTE L1-04 Garden L2 Flower PARK`

Fonte: [Java exato](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/blob/main/historico/meca_sim_l1_2026_10_29/AutonomoL104GardenL2FlowerParkRPM.java) · Blob Git SHA `a02bed962739c02b33befb0ee51ea7542a50918a`.

| Constante original | Valor ou expressão original | Para que serve |
|---|---|---|
| `TICKS_M` | `560.0 / (Math.PI * 0.090)` | Conversão encoder/distance da tração (ticks por metro). |
| `X_START` | `1.530` | X inicial assumido no mapa; não detectado fisicamente. |
| `Y_START` | `0.255` | Y inicial assumido no mapa; não detectado fisicamente. |
| `X_PRE_PARK` | `0.520` | Coordenada X de preparação do PARK. |
| `Y_PRE_PARK` | `2.380` | Coordenada Y de preparação do PARK. |
| `X_PARK` | `0.255` | Centro X do PARK pretendido. |
| `Y_PARK` | `2.380` | Centro Y do PARK pretendido. |
| `Y_PREPARACAO_GARDEN` | `0.450` | Linha de preparação antes da aproximação ao Garden. |
| `Y_GARDEN_COLETA` | `0.262` | Y do centro do chassi ao coletar no Garden. |
| `X_GARDEN_FIM` | `0.285` | X final da varredura Garden. |
| `X_GARDEN_RETORNO` | `1.420` | X ao terminar o recuo Garden. |
| `COMPRIMENTO_RAMPA_GARDEN` | `1.050` | Comprimento geométrico de transição de Garden. |
| `FOLGA_MIN_GARDEN` | `0.012` | Folga mínima calculada de parede no Garden. |
| `LIM_GLOBAL` | `29.3` | Tempo global interno do autônomo, segundos. |
| `X_CORREDOR_FLOWER` | `0.86` | X do corredor usado na v1 L1-04 (geometria superada). |
| `Y_SAIDA_L2_SEGURA` | `2.80` | Y pretendido de afastamento da parede L2 (v1 L1-04). |
| `Y_PASSAGEM_FLOWER` | `0.69` | Y do caminho da v1 L1-04 (geometria incorreta). |
| `X_FLOWER` | `1.30` | X de aproximação/varredura da Flower. |
| `Y_FLOWER_COLETA` | `0.278` | Y no fim da coleta Flower. |
| `X_TERCEIRO_LANCAMENTO` | `1.68` | X de tiro extra proposto pela v1 L1-04. |
| `Y_TERCEIRO_LANCAMENTO` | `0.405` | Y de tiro extra proposto pela v1 L1-04. |
| `MARGEM_FOLGA` | `0.015` | Margem mínima geométrica em área restrita. |
| `POLLEN_FLOWER` | `4` | POLLEN planejados no terceiro posto; NÃO representa estoque medido. |
| `TOL_GRAUS` | `1.5` | Tolerância do giro estacionário, graus. |
| `MS_SHOT` | `0.60` | Power base do shooter na simulação. |
| `MS_STANDBY` | `0.30` | Power em espera do shooter; cuidado durante coleta. |
| `MS_POWER_MAX` | `0.72` | Limite superior de power do shooter. |
| `MI_INTAKE` | `1.00` | Power do intake na coleta. |
| `MI_FEED` | `1.00` | Power do intake ao alimentar. |
| `SA_FECHADO` | `0.20` | Posição normalizada do servo fechado. |
| `SA_ABERTO` | `0.70` | Posição normalizada do servo aberto. |
| `TICKS_POR_VOLTA_MS` | `560.0` | Ticks/revolução do encoder do shooter: APENAS parâmetro de Meca Sim. |
| `RPM_ALVO_MS_SIM` | `127.0` | RPM alvo Meca Sim; NÃO valor físico medido. |
| `RPM_MIN_MS` | `RPM_ALVO_MS_SIM * 0.88` | Limite RPM mínimo calculado da faixa. |
| `RPM_MAX_MS` | `RPM_ALVO_MS_SIM * 1.10` | Limite RPM máximo calculado da faixa. |
| `KP_RPM_MS` | `0.0015` | Ganho proporcional de correção RPM. |
| `JANELA_RPM` | `0.080` | Intervalo entre leituras RPM, segundos. |
| `AMOSTRAS_RPM_ESTAVEL` | `3` | Número de amostras consecutivas válidas. |
| `TIMEOUT_ENCODER_MS` | `0.60` | Tempo para detectar ausência de mudança do encoder shooter, segundos. |
| `INTERVALO_TIROS` | `0.82` | Tempo mínimo entre inícios de tiros, segundos. |
| `INTERVALO_TIROS_L2` | `0.70` | Cadência específica do posto L2, segundos. |
| `PULSO_ALIMENTADOR` | `0.14` | Tempo do servo na posição aberta para tiros normais. |
| `PULSO_ULTIMO_L2` | `0.25` | Pulso mais longo do último tiro L2/terceiro. |
| `PRE_ALIMENTACAO_L2` | `0.07` | Tempo MI pré-servo em L2. |
| `TEMPO_LIMPEZA_FINAL_L2` | `0.12` | Espera após quarto pulso antes de encerrar. |
| `POLLENS_POR_POSTO` | `4` | Número desejado de pulsos por posição. |
| `ARENA` | `3.660` | Comprimento assumido do lado da arena, metros. |
| `CHASSI` | `0.450` | Lado do quadrado do chassi, metros. |
| `FOLGA_L2` | `0.015` | Folga objetivo entre chassi e parede superior. |

**Métodos declarados na fonte:** `runOpMode()`, `dispararQuatro()`, `aguardarRpmEIntervalo()`, `esperarAte()`, `segmento()`, `deslocarSemGirar()`, `linhaGardenY()`, `folgaInferior()`, `folgaEsquerda()`, `percorrerGardenGuiado()`, `avancarFlowerComLimite()`, `aproximarL2()`, `girar()`, `andar()`, `atualizarOdom()`, `prazoEsgotado()`, `yaw()`, `norm()`, `parar()`, `falha()`, `telemetria()`.

### L1-04 v2 — 1799 linhas, OpMode `TESTE L1-04 Flower SUPERIOR v2`

Fonte: [Java exato](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/blob/main/historico/meca_sim_l1_2026_10_29/AutonomoL104FlowerSuperiorRPMv2.java) · Blob Git SHA `2ce0a70bb46e1b5abff3437cfadebecc6772188e`.

| Constante original | Valor ou expressão original | Para que serve |
|---|---|---|
| `TICKS_M` | `560.0 / (Math.PI * 0.090)` | Conversão encoder/distance da tração (ticks por metro). |
| `X_START` | `1.530` | X inicial assumido no mapa; não detectado fisicamente. |
| `Y_START` | `0.255` | Y inicial assumido no mapa; não detectado fisicamente. |
| `X_PRE_PARK` | `0.520` | Coordenada X de preparação do PARK. |
| `Y_PRE_PARK` | `2.380` | Coordenada Y de preparação do PARK. |
| `X_PARK` | `0.255` | Centro X do PARK pretendido. |
| `Y_PARK` | `2.380` | Centro Y do PARK pretendido. |
| `Y_PREPARACAO_GARDEN` | `0.450` | Linha de preparação antes da aproximação ao Garden. |
| `Y_GARDEN_COLETA` | `0.262` | Y do centro do chassi ao coletar no Garden. |
| `X_GARDEN_FIM` | `0.285` | X final da varredura Garden. |
| `X_GARDEN_RETORNO` | `1.420` | X ao terminar o recuo Garden. |
| `COMPRIMENTO_RAMPA_GARDEN` | `1.050` | Comprimento geométrico de transição de Garden. |
| `FOLGA_MIN_GARDEN` | `0.012` | Folga mínima calculada de parede no Garden. |
| `LIM_GLOBAL` | `29.3` | Tempo global interno do autônomo, segundos. |
| `X_FLOWER` | `1.30` | X de aproximação/varredura da Flower. |
| `Y_FLOWER_PRE` | `3.060` | Y de preparação da Flower superior. |
| `Y_FLOWER_COLETA` | `3.398` | Y no fim da coleta Flower. |
| `X_LANCAMENTO_L2` | `1.574` | X de lançamento L2. |
| `MARGEM_FOLGA` | `0.015` | Margem mínima geométrica em área restrita. |
| `RESERVA_PARK` | `4.0` | Tempo reservado/estimado para PARK, segundos. |
| `LIMITE_INICIO_TERCEIRO` | `23.8` | Alerta ou corte temporal de terceira sequência, segundos. |
| `TOL_GRAUS` | `1.5` | Tolerância do giro estacionário, graus. |
| `MS_SHOT` | `0.60` | Power base do shooter na simulação. |
| `MS_STANDBY` | `0.30` | Power em espera do shooter; cuidado durante coleta. |
| `MS_POWER_MAX` | `0.72` | Limite superior de power do shooter. |
| `MI_INTAKE` | `1.00` | Power do intake na coleta. |
| `MI_FEED` | `1.00` | Power do intake ao alimentar. |
| `SA_FECHADO` | `0.20` | Posição normalizada do servo fechado. |
| `SA_ABERTO` | `0.70` | Posição normalizada do servo aberto. |
| `TICKS_POR_VOLTA_MS` | `560.0` | Ticks/revolução do encoder do shooter: APENAS parâmetro de Meca Sim. |
| `RPM_ALVO_MS_SIM` | `127.0` | RPM alvo Meca Sim; NÃO valor físico medido. |
| `RPM_MIN_MS` | `RPM_ALVO_MS_SIM * 0.88` | Limite RPM mínimo calculado da faixa. |
| `RPM_MAX_MS` | `RPM_ALVO_MS_SIM * 1.10` | Limite RPM máximo calculado da faixa. |
| `KP_RPM_MS` | `0.0015` | Ganho proporcional de correção RPM. |
| `JANELA_RPM` | `0.080` | Intervalo entre leituras RPM, segundos. |
| `AMOSTRAS_RPM_ESTAVEL` | `3` | Número de amostras consecutivas válidas. |
| `TIMEOUT_ENCODER_MS` | `0.60` | Tempo para detectar ausência de mudança do encoder shooter, segundos. |
| `INTERVALO_TIROS` | `0.82` | Tempo mínimo entre inícios de tiros, segundos. |
| `INTERVALO_TIROS_L2` | `0.70` | Cadência específica do posto L2, segundos. |
| `PULSO_ALIMENTADOR` | `0.14` | Tempo do servo na posição aberta para tiros normais. |
| `PULSO_ULTIMO_L2` | `0.25` | Pulso mais longo do último tiro L2/terceiro. |
| `PRE_ALIMENTACAO_L2` | `0.07` | Tempo MI pré-servo em L2. |
| `TEMPO_LIMPEZA_FINAL_L2` | `0.12` | Espera após quarto pulso antes de encerrar. |
| `POLLENS_POR_POSTO` | `4` | Número desejado de pulsos por posição. |
| `ARENA` | `3.660` | Comprimento assumido do lado da arena, metros. |
| `CHASSI` | `0.450` | Lado do quadrado do chassi, metros. |
| `FOLGA_L2` | `0.015` | Folga objetivo entre chassi e parede superior. |

**Métodos declarados na fonte:** `runOpMode()`, `dispararQuatro()`, `aguardarRpmEIntervalo()`, `esperarAte()`, `segmento()`, `deslocarSemGirar()`, `linhaGardenY()`, `folgaInferior()`, `folgaEsquerda()`, `percorrerGardenGuiado()`, `avancarFlowerComLimite()`, `aproximarL2()`, `girar()`, `andar()`, `atualizarOdom()`, `prazoEsgotado()`, `yaw()`, `norm()`, `parar()`, `falha()`, `telemetria()`.

### L1-05 v17 — 2236 linhas, OpMode `TESTE L1-05 V17 2 TIPS SEM PARK`

Fonte: [Java exato](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/blob/main/historico/meca_sim_l1_2026_10_29/AutonomoL105DoisTipsParadaL2RPMv17.java) · Blob Git SHA `20c171fa2b1368efd60fba76a2e31e45aefb1025`.

| Constante original | Valor ou expressão original | Para que serve |
|---|---|---|
| `TICKS_M` | `560.0 / (Math.PI * 0.090)` | Conversão encoder/distance da tração (ticks por metro). |
| `X_START` | `1.530` | X inicial assumido no mapa; não detectado fisicamente. |
| `Y_START` | `0.255` | Y inicial assumido no mapa; não detectado fisicamente. |
| `Y_PREPARACAO_GARDEN` | `0.450` | Linha de preparação antes da aproximação ao Garden. |
| `Y_GARDEN_COLETA` | `0.262` | Y do centro do chassi ao coletar no Garden. |
| `X_GARDEN_FIM` | `0.285` | X final da varredura Garden. |
| `X_GARDEN_RETORNO` | `1.420` | X ao terminar o recuo Garden. |
| `COMPRIMENTO_RAMPA_GARDEN` | `1.050` | Comprimento geométrico de transição de Garden. |
| `FOLGA_MIN_GARDEN` | `0.012` | Folga mínima calculada de parede no Garden. |
| `LIM_GLOBAL` | `29.95` | Tempo global interno do autônomo, segundos. |
| `X_FLOWER` | `1.30` | X de aproximação/varredura da Flower. |
| `Y_FLOWER_PRE` | `3.060` | Y de preparação da Flower superior. |
| `Y_FLOWER_COLETA` | `3.398` | Y no fim da coleta Flower. |
| `X_LANCAMENTO_L2` | `1.574` | X de lançamento L2. |
| `Y_L2_LANCAMENTO` | `3.420` | Y de lançamento L2. |
| `Y_PRE_APROX_L2` | `3.340` | Y de pré-aproximação L2. |
| `MARGEM_FOLGA` | `0.015` | Margem mínima geométrica em área restrita. |
| `P_ENTRADA_FLOWER_CURVA` | `0.50` | Power máximo da curva de entrada Flower. |
| `LIMITE_INICIO_TERCEIRO` | `23.8` | Alerta ou corte temporal de terceira sequência, segundos. |
| `TOL_GRAUS` | `1.5` | Tolerância do giro estacionário, graus. |
| `P_CORREDOR_HIVE` | `0.96` | Power máximo no corredor sob HIVE. |
| `P_SAIDA_L2` | `0.72` | Power previsto para saída de L2. |
| `P_TRANSVERSAL_FLOWER` | `0.73` | Power previsto para deslocamento lateral Flower. |
| `P_RECUO_FLOWER` | `0.78` | Power de recuo da Flower. |
| `P_RETORNO_L2` | `0.77` | Power de retorno à posição L2. |
| `P_APROX_FINAL_L2` | `0.40` | Power para aproximação final de L2. |
| `MS_SHOT` | `0.60` | Power base do shooter na simulação. |
| `MS_STANDBY` | `0.30` | Power em espera do shooter; cuidado durante coleta. |
| `MS_COLETA_SEGURO` | `0.00` | Power seguro do shooter durante intake (deve ser zero). |
| `MS_POWER_MAX` | `0.72` | Limite superior de power do shooter. |
| `MI_INTAKE` | `1.00` | Power do intake na coleta. |
| `MI_FEED` | `1.00` | Power do intake ao alimentar. |
| `SA_FECHADO` | `0.20` | Posição normalizada do servo fechado. |
| `SA_ABERTO` | `0.70` | Posição normalizada do servo aberto. |
| `TICKS_POR_VOLTA_MS` | `560.0` | Ticks/revolução do encoder do shooter: APENAS parâmetro de Meca Sim. |
| `RPM_ALVO_MS_SIM` | `127.0` | RPM alvo Meca Sim; NÃO valor físico medido. |
| `RPM_MIN_MS` | `RPM_ALVO_MS_SIM * 0.88` | Limite RPM mínimo calculado da faixa. |
| `RPM_MAX_MS` | `RPM_ALVO_MS_SIM * 1.10` | Limite RPM máximo calculado da faixa. |
| `KP_RPM_MS` | `0.0015` | Ganho proporcional de correção RPM. |
| `JANELA_RPM` | `0.080` | Intervalo entre leituras RPM, segundos. |
| `AMOSTRAS_RPM_ESTAVEL` | `3` | Número de amostras consecutivas válidas. |
| `TIMEOUT_ENCODER_MS` | `0.60` | Tempo para detectar ausência de mudança do encoder shooter, segundos. |
| `INTERVALO_TIROS` | `0.82` | Tempo mínimo entre inícios de tiros, segundos. |
| `INTERVALO_TIROS_L2` | `0.84` | Cadência específica do posto L2, segundos. |
| `INTERVALO_TIROS_TERCEIRO` | `0.84` | Cadência específica do terceiro ciclo, segundos. |
| `PULSO_ALIMENTADOR` | `0.14` | Tempo do servo na posição aberta para tiros normais. |
| `PULSO_ULTIMO_L2` | `0.32` | Pulso mais longo do último tiro L2/terceiro. |
| `PRE_ALIMENTACAO_L2` | `0.09` | Tempo MI pré-servo em L2. |
| `TEMPO_LIMPEZA_FINAL_L2` | `0.16` | Espera após quarto pulso antes de encerrar. |
| `POLLENS_POR_POSTO` | `4` | Número desejado de pulsos por posição. |
| `ARENA` | `3.660` | Comprimento assumido do lado da arena, metros. |
| `CHASSI` | `0.450` | Lado do quadrado do chassi, metros. |
| `FOLGA_L2` | `0.015` | Folga objetivo entre chassi e parede superior. |

**Métodos declarados na fonte:** `runOpMode()`, `dispararQuatro()`, `iniciarColetaSegura()`, `encerrarColetaSegura()`, `alimentarSomenteSeAutorizado()`, `validarPoseDisparo()`, `aguardarRpmEIntervalo()`, `ativarControleShooter()`, `desativarControleShooter()`, `atualizarRPMContinuo()`, `marcarFase()`, `medirVelocidadeLivre()`, `esperarAte()`, `segmento()`, `deslocarSemGirar()`, `linhaGardenY()`, `folgaInferior()`, `folgaEsquerda()`, `percorrerGardenGuiado()`, `entrarFlowerEmCurva()`, `avancarFlowerComLimite()`, `bezier()`, `seguirBezier()`, `confirmarPoseL2()`, `aproximarL2()`, `girar()`, `andar()`, `atualizarOdom()`, `prazoEsgotado()`, `yaw()`, `norm()`, `parar()`, `falha()`, `telemetria()`.

### L2-01 v1 — 391 linhas, OpMode `TESTE L2-01 V1 4 POLLEN PARK`

Fonte: [Java exato](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/blob/main/historico/meca_sim_l2_2026_11_03/AutonomoL201L2QuatroParkRPMv1.java) · Blob Git SHA `5a08dde59ebd025183f794719978ba9e16e86f25`.

| Constante original | Valor ou expressão original | Para que serve |
|---|---|---|
| `LIMITE_TOTAL` | `29.30` | Tempo global interno do autônomo, segundos. |
| `RESERVA_PARK` | `5.00` | Tempo reservado/estimado para PARK, segundos. |
| `ARENA` | `3.660` | Comprimento assumido do lado da arena, metros. |
| `CHASSI` | `0.450` | Lado do quadrado do chassi, metros. |
| `X_INICIAL` | `1.574` | X da largada L2 assumido no Meca Sim. |
| `Y_INICIAL` | `3.420` | Y da largada L2 assumido no Meca Sim. |
| `YAW_INICIAL` | `180.0` | Orientação de largada L2 assumida; offset IMU não valida pose. |
| `X_PARK` | `0.255` | Centro X do PARK pretendido. |
| `Y_PARK` | `2.380` | Centro Y do PARK pretendido. |
| `YAW_PARK` | `90.0` | Orientação final do PARK. |
| `TICKS_M` | `560.0 / (Math.PI * 0.090)` | Conversão encoder/distance da tração (ticks por metro). |
| `TICKS_VOLTA_MS_SIM` | `560.0` | Ticks/revolução do encoder do shooter: APENAS Meca Sim. |
| `RPM_ALVO_SIM` | `127.0` | RPM alvo Meca Sim; NÃO valor físico medido. |
| `RPM_MIN` | `RPM_ALVO_SIM * 0.88` | Limite RPM mínimo calculado da faixa. |
| `RPM_MAX` | `RPM_ALVO_SIM * 1.10` | Limite RPM máximo calculado da faixa. |
| `INTERVALO_TIROS` | `0.84` | Tempo mínimo entre inícios de tiros, segundos. |
| `JANELA_RPM` | `0.080` | Intervalo entre leituras RPM, segundos. |
| `POTENCIA_MS_BASE` | `0.60` | Power base do shooter na simulação. |
| `SA_FECHADO` | `0.20` | Posição normalizada do servo fechado. |
| `SA_ABERTO` | `0.70` | Posição normalizada do servo aberto. |

**Métodos declarados na fonte:** `runOpMode()`, `dispararQuatro()`, `seguirPark()`, `ligarShooter()`, `atualizarRPM()`, `atualizarOdom()`, `poseL2Valida()`, `esperar()`, `desligarShooter()`, `fecharAlimentador()`, `pararTracao()`, `pararTudo()`, `falhar()`, `prazoEsgotado()`, `yawBruto()`, `yaw()`, `norm()`, `bezier()`, `telemetriaFinal()`.

### L2-02 v2 — 716 linhas, OpMode `TESTE L2-02 V2 SEM CAMERA`

Fonte: [Java exato](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/blob/main/historico/meca_sim_l2_2026_11_03/AutonomoL202Flower8PollenSemCameraRPMv2.java) · Blob Git SHA `5cd588e02234d6ade37e72fc6f0d0f82d58725a8`.

| Constante original | Valor ou expressão original | Para que serve |
|---|---|---|
| `LIMITE_TOTAL` | `29.30` | Tempo global interno do autônomo, segundos. |
| `RESERVA_PARK` | `4.00` | Tempo reservado/estimado para PARK, segundos. |
| `ARENA` | `3.660` | Comprimento assumido do lado da arena, metros. |
| `CHASSI` | `0.450` | Lado do quadrado do chassi, metros. |
| `X_INICIAL` | `1.574` | X da largada L2 assumido no Meca Sim. |
| `Y_INICIAL` | `3.420` | Y da largada L2 assumido no Meca Sim. |
| `YAW_INICIAL` | `180.0` | Orientação de largada L2 assumida; offset IMU não valida pose. |
| `X_PARK` | `0.255` | Centro X do PARK pretendido. |
| `Y_PARK` | `2.380` | Centro Y do PARK pretendido. |
| `YAW_PARK` | `90.0` | Orientação final do PARK. |
| `X_FLOWER` | `1.300` | X de aproximação/varredura da Flower. |
| `Y_FLOWER_PRE` | `3.060` | Y de preparação da Flower superior. |
| `Y_FLOWER_COLETA` | `3.398` | Y no fim da coleta Flower. |
| `MARGEM_PAREDE` | `0.010` | Margem de segurança geométrica de parede. |
| `MARGEM_FIM` | `0.15` | Margem final antes de encerrar, segundos. |
| `TICKS_M` | `560.0 / (Math.PI * 0.090)` | Conversão encoder/distance da tração (ticks por metro). |
| `TICKS_VOLTA_MS_SIM` | `560.0` | Ticks/revolução do encoder do shooter: APENAS Meca Sim. |
| `RPM_ALVO_SIM` | `127.0` | RPM alvo Meca Sim; NÃO valor físico medido. |
| `RPM_MIN` | `RPM_ALVO_SIM * 0.88` | Limite RPM mínimo calculado da faixa. |
| `RPM_MAX` | `RPM_ALVO_SIM * 1.10` | Limite RPM máximo calculado da faixa. |
| `INTERVALO_TIROS` | `0.84` | Tempo mínimo entre inícios de tiros, segundos. |
| `JANELA_RPM` | `0.080` | Intervalo entre leituras RPM, segundos. |
| `POTENCIA_MS_BASE` | `0.60` | Power base do shooter na simulação. |
| `SA_FECHADO` | `0.20` | Posição normalizada do servo fechado. |
| `SA_ABERTO` | `0.70` | Posição normalizada do servo aberto. |

**Métodos declarados na fonte:** `runOpMode()`, `dispararQuatro()`, `seguirPark()`, `executarFlower()`, `entrarFlowerEmCurva()`, `folgaChassi()`, `andarReto()`, `girar()`, `seguirBezier()`, `ligarShooter()`, `atualizarRPM()`, `atualizarOdom()`, `poseL2Valida()`, `esperar()`, `desligarShooter()`, `fecharAlimentador()`, `pararTracao()`, `pararTudo()`, `falhar()`, `prazoEsgotado()`, `yawBruto()`, `yaw()`, `norm()`, `bezier()`, `telemetriaFinal()`.

## 4. Diferenças mais perigosas de ignorar

- **L1-01** usa spawn (1.678, 0.399) e PARK (0.350, 2.820), diferentemente de L1-02/03/04/05 (1.530,0.255) e PARK (0.255,2.380). Não padronizar coordenadas cegamente.
- **L1-03 e L1-04** usam 0.70 s entre tiros em L2, pulso final L2 0.25 s, prefeed 0.07 s e limpeza 0.12 s. **L1-05 v17** usa 0.84 s, pulso final 0.32 s, prefeed 0.09 s e limpeza 0.16 s.
- **L2-01/L2-02** usam intervalo 0.84s e valores de 0.09/0.14/0.32/0.16s **embutidos no método**; não aparecem todos na tabela de constantes. Ao refatorar, extrair sem alterar valores.
- **L1-04 v1** traça Flower no lado errado do mapa (Y ~0.278); **L1-04 v2** corrige para Y ~3.398, mas ainda corta terceiro lançamento por tempo. Não tratar v1 como opção de competição.
- **L1-05 v17** usa limite 29.95 e **NÃO faz PARK**. Todos os demais usam limite 29.3 em sua fonte de referência e as estratégias curtas tentam PARK.
- **L2-01 e L2-02** pressupõem a pose inicial (1.574,3.420,180°) e offset de yaw; ela é provisória, não verificada como largada legal/real. Uma pose assumida não se transforma em medição ao chamar `yawOffset`.
- **Pulsos ≠ ejeções ≠ acertos ≠ TIP**. É obrigatório separar essas quatro observações na telemetria.

## 5. Biblioteca Java utilizada e responsabilidades

| API | Uso nos arquivos | Por que |
|---|---|---|
| `LinearOpMode`, `@Autonomous` | Ciclo do autônomo | INIT/START/STOP e verificação opModeIsActive |
| `hardwareMap.get(...)` | Encontrar ME, MD, MI, MS, SA, imu | Nomes precisam coincidir com configuração RC/simulador |
| `DcMotor`, `Servo` | Controle de potência e servo | Atuadores reais, com limites e proteção contra comandos indevidos |
| `IMU`, `AngleUnit`, `RevHubOrientationOnRobot` | Rumo yaw, montagem HUB | Controle de curva/giro e odometria |
| `ElapsedTime` | Prazo global, trechos, tiros e temporizações | Evitar `sleep()` longo e registrar tempos |
| `Range.clip()` | Limitar comandos | Evitar saturação de motores e controladores |
| `telemetry` | Fase, pose, RPM, contagem, causa de falha | Rastreabilidade dos ensaios |
| `Math.atan2/hypot/sin/cos/sqrt` | Geometria e trajetória | Diferença de posição, rumo alvo, desaceleração, curva Bézier |

## 6. Refatoração POO: regra de preservação

1. **NÃO alterar nenhum dos oito Java originais.** Eles são bases de comparação; L1-04 ambas seguem experimentais.
2. Reutilizar `HardwareMequinha`, `ParametrosMequinha`, `PoseMequinha` e `OdometriaDiferencial`, já existentes em `poo_refatoracao_2026_11_03/`.
3. Criar apenas `AjustesAutonomoMequinha` (valores fáceis de editar), `ControleLancamento` (RPM + MI + MS + SA) e `NavegacaoMequinha` (andar/girar/Bézier), quando passar à próxima fase; não criar hierarquia complexa.
4. As classes de estratégia L1 e L2 continuam contendo a **sequência em português**, curta e legível, sem duplicar fórmulas dos motores.
5. A câmera fica em **módulo isolado futuramente**, com modo desligado por padrão. A ausência de classes de visão no Meca Sim não pode impedir a compilação dos autônomos sem câmera.
6. Comparar cada novo OpMode com a fonte correspondente: sequência, inicialização, direção dos motores, tempo, tiro, pose, rota, erro e parada de segurança.
7. Só substituir o original depois de testes A/B com métricas; jamais prometer equivalência apenas porque compila.

## 7. Fonte de regras e visão (para planejamento futuro, NÃO implementado agora)

- [Materiais oficiais FTC BIOBUZZ atualizados](https://ftc-resources.firstinspires.org/ftc/game).
- [FTC Docs — AprilTag Clusters SDK 12.0](https://ftc-docs.firstinspires.org/en/latest/tech_tips/tech-tips/tech-tip-apriltag-clusters/tech-tip-apriltag-clusters.html).
- BIOBUZZ usa 4 clusters de 4 tags: IDs 30–33, 34–37, 38–41, 42–45. O SDK retorna um objeto de cluster mesmo que só algumas tags dele estejam visíveis. Um cluster não deve ser selecionado por ID numérico fixo.
- Comparar cor da aliança, nome do cluster, `ftcPose.bearing` para ajuste angular e `ftcPose.roll` para evidência de CELL voltada para cima. O HIVE é móvel; AprilTag de CELL não serve como marco fixo de campo.
- Um TIP oficial requer mudança entre estados estáveis com contato do damper com a estrutura. A câmera pode produzir **indício** de virada, não confirmação jurídica da pontuação.

**Este documento cataloga o que há nas fontes. Não altera, compila, instala nem declara aprovadas as estratégias.**
