# FICHA DE ENSAIO DO AUTÔNOMO — MECA TIGER FTC #32578

**Copie esta ficha para cada execução física, inclusive as que falharem.** Não substituir uma medição ausente por número de simulação. Salvar com identificação única, por exemplo: ensaios/2026-10-13_3_5_giro_01.md.

## A. Identificação e rastreabilidade

| Campo | Preencher |
| --- | --- |
| Data e hora local | |
| Ensaio nº / responsável / mentor | |
| Versão do Autônomo (3.5 / 5.1 / outra) | |
| Fonte Java e SHA de commit/blob | |
| Branch ou tag | |
| FTC SDK / versão do app RC/DS | |
| Compilação associada (link de CI ou log local) | |
| Modo selecionado durante INIT | |
| Aliança e posição de partida | |
| Tipo: sinais / giro / percurso / lançador / visão / completo | |
| Condição da bateria / tensão antes e depois | |
| Piso, superfície e dimensões do campo | |
| Foto da montagem / link de vídeo / telemetry log | |

## B. Hardware e configuração em uso

| Campo | Medição ou confirmação |
| --- | --- |
| MD direito: porta, direção, encoder positivo? | |
| ME esquerdo: porta, direção, encoder positivo? | |
| MI: porta e sentido correto? | |
| MS: porta, sentido e RPM se houver sensor | |
| IMU: nome no hardwareMap e orientação física do Hub | |
| Yaw anti-horário cresce? | |
| Ticks/rev do motor | |
| Redução mecânica TOTAL até a roda | |
| Diâmetro e circunferência efetivos da roda | |
| FATOR_CALIBRACAO_DISTANCIA usado | |
| TICKS_POR_MM usado | |
| C270 nome, USB, resolução e estado STREAMING | |
| Camera: offset angular configurado | |
| Segurança: área isolada, STOP, supervisão | |

## C. Medição do movimento

| Métrica | Valor pedido | Valor real / telemetria | Erro |
| --- | --- | --- | --- |
| Giro inicial (graus) | | | |
| Giro final (graus) | | | |
| Distância do centro (mm) | | | |
| Desvio lateral final (mm) | | | |
| Yaw inicial (graus) | | | |
| Yaw final (graus) | | | |
| Ticks MD | | | |
| Ticks ME | | | |
| Diferença de ticks MD − ME | | | |
| Duração do giro 1 (s) | | | |
| Duração da translação (s) | | | |
| Duração da correção final (s) | | | |
| Duração do giro 2 (s) | | | |
| Duração do lançador (s) | | | |
| Tempo total do AUTO (s) | | | |

Equações de referência:
- Erro linear (mm) = distância_real − distância_comandada.
- Erro linear percentual = 100 × erro_linear / distância_comandada (quando distância_comandada ≠ 0).
- Erro angular deve ser normalizado ao intervalo -180° a +180°.
- Estimativa inicial do próximo fator = fator_atual × distância_comandada / distância_real, somente após confirmar transmissão e ausência de escorregamento anormal.

## D. Visão — preencher somente para as versões com C270

| Campo | Valor observado |
| --- | --- |
| Aliança selecionada (RED/BLUE) | |
| Lado preferido (SCORING/AUDIENCE) | |
| A confirmado antes do START? | |
| Nome de cada cluster detectado | |
| Cluster escolhido para mirar | |
| CELL pontuável (roll ±90°) conferida? | |
| Bearing inicial e final (graus) | |
| Range observado (polegadas) | |
| Yaw / roll visual (graus) | |
| Intervalo entre frames novos (ms) | |
| Perdas consecutivas de frame | |
| Houve timeout / fallback por IMU? | |
| Comando de giro diminuiu o bearing? | |
| Câmera aponta na mesma direção do MS? | |
| Offset necessário para alinhar a saída real | |

## E. Mecanismos e resultado

| Campo | Valor |
| --- | --- |
| Número de ciclos de alimentação comandados | |
| Número de peças fisicamente alimentadas | |
| Número de peças realmente lançadas | |
| Número de peças pontuadas | |
| Tempo de aquecimento MS (s) | |
| Tempo de alimentação MI por peça (s) | |
| Travamentos, queda de RPM, falhas de mecanismo | |
| Parada após STOP ou exceção observada? | |
| Motivo de falha do código / etapa | |

## F. Aceite e alteração de código

**Tolerâncias PREVIAMENTE definidas para este ensaio:**

- Erro máximo de distância permitido:
- Erro máximo de ângulo permitido:
- Desvio lateral máximo permitido:
- Tempo máximo para a etapa:
- Quantidade mínima de peças efetivamente lançadas:
- Critério de segurança:

**Resultado:** [ ] Aprovado dentro das tolerâncias  [ ] Reprovado  [ ] Inconclusivo / interrupção segura.

**Motivo objetivo:** 

**Hipóteses para o erro (marcar apenas após observar evidências):** [ ] configuração  [ ] IMU  [ ] encoder  [ ] transmissão  [ ] atrito  [ ] bateria  [ ] PID  [ ] câmera/latência  [ ] alimentação/lançador  [ ] outro.

**Mudança proposta:** qual arquivo, qual variável e qual valor antigo/novo, fundamentado por que medição?

**Teste de regressão que deverá ser repetido:**

**Versão/commit novo gerado após alteração (se aplicável):**

**Responsável pelo ensaio e revisão:**

---

**Importante:** relatório físico com os campos preenchidos **não** comprova que as outras versões foram testadas. Vincular cada ensaio a seu arquivo e SHA exato. Comparação entre 3.5 e 5.1 requer mesma posição inicial e redução efetivamente calibrada, e não apenas mesmas distâncias nominais no software.