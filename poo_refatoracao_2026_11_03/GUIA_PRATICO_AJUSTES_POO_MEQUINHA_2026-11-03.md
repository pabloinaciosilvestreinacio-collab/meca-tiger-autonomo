# Manual prático de ajustes e POO simples — Mequinha FTC #32578

**Data:** 03/11/2026  
**Base auditada:** oito arquivos Java (sete estratégias numeradas, incluindo L1-04 v1 e v2).  
**Objetivo:** tornar o código legível e ajustável sem alterar as trajetórias dos autônomos originais.  
**Situação:** arquitetura em preparação; os OpModes originais ainda **não** consomem o novo arquivo `AjustesAutonomoMequinha.java`.

## 1. Como vamos organizar, sem complicação

Cada autônomo vai continuar sendo uma classe Java independente e curta, com a **sequência dos passos**. O restante será extraído somente quando realmente se repetir.

```text
TeamCode/src/main/java/org/firstinspires/ftc/teamcode/
  AutonomoL101POO.java               <- sequência L1-01, quando convertido
  AutonomoL102POO.java               <- sequência L1-02, quando convertido
  ...                                <- uma classe por estratégia
  meca/
    ParametrosMequinha.java          <- medidas físicas e posições servo (já existe)
    AjustesAutonomoMequinha.java     <- parâmetros e perfis fáceis de editar (novo)
    HardwareMequinha.java            <- motores, servo, IMU, parada segura (já existe)
    PoseMequinha.java                <- X, Y, yaw (já existe)
    OdometriaDiferencial.java        <- encoders + orientação IMU (já existe)
    ControleLancamento.java          <- RPM + alimentação + intertravamentos (futuro)
    NavegacaoMequinha.java           <- andar, girar, curva Bézier (futuro)
    CameraHive.java                  <- somente após testes de câmera (futuro, fora do Meca Sim atual)
```

**Não criar** superclasses para cada tipo de Flower, fábrica de robôs, sistema de plugins ou várias camadas de interfaces. Garden e Flower podem continuar como *métodos de percurso* dentro da estratégia, usando `NavegacaoMequinha`. Cada código escolhe o perfil correspondente, sem obrigar todos a usarem os mesmos tempos.

**Regra inegociável:** converter sempre uma cópia, com novo nome de classe/OpMode, e comparar com o original no mesmo cenário. Os originais não serão reescritos nesta etapa.

## 2. Onde ajustar cada coisa

| O que você quer mudar | Onde ficará no POO | Onde procurar nos códigos antigos | Unidade |
|---|---|---|---|
| Posição fechada do servo | `ParametrosMequinha.SERVO_FECHADO` | `SA_FECHADO` | posição 0–1 |
| Posição aberta do servo | `ParametrosMequinha.SERVO_ABERTO` | `SA_ABERTO` | posição 0–1 |
| Tempo que o servo permanece aberto | `AjustesAutonomoMequinha.PULSO_NORMAL_S` e `PULSO_ULTIMO_...` | `PULSO_ALIMENTADOR`, `PULSO_ULTIMO_L2` ou literal `0.14`/`0.32` | segundos |
| Tempo que MI alimenta antes de abrir | `PRE_ALIMENTAR_...` | `PRE_ALIMENTACAO_L2` ou literal `0.09` | segundos |
| Espera após último disparo | `LIMPEZA_...` | `TEMPO_LIMPEZA_FINAL_L2` ou literal `0.16` | segundos |
| Intervalo entre tiros | `INTERVALO_...` | `INTERVALO_TIROS*` | segundos |
| Potência do shooter | `SHOOTER_BASE_POWER_SIM` | `MS_SHOT`/`POTENCIA_MS_BASE` | power 0–1 |
| RPM alvo do shooter | `RPM_ALVO_SIM` | `RPM_ALVO_MS_SIM`/`RPM_ALVO_SIM` | rotações/min |
| Ticks por volta do shooter | `SHOOTER_TICKS_VOLTA_SIM` | `TICKS_POR_VOLTA_MS`/`TICKS_VOLTA_MS_SIM` | ticks/volta |
| Potência da coleta | `INTAKE_COLETA_POWER` | `MI_INTAKE` ou literal `1.00` | power 0–1 |
| Distância avançada | variável da rota, depois `NavegacaoMequinha` | chamadas a `andar`, `segmento`, `deslocarSemGirar` | metros |
| Posição de L1/L2 | constantes da estratégia, depois `PoseMequinha` | `X_START`, `Y_START`, `X_INICIAL`, `Y_INICIAL` | metros |
| Posição do PARK | constantes da estratégia | `X_PARK`, `Y_PARK`, `YAW_PARK` | metros/graus |
| Velocidade da curva | estratégia e `NavegacaoMequinha` | último argumento `potenciaMax`, `P_CORREDOR_HIVE`, etc. | power 0–1 |
| Tolerância angular | estratégia/navegação | `TOL_GRAUS`, `erroFinalYaw` | graus |
| Prazo do autônomo | por estratégia | `LIM_GLOBAL` ou `LIMITE_TOTAL` | segundos |
| Webcam/AprilTag/HIVE | **módulo futuro separado** | **não há câmera nos oito arquivos selecionados** | vide seção 6 |

