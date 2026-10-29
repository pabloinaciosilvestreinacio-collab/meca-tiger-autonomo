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
## L1-05 v2 — Correção da volta a L2 e do PARK (29/10/2026)

- Classe independente: `AutonomoL105FlowerRetornaL2RPMv2.java`; `AutonomoL105FlowerRetornaL2RPM.java` foi preservada.
- Defeito na v1: `segmento()` alcançava Y=3.420 (folga superior de 15 mm) e **depois** chamava `aproximarL2()`, que rejeita folga inicial inferior a 28 mm; isso abortava o terceiro disparo e o PARK.
- Correção: retornar a Y=3.340 (folga de aproximadamente 95 mm com yaw=180), executar a aproximação fina a L2, tentar 4 disparos extras somente com RPM estável e seguir para PARK X=0.255/Y=2.380.
- Se o terceiro ciclo não completar por timeout/RPM e ainda houver tempo, tentar PARK e registrar o resultado como parcial, sem alegar 4+4+4.
- Verificação: compilação Java com stubs concluída; teste Meca Sim e validação física **pendentes**.

## L1-05 v3 — curvas contínuas e lançamento adicional (29/10/2026)

- Arquivo: `AutonomoL105FlowerCurvasL2ParkRPMv3.java`.
- Motivação: vídeo do Meca Sim e imagem com curva azul e setas vermelhas mostram paradas de giro/alinhamento na ida para L2; no segundo L2 faltaram disparos e PARK.
- Garden → L2: substituídos dois `segmento()` e `aproximarL2()` por Bézier cúbica **em marcha ré**, alvo L2 X=1,574 / Y=3,420 / yaw=180; reduz paradas intermediárias.
- Flower → L2: após coleta e recuo, gira **uma vez em zona livre** (yaw=128°) e entra na curva de ré até L2 sem passar por um waypoint de parada em Y=3,340.
- Shooter: mantém `MS_SHOT` durante Flower com `MI=0` e servo fechado; alimentação extra em L2 continua condicionada a três janelas consecutivas de RPM dentro da tolerância e a prazo.
- PARK: Bézier de avanço que sai de L2 e curva para a área vermelha X=0,255 / Y=2,380 / yaw=90, sem giros intermediários; redução de potência perto das paredes.
- Validada **compilação sintática Java com stubs** e **simulação cinemática simplificada** das três curvas; **não** validado no Meca Sim ou no robô físico. Ciclo 4+4+4 e PARK em 29,3 segundos ainda não confirmado; contadores de comandos de alimentação não comprovam acertos físicos.
- Versões anteriores L1-01, L1-02, L1-03, L1-04, L1-05 v1 e v2 preservadas sem modificação.

## L1-05 v4 — terceira sequência de disparos desbloqueada (29/10/2026)

- Classe separada: `AutonomoL105FlowerCurvasL2ParkRPMv4.java`.
- Relato operador v3: percurso, coleta Flower e PARK funcionaram, mas não houve terceiro ciclo de lançamento.
- Causa provável isolada no código: prazo do 3º ciclo terminava em `LIM_GLOBAL - RESERVA_PARK` = `29.3 - 4.2 = 25.1s`. Se o retorno a L2 se completasse após esse instante, `aguardarRpmEIntervalo()` não executaria o loop, resultando em 0/4 pulsos.
- Ajuste v4: reserva experimental PARK = 2.35 s (prazo do terceiro ciclo = 26.95 s); intervalo mínimo **somente** do terceiro ciclo = 0.44 s, ainda condicionado a três janelas estáveis de 80 ms por disparo.
- Novos diagnósticos: instante da chegada a L2, prazo de tiro, primeiro e último pulso, motivo de bloqueio.
- Rotas/curvas e coordenadas de v3 mantidas, assim como todos os L1 anteriores. Compilação Java **com stubs** concluída, teste Meca Sim pendente. Não garantir 12 POLLEN + PARK sem tempo real/feedback da simulação.

## L1-05 v5 — otimizacao de tempo no Meca Sim (29/10/2026)

- Classe independente: `AutonomoL105FlowerCurvasL2ParkRPMv5.java`, v4 intacta.
- RPM: monitoramento a cada ~80 ms integrado nos loops de movimento e esperas. Historico de tres amostras estaveis e reaproveitado ao chegar a L2; a estabilidade e reiniciada apos cada pulso alimentador. Encoders do shooter supervisionados por watchdog. `RPM_ALVO_MS_SIM=127` e TICKS_POR_VOLTA_MS=560 sao **parametros exclusivos da simulacao**, nao calibrados fisicamente.
- Acesso a Flower: substitui saida reta L2 + segmento lateral + giro 0 graus por uma curva continua dedicada, que termina apontando +Y em (X=1.30, Y=3.06). Os demais seguidores de curva ja testados permanecem com a geometria da v4; unico acréscimo e amostragem de RPM e velocidade.
- Prazo do 3o L2: reserva de PARK dinamica na faixa 2.10..3.10 s baseada em velocidade livre estimada do corredor, com fallback 0.82 m/s. E **estimativa inicial**, nao substitui cronometro no simulador ou margem validada.
- Telemetria: duracao de seis fases (L1, Garden, L2 #1, Flower, L2 #2, PARK), velocidade reta estimada, reserva PARK, RPM/estado do controlador e pulsos por posto.
- Verificacao: `javac` com stubs FTC e modelo cinemático simplificado para a entrada Flower; **sem execução real no Meca Sim e sem teste fisico**. O controle de RPM exige tres amostras antes de cada disparo, mas `pulsos` nao comprovam POLLEN ejetado.
- Regra: preservar codigo v4 para regressao e nao declarar L1-05 finalizado ate 4+4+4 lançamentos e PARK serem comprovados.

## L1-05 v6 — investigar 3/4 nos postos L2 e Flower (29/10/2026)

- Relato do operador na v5: L1 lançou 4; primeiro L2 lançou 3; retorno a L2 após Flower lançou 3. **Não foi disponibilizada telemetria de estoque ou ejeção**, então o problema pode ser cadência/feeder, coleta incompleta ou prazo.
- Nova classe independente: `AutonomoL105FlowerCurvasL2ParkRPMv6.java`.
- Alimentação: L1 mantém intervalo 0.82 s; L2 aumenta 0.70 → 0.84 s; terceiro posto aumenta 0.44 → 0.84 s. Valores experimentais, baseados na diferença entre L1 com quatro e postos com três; não há confirmação do cooldown interno do simulador.
- Pulso #4 L2/Flower: aberto 0.25 → 0.32 s, pré-alimentação 0.07 → 0.09 s, esvaziamento 0.12 → 0.16 s. Exigir 3 janelas de RPM estável antes de cada disparo.
- Coleta: MI fica ligado durante a ida e o recuo do Garden e da Flower para reduzir a chance de uma peça ficar na entrada; **sem alterar nenhum waypoint/curva**.
- Telemetria: horário de comando do último feeder, intervalos reais entre pulsos de L2 e Flower, contadores de pulsos separados. Nenhum contador significa detecção física de POLLEN.
- Contras: intervalo maior pode consumir tempo de PARK; avaliar tempo final no Meca Sim. Sem confirmação de que saiu 4+4+4.
- Validação realizada: `javac` com stubs e verificação estática das curvas, destinos e sequência. **Ainda não executado no Meca Sim.** v5 congelada e preservada.
