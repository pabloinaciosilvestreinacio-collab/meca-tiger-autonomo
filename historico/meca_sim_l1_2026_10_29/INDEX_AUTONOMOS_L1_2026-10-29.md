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

## Hardware e limites de validação

2WD diferencial; ME, MD, MI, MS, SA e imu; SDK FTC 12.0; roda Ø90 mm; 560 ticks/revolução da roda de locomoção; bitola aprox. 45 cm. `RPM_ALVO_MS_SIM=127` é parâmetro do simulador, **não um RPM físico calibrado**. Potência física 0.60 acertou alvo empiricamente, mas o RPM correspondente ainda precisa ser medido no encoder do MS.

**Política de preservação:** Não substituir classes antigas e não misturar código aprovado no simulador com código validado no robô físico. Aguardar análise de logs/vídeos de L1-04 antes de elevá-lo a aprovado.