**Atenção:** como os OpModes atuais não usam o novo arquivo de ajustes, **alterar um valor nele hoje não mudará a competição**. Até a conversão de cada código, edite uma **cópia** do original no próprio local indicado na tabela.

## 3. Tutorial: ajustar o servo SA

**Duas coisas diferentes:**

- `SA_ABERTO = 0.70` define a posição que o servo tenta atingir.
- `PULSO_ALIMENTADOR = 0.14` define por quanto tempo manter o servo aberto; **não** muda a posição.

Exemplo da lógica atual (simplificado; ilustrativo):

```java
SA.setPosition(SA_ABERTO);             // Abre
esperarAte(PULSO_ALIMENTADOR, prazo);   // Mantém por um tempo
SA.setPosition(SA_FECHADO);            // Fecha
```

**Procedimento recomendado, na ordem:**

1. Faça **cópia da versão** e registre o valor anterior.
2. Sem POLLEN e com MS/MI desligados, teste apenas `SA_FECHADO` e `SA_ABERTO` para verificar limites mecânicos. Nunca force o braço do servo até bater no batente. Ajuste a posição em pequenos passos (por exemplo 0,02) **somente se necessário**.
3. Mantenha as posições e ajuste **somente o tempo aberto**. O padrão dos primeiros três tiros é `0.14 s`; para o último em L1-03/L1-04, `0.25 s`; e em L1-05/L2-01/L2-02, `0.32 s`.
4. Teste 4 disparos isolados, com o mecanismo protegido e a câmera do telefone voltada para a saída. Registre **pulsos comandados e elementos realmente ejetados**, separadamente.
5. Se o último falhar, primeiro identifique se faltou POLLEN, se o encoder/RPM estava fora da faixa, se o prazo cortou o pulso ou se o alimentador não empurrou até o fim. **Não aumentar o servo e o RPM simultaneamente.**
6. Recoloque a versão anterior se piorar e anote o motivo. Testar com óculos de proteção, trajetória livre e supervisor da equipe.

**Risco específico do Meca Sim:** em algumas configurações, `MI > 0.50` **junto** com `MS > 0.50` pode ejetar POLLEN mesmo com servo fechado. Assim, aumentar pré-alimentação pode causar ejeção não intencional; não interpretar a contagem de pulsos como ejeções.

## 4. Tutorial: cadência, RPM e intake

### Intervalo entre tiros

- L1-01/L1-02/L1-03 em L1: **0,82 s**.
- L1-03/L1-04 em L2: **0,70 s**.
- L1-05 v17 em L2 e no terceiro ciclo: **0,84 s**.
- L2-01/L2-02: **0,84 s**.

Se reduzir o intervalo, o último tiro pode chegar com o shooter ainda desacelerado. Se aumentar, pode estourar o limite de 30 s. Altere **uma cadência por perfil** e verifique se o RPM recupera antes de cada pulso.

### RPM

O código usa a fórmula:

`RPM = (deltaTicks / ticksPorVoltaDoShooter) * (60 / deltaTempoSegundos)`.

No Meca Sim: referência **127 RPM**, 560 ticks/volta do **shooter simulado**, faixa 88–110% e três leituras estáveis de aproximadamente 80 ms. **Isso não é medição do motor físico.** No robô real, identificar o encoder do MS e a transmissão antes de configurar ticks por volta ou alvo de RPM; medir com telemetria, depois calibrar contra acertos. O valor de 560 ticks/volta da **roda de locomoção** não deve ser copiado para o shooter por coincidência numérica do simulador.

