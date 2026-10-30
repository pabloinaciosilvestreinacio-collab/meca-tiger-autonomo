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

## L1-05 v7 — intertravamento contra disparo fora de L2 (30/10/2026)

- Relato: na segunda execução do vídeo 30/10/2026, o robô solta POLLEN fora de uma posição de lançamento. Atenção: na interface do vídeo, o projeto selecionado aparece como `AutonomoL105FlowerCurvasL2ParkRPMv5`; a correção foi implementada **sobre v6**, arquivo mais recente fornecido no histórico.
- Causa encontrada na fonte v6: MI=1.00 durante retorno Garden e durante Flower, simultaneamente com MS~0.60, condição que permite liberação automática de peças no Meca Sim, independentemente do servo. A cadência não era a causa primária desse disparo fora de posição.
- Correção v7: estado explícito do mecanismo (coleta, trânsito, três postos de disparo, PARK); MS=0.0 durante coleta Garden/Flower; retorno da coleta com MI ativo e MS desligado; MS liga **após MI=0** e acelera durante o deslocamento a L2.
- Alimentação: antes de CADA tiro, testar pose no posto L1 ou L2, MD/ME com power zero e três medições recentes consecutivas de RPM na faixa. Em caso negativo, não alimentar; telemetria registra bloqueio.
- Watchdog redundante corta MS se detectar MI>0.50 e MS>0.50 fora da alimentação autorizada. A proteção se baseia em comandos e odometria estimada, NÃO em sensores de POLLEN ou confirmação independente do alvo.
- Rotas mantidas: Garden guiado, Garden→L2 Bézier, acesso Flower em curva contínua, Flower→L2 Bézier e PARK final X=0.255/Y=2.380; potências, intervalos 0.84 s e limite global 29.3 s da v6 preservados.
- Validação: compilação `javac --release 17` com stubs FTC, teste unitário simulado de intertravamento (MI/MS e pose L1), comparação estática de todas as coordenadas e três curvas entre v6/v7. **Não houve teste da v7 no Meca Sim ou no robô real**.
- Risco temporal: MS deixa de preaquecer durante a coleta; isso pode acrescentar spin-up, embora o motor volte a girar em trânsito. A viabilidade de 12 POLLEN + PARK em 29.3s continua não demonstrada. Priorizar integridade da coleta e da pose.
- Código preservado em `AutonomoL105FlowerCurvasL2ParkRPMv7.java`. Todas as versões v1-v6 permanecem preservadas para regressão.


## Ensaio de tolerância angular — L1-05 v7 (30/10/2026)

- **Ambiente:** Meca Sim; observação reportada pelo operador. Não equivale a validação no robô físico.
- **Referência:** `AutonomoL105FlowerCurvasL2ParkRPMv7.java`, preservada no GitHub com `TOL_GRAUS = 1.5` (linha 126 na fonte de referência).
- **Variável testada:** alteração manual proposta de `TOL_GRAUS` de `1.5` para `2.0` graus, mantendo os demais parâmetros.
- **Resultado informado:** **não houve mudança perceptível**. O teste anterior foi descrito com conclusão em cerca de **29,34 s**; o operador relatou que alterar a tolerância não melhorou o resultado. **Não foi fornecido novo tempo cronometrado independente**, logo não atribuir ganho zero exato em milissegundos.
- **Interpretação:** o giro estacionário governado por `TOL_GRAUS` não se mostrou gargalo relevante neste ensaio; curvas Bézier e a aproximação de lançamento possuem tolerâncias distintas. Este teste não justifica flexibilizar a tolerância de pose de L2 nem de RPM.
- **Decisão:** **não incorporar** a alteração à versão de referência. Manter a v7 com `TOL_GRAUS = 1.5`; encerrar esta hipótese de otimização até aparecer telemetria por fase que indique gasto relevante em giros.
- **Contexto anterior relatado:** em execução recente da v7, o operador observou todos os lançamentos e término por volta de 29,34 s. A contagem foi visual, não confirmada por sensor de ejeção. PARK e total de 12 devem continuar sendo conferidos por execução.


## Ensaio seguinte — tolerância angular 2,5° L1-05 v8 (30/10/2026)

