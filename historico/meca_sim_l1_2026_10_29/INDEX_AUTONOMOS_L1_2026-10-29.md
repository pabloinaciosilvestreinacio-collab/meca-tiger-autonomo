# Mequinha — Estratégias L1, 29/10/2026

Equipe Meca Tiger FTC #32578. Arena BIOBUZZ, ambiente Meca Sim.

## L1-01 — concluído pelo operador no Meca Sim

- Fluxo: 4 POLLEN L1 → PARK.
- Classe usada em conversa: `AutonomoL101V11`.
- Pose de referência L1-01: (1.678 m, 0.399 m), yaw=0°.
- PARK L1-01 (versão originalmente descrita): (0.350 m, 2.820 m).
- Arquivo disponibilizado neste histórico: `AutonomoL101V11.java` é uma **reconstrução da conversa**, compilável, **não é uma cópia byte a byte verificada do arquivo aprovado**. Preservar essa distinção e substituir somente quando o original for recuperado.

## L1-02 — concluído pelo operador no Meca Sim

- Fluxo: 4 POLLEN L1 → Garden → PARK.
- Classe: `AutonomoL102GardenParkRPM`.
- O arquivo corresponde ao Java integral gerado no processo de desenvolvimento.
- Pose (1.530, 0.255), yaw=0°; PARK (0.255, 2.380).

## L1-03 — concluído pelo operador no Meca Sim

- Fluxo: 4 POLLEN em L1 → Garden guiado → 4 POLLEN em L2 → PARK.
- Classe: `AutonomoL103GardenGuiadoPark4L2RPM`.
- Arquivo integral final da conversa, corrigido para o quarto pulso de alimentação em L2.
- Pose (1.530, 0.255), yaw=0°; PARK (0.255, 2.380).

## L1-04 — EXPERIMENTAL, NÃO APROVADO

- Nova classe independente: `AutonomoL104GardenL2FlowerParkRPM`.
- Fluxo: L1-03 até lançar 4 em L2 → contornar o HIVE pelo corredor esquerdo → coletar na Flower superior no sentido da seta → terceiro disparo de 4 POLLEN → PARK marcado.
- Rota e terceiro lançamento ainda não validados no Meca Sim; risco significativo de exceder 29,3 segundos. A fonte do layout da Flower é a imagem marcada enviada pelo operador, não uma medição física.
- O código não tem sensor de presença/contagem de POLLEN; quatro pulsos de MI/SA não provam quatro bolas coletadas ou lançadas.

## L1-04 v2 — Flower superior corrigida, experimental

- Classe: `AutonomoL104FlowerSuperiorRPMv2`.
- Corrigido o erro de eixo Y: Flower no topo (Y aproximadamente 3,398 m), em vez da parede inferior.
- Depois de L2, sai da parede, coleta avançando para +Y, retorna em ré, tenta novo lançamento de 4 POLLEN e PARK. Mantido como histórico independente.
- Status: versão experimental; não substituir L1-01, L1-02 ou L1-03.

## L1-05 — Flower superior → RETORNO obrigatório a L2 → 4 lançamentos → PARK

- Classe: `AutonomoL105FlowerRetornaL2RPM`.
- Fluxo: 4 L1 → coleta Garden → 4 L2 → coleta Flower superior → regressa à faixa L2, aproxima-se novamente da parede com verificação de folga → 4 novos pulsos com RPM estável → saída reta → PARK (X=0,255; Y=2,380 m).
- Otimização de potência em trechos livres: corredor sob HIVE 0,85 → 0,94; travessia lateral da Flower 0,53 → 0,73; retorno lateral 0,64 → 0,77; PARK livre 0,88 → 0,95. Aproximação final da parede e Garden mantidas lentas.
- Regra de tiro: 3 amostras consecutivas de RPM na faixa; sem liberar alimentação se RPM insuficiente; não omitir silenciosamente o terceiro posto.
- Compilação Java de estrutura verificada com stubs; **não equivale a build integral FTC SDK nem a teste do simulador ou robô físico**.
- 29,3 segundos pode ser insuficiente para 12 disparos + duas coletas + PARK. Registrar tempo e falhas reais antes de aprovar.
- Fonte congelada; SHA-256 do arquivo local: `c6591e566f8f389508b89ccf50b53ba5b4b3e9496ff07b94de217488f8d8d13f` (confirmar equivalência de conteúdo antes de usar este hash como hash do blob GitHub).

## Hardware e limites de validação

2WD diferencial; ME, MD, MI, MS, SA e imu; SDK FTC 12.0; roda Ø90 mm; 560 ticks/revolução da roda de locomoção; bitola aprox. 45 cm. `RPM_ALVO_MS_SIM=127` é parâmetro do simulador, **não um RPM físico calibrado**. Potência física 0.60 acertou alvo empiricamente, mas o RPM correspondente ainda precisa ser medido no encoder do MS.

**Política de preservação:** Não substituir classes antigas e não misturar código aprovado no simulador com código validado no robô físico. Aguardar análise de logs/vídeos de L1-04 antes de elevá-lo a aprovado.