### Intake

Na coleta Garden/Flower: MI pode usar potência 1.00, mas o MS deve ficar desligado para evitar liberar POLLEN durante o percurso. Durante os tiros, MI só opera na fase de alimentação autorizada, com tração parada e RPM estável. A checagem da pose deve preceder a abertura do servo.

## 5. Tutorial: navegação, posição, giros e PARK

1. **Observe o mapa do Meca Sim.** L1-01 usa spawn `(1.678, 0.399, 0°)`, enquanto L1-02/03/04/05 usam `(1.530, 0.255, 0°)`. As duas estratégias L2 usam uma **hipótese de spawn** `(1.574, 3.420, 180°)`. Não padronizar sem testar.
2. No código, `yaw = 0°` aponta para **+Y**, e `yaw = 90°` aponta para **−X**. O MD está em `REVERSE`. Se a correção gira no sentido errado, confirmar essas convenções antes de inverter `KP`.
3. A odometria estima pose com encoders+IMU. `imu.resetYaw()`/offset de 180° **não localizam o robô no campo**: é necessário configurar e conferir a pose inicial.
4. Para um deslocamento que termina cedo, examine **tolerância espacial** e timeout separadamente. Aumentar timeout não torna a curva mais rápida; aumentar tolerância espacial reduz precisão.
5. Verifique o **quadrado do chassi de 45 cm**, não apenas o ponto central. A folga nominal de 15 mm à parede é pequena para hardware real e erros de odometria.
6. PARK em L1-05 v17 **não existe por decisão de estratégia**. L2-02 pode omitir o PARK caso haja pouco tempo. **Não adicionar PARK indiscriminadamente** a perfis ofensivos.

### Como testar um ajuste de percurso

Em uma cópia do OpMode, mude um único waypoint, potência ou tolerância; mantenha todo o restante. Execute três vezes, anote tempo de cada trecho, X/Y/yaw estimados no início e no fim, colisões, saídas da zona de segurança e se o mecanismo permanece desligado. O teste físico só começa depois que a geometria no simulador estiver coerente.

## 6. Assistência de câmera e HIVE — PLANO FUTURO, não ativado agora

**Por decisão do operador, os oito autônomos deste catálogo permanecem SEM CÂMERA.** O arquivo separado L2-01 v2 com visão segue arquivado para o teste apropriado. Quando a integração for autorizada, usar um módulo `CameraHive.java` dedicado ao FTC SDK 12.0, sem inseri-lo nas versões do Meca Sim que não possuem `VisionPortal`.

### Etapa 1 — Inicialização e diagnóstico da câmera

- Logitech C270, nome do hardware `Webcam 1`, resolução inicial 640×480 (validar suporte e calibração de intrínsecos).
- Verificar stream real, tempo de inicialização, luz/exposição, frames válidos e fechamento seguro.
- Em caso de falha, estado `CAMERA_INDISPONIVEL`; **timeout em toda espera**. Nenhum `while` infinito esperando tag.

### Etapa 2 — Reconhecer todos os clusters da BIOBUZZ

- BIOBUZZ: quatro clusters, cada um com quatro IDs únicos: **30–33 (vermelho oposto à audiência), 34–37 (vermelho audiência), 38–41 (azul audiência), 42–45 (azul oposto à audiência)**.
- SDK 12.0 retorna `AprilTagClusterDetection` para clusters. Percorrer **todas** as detecções recebidas por `AprilTagProcessor.getDetections()`, não selecionar um ID fixo; observar o nome do cluster em `metadata.name`.
- Escolher para mirar somente a **aliança configurada** e CELL aparentemente voltada para cima. Para uma câmera apontada na direção do lançamento, `abs(ftcPose.roll) < 90°` é **indício** de CELL pontuável; verificar várias amostras e qualidade da imagem.

### Etapa 3 — Alinhar ao HIVE por visão