- **Pedido:** testar uma tolerância angular maior depois de não observar ganho perceptível de 1,5° para 2,0°.
- **Candidato:** `AutonomoL105FlowerCurvasL2ParkRPMv8.java`.
- **Única alteração funcional frente à v7:** `TOL_GRAUS = 1.5` → `2.5` (método de giro estacionário); nome de classe, nome do OpMode e comentários também atualizados.
- **Permanecem iguais:** tolerâncias de pose em L1/L2 e curvas Bézier, amostras estáveis de RPM, intertravamento MI/MS, alimentação, waypoints, velocidades e PARK.
- **Validação técnica:** compilação Java 17 com stubs FTC e conferência textual do diff; **ainda sem execução no Meca Sim** e sem validação física.
- **Comparação desejada:** executar mesmo cenário e observar tempo total, quarto POLLEN após Flower, contagem de TIPs, posição PARK e eventuais movimentos inesperados. Se não houver ganho, não alterar novamente a tolerância indiscriminadamente.
- **Referência preservada:** v7 imutável em arquivo próprio. Versão anterior de tolerância 2,0° sem melhora fica registrada acima.

## Teste L1-05 v9Tempo — ampliar limite interno (30/10/2026)

- Objetivo solicitado: testar **tolerância de tempo** maior, após o ensaio angular de `TOL_GRAUS = 1.5 -> 2.0` sem melhora perceptível.
- Baseline: **L1-05 v7** inalterada, pois já houve relato de 4+4+4 com conclusão aproximadamente em 29.34 s no Meca Sim.
- Mudança funcional isolada: `LIM_GLOBAL = 29.3` -> `29.6` segundos (linha 95 da classe-base). Margem nominal passa de 0.7 para 0.4 s até 30 s. Os prazos derivados de `LIM_GLOBAL` também aumentam 0.3 s.
- Constantes de tolerância angular, pose L2, RPM, alimentação, sequência e curvas **não mudaram**. `TOL_GRAUS` permanece 1.5°.
- Código de ensaio: `AutonomoL105FlowerCurvasL2ParkRPMv9Tempo.java` (nome próprio de OpMode). Um arquivo `v8` já existia no repositório com outro experimento de tolerância; preservado sem alterações para não misturar hipóteses.
- Verificação: comparação contra v7 e compilação `javac --release 17` com stubs FTC; o código publicado e o arquivo local foram verificados pelo mesmo Git blob SHA `46be7c13224ef27a421f852322ba716ec76e4328`.
- **Resultado do ensaio: pendente.** Não presumir ganho de tempo; ampliar prazo pode atrasar começo do PARK. A execução física FTC não foi validada. Verificar início e fim do PARK, 4+4+4 POLLEN e resultado até os 30 s.

## Análise de três execuções do Meca Sim e L1-05 v10 (30/10/2026)

**Vídeo de origem:** `teste.mp4` enviado pelo operador. O vídeo mostra três execuções em sequência; nem todos os relatórios finais são visíveis. O seletor de projeto aparece como `AutonomoL105FlowerCurvasL2ParkRPMv6` na gravação, embora o editor indique 2374 linhas; portanto, sem export do código/projeto não afirmar que cada uma das três execuções usou exatamente a v7, v8 ou v9.

