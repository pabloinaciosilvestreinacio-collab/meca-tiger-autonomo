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

## L2-02 v1 — 4 L2 + coleta Flower + 4 L2 + PARK condicional + verificacao visual de TIP (03/11/2026)

- **Decisão:** manter L2-01 v2 congelado para testar a webcam C270 amanhã (04/11/2026); não declarar câmera validada. Novo código L2-02 avança a estratégia, com visão obrigatória na ARQUITETURA mas sem bloqueio quando câmera estiver indisponível.
- **Arquivo:** `AutonomoL202Flower8PollenTodasTagsRPMv1.java` (**942 linhas**). OpMode `TESTE L2-02 V1 FLOWER 8 POLLEN`. Projeto Meca Sim: `MecaSim_L2_02_v1_Flower8PollenTodasTags.json`. Java/JSON em classes/arquivos novos, sem sobrescrever L2-01 e L1.
- **Sequência planejada:** spawn provisório na pose L2 (X=1.574,Y=3.420,yaw=180), visão pre/post da primeira série, 4 POLLEN iniciais com MI/MS/SA e RPM, sair em curva para Flower superior (X≈1.300/Y≈3.060, yaw=0), avançar até Y=3.398 com **MS desligado** e MI intake=1.00, recuar 0.35m com intake, parar MI, girar para yaw=128 e retornar em curva de ré à pose L2 (yaw=180), segunda série de 4 tiros; visão antes/depois e PARK esquerdo X=.255/Y=2.380 **apenas se restarem ≥4.00 s** até limite interno de 29.30s. Se janela de PARK insuficiente, permanece em L2 com mecanismos parados (não sai para uma rota inexequível).
- **Visão em todos os ciclos:** usa o módulo da L2-01 v2, examina todas detecções `AprilTagClusterDetection` e `AprilTagSingleDetection` disponíveis, sem ID fixo, filtra leitura da CELL à aliança configurada RED, compara snapshots pre e pós cada grupo de disparos; timeout 0.65/0.45 s, duas amostras novas para estabilidade. Câmera ausente -> `INCONCLUSIVO`, não trava; código precisa de SDK12/compilador com API, ou nem compila no Meca Sim. Não afirmar TIP pontuado pelo processamento visual.
- **Intertravamentos:** durante coleta shooter=0, servo fechado, MI=1.00; nas duas séries somente com robô parado em pose de L2, 3 amostras de RPM consecutivas válidas, pulso #4 prolongado. Nenhum disparo em movimento.
- **Limite 30 s:** interno 29.30 s e reserva PARK 4.0 s (estimativa, não garantia). O quarto POLLEN da segunda série pode não caber se Flower demorar; registra parcial; não força PARK após janela.
- **Rota:** entrada na Flower e curva de retorno adaptadas da familia L1-05 v7; a versão nova é **somente candidata**, não testada integralmente no Meca Sim. A posição de largada, o corredor, a coleta de 4 POLLEN e PARK ainda precisam de validação real em execução.
- **Verificação realizada:** `javac --release 17` com stubs FTC/Vision locais (simula presença das classes, não testa pipeline/câmera). Inspeção de 2 grupos de disparos, captura TIP duas vezes, intertravamento no intake, retorno e condição PARK. Nenhum resultado simulado de acerto/TIP/PARK ainda comprovado.
- **Próximo teste:** amanhã, confirmar compatibilidade da webcam/AprilTag SDK e reconhecimento dos 4 clusters; depois executar L2-02 sem alterar variáveis, inspecionando 4+4 lançamentos reais, dois estados visuais, coleta Flower, último 4º pulso, tempo para PARK e pose final.

## Decisão do operador: SEM CÂMERA por enquanto (03/11/2026)

- Pedido explícito: **não incorporar visão, VisionPortal, webcam, AprilTags, clusters nem checagem automática de TIP nos novos códigos L2 até a etapa própria de testes**.
- L2-01 v2 com câmera **fica arquivada e não deve ser utilizada no teste atual**. Para L2-01 sem câmera, a v1 já existe.
- L2-02 v1 tinha visão integrada indevidamente. Corrigida numa nova classe `AutonomoL202Flower8PollenSemCameraRPMv2.java` com OpMode `TESTE L2-02 V2 SEM CAMERA`, 715 linhas; removidos os sete imports de visão, campos e métodos VisionPortal/AprilTag e as quatro janelas de observação. O código não faz nenhuma inferência de TIP: telemetria `NAO VERIFICADO: SEM CAMERA`.
- Preservados: pose L2 provisória (1.574,3.420,180°), quatro tiros em L2, percurso Flower e coleta com MS desligado, retorno L2, quatro tiros extras, RPM e intertravamento de alimentação, PARK condicional a 4.00s de margem, LIMITE_TOTAL=29.30s.
- Projeto Meca Sim: `MecaSim_L2_02_v2_Flower8PollenSemCamera.json`, `source` corresponde byte a byte ao Java produzido.
- **Verificação técnica:** compilação javac --release 17 usando APENAS 9 stubs básicos FTC, excluindo vision, WebcamName e android.util. Inspeção textual sem referências de câmera e comparando pontos principais da estratégia. **Não houve simulação integral Meca Sim ou teste físico**. Testar a geometria e avaliar 4+4 lançamentos reais antes de aprovar.
- **Próximas versões L2-03 e outras:** SEM câmera até o usuário autorizar expressamente retomar visão. Não interpretar pulsos de feeder como TIP realizado.
