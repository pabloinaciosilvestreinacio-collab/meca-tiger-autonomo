# Mequinha — nova família de autônomos com largada em L2 (03/11/2026)

## Premissas e plano

- **L2-01:** verificar estado de TIP oposto (futura integração de visão) → lançar 4 POLLEN diretamente da largada em L2 → PARK parcial esquerdo. O baseline V1 **não** verifica TIP automaticamente.
- **L2-02:** após lançamento, coletar na Flower superior → PARK, com ensaios de trajetória, alimentação e tempo separados.
- **L2-03:** disparar e avaliar varredura/alternativas condicionadas a evidência de TIP e tempo; não interpretar AprilTag isolada como prova de TIP.
- Hardware 2WD diferencial, ME/MD/MI/MS/SA/imu, câmera Logitech C270 para integração posterior. Java e cada estratégia em classes independentes; versões L1 preservadas.
- **IMPORTANTE:** a decisão de **não fazer PARK** se aplica à estratégia ofensiva L1-05 de três ciclos/12 POLLEN para dois TIPs. Não é regra geral de todo código L2.

## L2-01 v1 — baseline de disparo e PARK (NÃO VALIDADO NO SIMULADOR)

- Arquivo: `AutonomoL201L2QuatroParkRPMv1.java`.
- OpMode: `TESTE L2-01 V1 4 POLLEN PARK`.
- Pose **provisória do experimento**, **não confirmada como largada legal/oficial**: X=1.574 m, Y=3.420 m, yaw=180° (frente do robô apontada ao HIVE). Precisa configurar essa pose em `config` do Meca Sim, pois Java não posiciona o spawn. JSON de projeto criado separadamente: `MecaSim_L2_01_v1_4Pollen_Park.json`.
- Disparos 4x a partir da pose inicial, shooter RPM-alvo=127 específico Meca Sim; 3 amostras consecutivas estáveis / intervalo 0,84s; 0.32s último pulso, MI/MS somente durante a alimentação. Servo .20/.70; sem movimento durante tiros.
- Margem `RESERVA_PARK=5.0s`; `LIMITE_TOTAL=29.30s`, 0.70s antes de 30s.
- PARK: curva Bézier de referência L1 X1.574/Y3.420 → controles X1.574/Y2.650, X0.620/Y2.700 → X0.255/Y2.380, yaw=90°. **Rota ainda não foi validada partindo da pose L2**, inclusive distância ao HIVE.
- IMU: offset de yaw no início para não assumir que `imu.resetYaw()` corresponda ao yaw de mapa 180°; IMU sozinha NÃO comprova posição do spawn. A odometria inicial assume `X_INICIAL/Y_INICIAL`.
- **Visão/TIP oposto: NÃO implementada**. A telemetria apresenta expressamente `NAO VERIFICADO (SEM VISAO)` e o baseline prossegue com 4 lançamentos; não declarar detecção real.
- Se terceiro/quarto tiro falhar por prazo/RPM, tenta PARK mesmo assim. A contagem `pulsosL2` indica apenas pulsos ordenados; não confirma POLLEN, acerto ou TIP.
- **Verificações:** compilação de `javac --release 17` com stubs FTC, inspeção de constantes, IMU e rotas. Sem execução completa Meca Sim nem teste físico. Não aprovada para competição.
- **Próximo passo:** testar posição de spawn, 4 elementos efetivamente ejetados, ausência de disparo antecipado e PARK em 29,3s; repetir 3 vezes, depois tratar a verificação de TIP com a câmera/visão realmente disponível.

## L2-01 v2 — camera C270 + todas AprilTags/Clusters BIOBUZZ (03/11/2026)

- **Pedido:** fazer verificacao por camera sem travar ao esperar um unico ID de AprilTag.
- **Correção fundamental de SDK 12.0:** as IDs BIOBUZZ 30..45 são agrupadas em **4 clusters** (`AprilTagClusterDetection`), e a API também pode retornar `AprilTagSingleDetection` (nenhuma tag singleton oficial em BIOBUZZ). O processor padrão reconhece a biblioteca da temporada; **não usar `AprilTagDetection.id` em SDK12**.
- **Arquivo:** `AutonomoL201L2QuatroParkTodasTagsRPMv2.java`; OpMode `TESTE L2-01 V2 TODAS TAGS`. Base v1 preservada. Projeto JSON `MecaSim_L2_01_v2_VisaoTodasTags.json`.
- **Estratégia da v2:** iniciar `Webcam 1` 640x480 por `VisionPortal` e `AprilTagProcessor.easyCreateWithDefaults()` (biblioteca de temporada); inspecionar **todas as deteccoes e clusters**, inclusive adversários, mas escolher somente CELL da `ALIANCA = "RED"` e `abs(roll)<90` para marcação do alvo scorable. Quando múltiplos clusters da aliança estão visíveis, escolher o melhor por fração visível e bearing; não assumir um ID fixo.
- **Busca limitada:** até 0.65 s antes de disparar, até 0.65 s após o quarto pulso, com amostragem ~0.10s; sem girar, sem mudar o percurso ou o shooter. Se camera ausente, API indisponível ou tags ausentes, status `INCONCLUSIVO` e segue 4 tiros + PARK. Leitura jamais bloqueia tiro/park.
- **Detecção de possível TIP:** comparar nome do cluster scorable estável antes/depois. Mudança pode indicar virada da CELL; não comprova contato físico de damper ou pontuação oficial. Sem observações antes/depois, não inventar mudança. O HIVE/AprilTag são móveis e **não devem ser usados como referências fixas de odometria do campo**.
- **Hardware/contexto:** C270 nome `Webcam 1`. `ALIANCA="RED"` porque projeto Meca Sim está configurado para robô vermelho; deve ser ajustado se a largada for azul e câmera apontar para o alvo. Camera deve olhar no sentido do shooter para validação de roll descrita no guia FTC.
- **Limitações:** fonte compilou em Java 17 com stubs locais que representam APIs de VisionPortal/AprilTag SDK 12.0, **não** compilação integral FTC SDK nem teste no robô ou Meca Sim. O compilador do Meca Sim precisa implementar `VisionPortal`, `AprilTagClusterDetection`, `AprilTagSingleDetection` e `WebcamName`; se as classes não estiverem nos stubs, a falha ocorre **na compilação** e o fallback em tempo de execução não resolve. Não afirmar que há câmera virtual no simulador.
- **Verificação:** novo arquivo 604 linhas; diff frente à v1 apenas integração de câmera, verificações visual de TIP, telemetria e identificador de versão; rotina de 4 pulsos e curva PARK não alteradas.
- **Teste recomendado:** SDK12 real: inicializar e assistir Camera Stream, observar RED SCORING/RED AUDIENCE alternando orientação quando HIVE vira, e documentar roll/range/cluster; no Meca Sim, verificar disponibilidade da API, compilar OpMode e observar se fallback não bloqueia trajetória. Nunca declarar TIP exclusivamente por contadores de pulso.