- **Ensaio 1:** relatório final visível com duração **29,34 s**, missão **não avaliada pelo simulador**, estimativa de **6 pontos**, HIVE/TIPs simulados 0 naquele quadro. Conclusão de Java não prova pontuação.
- **Ensaio 2:** relatório visível com duração aproximadamente **29,75 s**; a área de diagnóstico mostrada na gravação registra **9 elementos lançados e 9 acertos no HIVE**, sem confirmação de TIP contabilizado. Registrar como leitura daquele relatório, não garantia de 4+4+4.
- **Ensaio 3:** vídeo termina com o robô ainda em movimento, sem relatório final integral visível; duração e pontuação final **não verificadas**.
- **Diagnóstico de código:** na classe de referência v7, `percorrerGardenGuiado(true)` trata o retorno até X=1,420 como meta de parada: condição a 13 mm chama `parar()`; em seguida `seguirBezier()` começa nova rampa a potência 0,29, após o comando de desligar MI e ativar MS. Portanto, já havia Bézier contínua **após** o Garden, mas havia frenagem/STOP na junção.
- **Teste proposto em arquivo separado:** `AutonomoL105GardenL2SemParadaRPMv10.java`, derivada da v7 de 29,3 s, para isolar **somente** o efeito da junção do retorno Garden→Bézier L2.
- **Mudanças funcionais v10:** gatilho de passagem antecipado a 45 mm no retorno Garden (ida de coleta mantém 13 mm e STOP obrigatório para inverter sentido); suprimir `parar()` nesse retorno; piso de distância de frenagem 0,115 m, equivalente a velocidade de transferência não nula; rampa inicial Bézier Garden→L2 0,45 em vez de 0,29. Mantidos os dois segmentos geométricos em **um movimento sem parada**; não substituir arbitrariamente por spline única, pois a coleta exige manter o robô paralelo à parede inferior.
- **Sem alteração:** RPM/MI/MS intertravamento, pulsos do lançador, curvas Flower→L2 e L2→PARK, coordenadas L2/PARK e `LIM_GLOBAL=29.3`. A v9Tempo (29,6 s) continua independente e **não foi misturada** na v10.
- **Validação efetuada:** compilação Java 17 com stubs FTC, análise estática do diff e avaliação geométrica da curva teórica para chassi quadrado de 45 cm, com folga teórica mínima aproximada de 15 mm no ponto final L2. **Não houve teste completo no Meca Sim, nem validação de segurança física.** O seguidor por IMU/encoders e derrapagem podem afetar a transição.
- **Próximo ensaio A/B:** executar v7 e v10 com a mesma pose inicial/configuração e, se possível, exportar o CSV de trajetória e telemetria. Comparar duração Garden→primeiro L2, velocidade mínima na junção, desvios X/Y e yaw, 4+4+4 alimentações e PARK. Caso oscile, reverter integralmente à v7, não aumentar potência.
- **Fonte congelada v10 SHA Git blob:** `32b6c4c2c0201fa9535e1766ed1ea5f571ffa95b`.

## L1-05 v11 — teste de tolerância de chegada a L2 (30/10/2026)

- **Motivação:** operador solicitou maior tolerância em L2 após experimento v10 para eliminar freada Garden→L2.
- **Base:** `AutonomoL105GardenL2SemParadaRPMv10.java`, conservada integralmente.
- **Mudança funcional isolada:** nas **duas** chamadas do seguidor `seguirBezier` que terminam em L2, aumentar `toleranciaFinal` **0.018 m (18 mm) → 0.025 m (25 mm)**. Primeiro L2 após Garden e segundo L2 após Flower. Os demais trechos não sofrem alteração.
- **Segurança preservada:** `confirmarPoseL2()` e `validarPoseDisparo()` permanecem com erro X≤0.055 m, erro Y≤0.045 m, yaw≤2.5° e folga superior entre 0.008 e 0.046 m; exige robô parado, RPM estável e alimentador intertravado. Os limites de prevenção de colisão por chassi continuam idênticos.
- **Sem mudança:** `LIM_GLOBAL=29.3 s` (não incorporar o experimento separado `v9Tempo`), tolerância de giros `TOL_GRAUS=1.5°`, forma e potência das curvas, intake, shooter, pulsos e PARK.
- **Cuidado:** tolerância espacial maior pode encerrar o movimento alguns milímetros antes; **não** significa que disparo possa ocorrer fora da posição. A geometria útil de lançamento e os dois TIPs precisam ser reconfirmados.
- **Validação até aqui:** compilação `javac --release 17` com stubs FTC, diff inspecionado e arquivo GitHub verificado com blob `31c6efdf7f7d10feca7c89a86106a4b0f887a945`. **Sem execução v11 no Meca Sim; ganho de tempo desconhecido.**
- **Teste A/B:** v10 versus v11 com mesma pose inicial e condições; comparar tempo final, tempo até cada L2, 4+4+4 POLLEN, pose em L2 e PARK. Reverter v11 se deslocar o lançamento ou perder pontuação.