- O alinhamento fino usa o `bearing` do cluster escolhido e um controle proporcional/PD/PID de **giro**, não a distância dos encoders como medidor de erro visual.
- A câmera pode estar deslocada ou inclinada em relação ao lançador: medir e corrigir o **offset angular entre o eixo óptico e o trajeto do POLLEN**. `bearing=0` não garante acerto se esse offset for ignorado.
- Testar o sinal do giro com potência muito baixa e robô suspenso/isolado, antes de autorizar correção automática. Impor limite de potência, timeout, tolerância angular, mínimo de amostras estáveis e `pararTracao()` na saída.
- Se o alvo desaparecer, **não tentar girar eternamente**; registrar `VISUAL_INCONCLUSIVO` e seguir apenas a política segura e pré-testada do autônomo (pode ser parar ou usar pose estimada; não disparar de posição perigosa).

### Etapa 4 — Verificar mudança do HIVE e possível TIP

- Registrar cluster/CELL estável **antes** e **depois** dos lançamentos; não interpretar uma única detecção com ruído como virada.
- Mudança da CELL aparentemente voltada para cima pode indicar HIVE virado. Também considerar perda de visão, oclusão, robô deslocando ou mudanças de iluminação.
- O manual oficial considera um TIP quando o HIVE passa entre **dois estados estáveis** e o damper passa a contactar a estrutura; uma câmera pode fornecer um **indício**, mas não autentica a pontuação oficial nem mede necessariamente o contato do damper.
- Telemetria futura deve distinguir: `TAG_DETECTADA`, `ALVO_ALINHADO`, `TROCA_VISUAL_DA_CELL`, `TIP_INDICIO`, `TIP_NAO_VERIFICADO`. Nunca declarar `TIP_CONFIRMADO` apenas porque ocorreram 4 pulsos ou uma mudança instantânea de roll.

### Etapa 5 — Repetibilidade e integração

Validar a câmera no FTC SDK real antes de integrar cada estratégia. Na validação, comparar sem visão × com visão mantendo trajetória, RPM e servo. Se o Meca Sim não possui as classes `AprilTagClusterDetection`/`VisionPortal`, a ausência delas causa falha de **compilação**: `try/catch` em runtime não resolve; manter arquivos e builds separados.

**Fontes oficiais consultadas:** [Materiais FTC BIOBUZZ](https://ftc-resources.firstinspires.org/ftc/game) e [FTC SDK 12 AprilTag Clusters](https://ftc-docs.firstinspires.org/en/latest/tech_tips/tech-tips/tech-tip-apriltag-clusters/tech-tip-apriltag-clusters.html).

## 7. Registro de ensaio simples, obrigatório para cada alteração

Preencher uma linha por execução:

| Versão e alteração única | Etapa e tempo | Pulsos servo | POLLEN ejetados | Acertos no HIVE | Indício visual de TIP | Pose final / folga | Resultado |
|---|---|---|---|---|---|---|---|
| Ex.: L1-03-POO-test01, último pulso 0.25→0.27 s | L2: __ s | __/4 | __/4 | __/4 | não verificado | X __ Y __ yaw __, folga __ | Aprovado / falhou + motivo |

Antes de aprovar POO, testar uma cópia **sem mudar parâmetros** e comparar com o original. O primeiro objetivo da refatoração é **mesmo comportamento**, não maior velocidade. Só depois fazemos calibrações deliberadas.

## 8. Checklist antes do primeiro teste físico

- [ ] Confirmar hardwareMap: `ME`, `MD`, `MI`, `MS`, `SA`, `imu`; e `Webcam 1` somente quando testar visão.
- [ ] Confirmar direção MD invertida e polaridade dos encoders.
- [ ] Confirmar chassi, roda, redução e ticks/volta medidos no robô real.
- [ ] Calibrar servo aberto/fechado sem POLLEN e com mecanismos desligados.
- [ ] Medir RPM real do shooter com encoder próprio e testar estabilidade.
- [ ] Conferir pose e orientação iniciais de cada estratégia.
- [ ] Confirmar distância das paredes e HIVE com o corpo inteiro do robô.
- [ ] Testar 4/4 ejeções antes de rodar estratégias 8 ou 12.
- [ ] Registrar telemetria e vídeo; identificar classe/OpMode exatos.
- [ ] Confirmar STOP/timeout corta motores e fecha servo.
- [ ] A câmera continua **desligada até a etapa específica de testes**.

**Referências de código:** [`CATALOGO_AUDITADO_8_AUTONOMOS_2026-11-03.md`](CATALOGO_AUDITADO_8_AUTONOMOS_2026-11-03.md), `ParametrosMequinha.java`, `AjustesAutonomoMequinha.java` e o README da POO.