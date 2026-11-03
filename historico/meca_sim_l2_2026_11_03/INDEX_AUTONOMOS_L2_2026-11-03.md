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
