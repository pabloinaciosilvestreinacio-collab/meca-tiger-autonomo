# RELATÓRIO TÉCNICO — Encerramento das famílias L1/L2 e início da POO

**Data:** 03/11/2026  
**Equipe:** Meca Tiger FTC #32578  
**Robô:** Mequinha — base diferencial 2WD  
**Ambiente:** Meca Sim, Java / FTC SDK 12.0  
**Escopo:** decisões de código, situações verificadas e planejamento de refatoração orientada a objetos.

## 1. Decisões e alterações consolidadas

**L1-01:** quatro POLLEN de L1 e PARK; concluído pelo operador no Meca Sim. O arquivo L1-01 preservado em GitHub é reconstrução da conversa, não cópia byte a byte comprovada do aprovado.

**L1-02:** quatro POLLEN em L1, coleta Garden e PARK; concluído no Meca Sim segundo o operador.

**L1-03:** quatro POLLEN L1, Garden, quatro em L2 e PARK; corrigida alimentação final de L2 e folgas do chassi; concluído no simulador segundo o operador. Referência para rotas com PARK.

**L1-04:** tentativa de acrescentar Flower e terceiro ciclo; correção da posição da Flower superior na v2. Experimental; não aprovado como autônomo completo.

**L1-05:** progressão desde Flower + retorno L2 + terceira sequência + PARK até a decisão v17 de **abandonar PARK** e permanecer parado em L2 após tentativa de 4+4+4, priorizando dois TIPs. A sequência sofreu problemas de disparo fora da posição (MI e MS simultâneos durante coleta), 3/4 ou 11/12 lançamentos, esperas de RPM, paradas entre curvas e prazo reservado ao PARK. Correções incluíram bloqueio do MS durante coleta, três amostras de RPM estável, curvas Bézier, telemetria de prazo e separação de estratégias. A v17 compilou, mas falta teste integral do Meca Sim e validação física. Pulso de alimentador não comprova ejeção nem TIP.

**L2-01 v1:** começa na posição L2 provisória, quatro lançamentos com RPM e PARK. Não possui câmera. Compilada com stubs FTC, não validada integralmente no Meca Sim.

**L2-01 v2:** candidata separada com câmera e todos os clusters AprilTag, fallback sem bloquear. **Arquivada, fora do teste atual; visão será estudada depois**. Nunca presumir que a detecção de clusters confirma pontuação oficial de TIP.

**L2-02 v2:** quatro lançamentos em L2, Flower com shooter desligado, retorno L2, mais quatro e PARK condicional. Removidas as APIs de visão que estavam na v1. Compilada com stubs FTC; execução integral no simulador pendente.

**L2-03 v1 Garden:** **descartado por decisão do operador** porque a travessia até Garden a partir de L2 aumentaria muito o tempo e complexidade para uma estratégia de oito lançamentos. Java e JSON retirados da branch principal do repositório GitHub e enviados à lixeira da Biblioteca; não será usado como base da POO. O histórico Git mantém commits anteriores, sem reescrita destrutiva.

## 2. O que motivou mudanças nos testes de vídeo (histórico da família L1-05)

- **`teste.mp4` (três execuções):** um relatório em 29,34 s, outro em ~29,75 s com 9 lançamentos/9 acertos registrados, terceiro sem relatório final. Motivou redução da parada Garden → curva L2. O nome salvo no seletor não identificava inequivocamente a versão compilada.
- **`teste2.mp4`:** aproximadamente 29,57 s e 11/12 elementos lançados/11 acertos; motivou diagnóstico da janela do quarto POLLEN.
- **`teste3.mp4`:** cerca de 29,57 s, 11 lançados e 8 coletados; relatório não avaliou a missão e registrou 0 TIPs, sem comprovação de pontuação. Motivou examinar corte de disparo causado pela reserva do PARK.
- **`teste4.mp4`:** ensaio encerrado aos 6,04 s e outro aos 29,35 s, com código compatível com a família v13. Sem dados suficientes para atribuir a causa da interrupção ou validar PARK e número de disparos. Reforçou a necessidade de preservar fontes e comparar apenas uma variável por teste.

Os vídeos acima foram descritos a partir do histórico de análises já documentado; este relatório **não constitui nova reanálise direta das gravações**.

## 3. Status de encerramento

| Estratégia | Situação funcional conhecida | Pendência principal |
|---|---|---|
| L1-01 | Operador concluiu no Meca Sim | Original autoritativo e testes reais |
| L1-02 | Operador concluiu no Meca Sim | Testes físicos |
| L1-03 | Operador concluiu no Meca Sim | Testes físicos, calibração RPM/folgas |
| L1-05 v17 | Compilada, 4+4+4 sem PARK | Teste Meca Sim integral, TIP e testes reais |
| L2-01 v1 | Compilada, 4 tiros e PARK, sem visão | Validar largada e rota no Meca Sim/robô |
| L2-02 v2 | Compilada, Flower 4+4 e PARK condicional | Validar coleta/tempo/PARK no Meca Sim/robô |
| L2-03 | Excluída da branch atual e Biblioteca | Não usar |

O desenvolvimento de **novas rotas** é encerrado por decisão do operador. Isso **não significa que todas as estratégias estejam validadas no Meca Sim**. As verificações de câmera/TIP e testes físicos permanecem como tarefas próprias.

## 4. Início da decomposição POO — etapa 01 efetivamente criada

Arquivos novos e **sem substituição de código competitivo**:

1. `ParametrosMequinha.java`: dimensões, ticks por metro e posições básicas de servo.
2. `PoseMequinha.java`: estado de posição e orientação, imutável.
3. `HardwareMequinha.java`: construção e parametrização dos motores/servo/IMU com parada segura.
4. `OdometriaDiferencial.java`: integra variação dos encoders e yaw da IMU, seguindo a convenção original.
5. `DiagnosticoPOOMequinhaV1.java`: demonstra integração do hardware e odometria em OpMode **inerte**, sem deslocar/lançar nem usar câmera.

**Benefício:** os códigos L1/L2 deixarão de repetir configuração de motores, normalização angular e integração de encoders. As estratégias futuras poderão escolher movimentos e número de lançamentos por composição dos componentes, reduzindo duplicação e facilitando testes unitários.

**Não alterado:** nenhuma rotina competitiva aprovada, nenhum tempo de tiro, potência, curva, pose de largada ou critério de TIP. Os arquivos originais continuam sendo a referência de comparação.

**Teste realizado:** `javac --release 17` com stubs locais de FTC e testes de conversão dos encoders, deslocamento e estados seguros. **Não** é validação em SDK real nem execução integral do Meca Sim.

## 5. Próximas extrações POO, em ordem

1. `ControleShooter`: encoder, medição de RPM, estabilidade em três janelas, segurança MI/MS, pré-alimentação e pulsos; parâmetros separados para Meca Sim versus calibração física.
2. `NavegacaoDiferencial`: correção de rumo, limites de parede, curvas Bézier, stop/reserva temporal e watchdog.
3. `ControleColeta`: mecanismos comuns MI/MS e rotas próprias Garden/Flower; sem disparos acidentais durante coleta.
4. `Estrategias`: classes L1-01/02/03/05 e L2-01/02 contendo principalmente sequenciamento das fases, sem duplicar implementação mecânica.
5. `VisaoTip`: módulo opcional, **somente depois** dos testes da Logitech C270 e API de AprilTags autorizados pelo operador.

## 6. Critérios para aceitar uma refatoração

- Compilar no FTC SDK real; não apenas nos stubs de teste.
- Não alterar os motores/rumos nem a trajetória dos OpModes originais sem pedido explícito.
- Executar comparação A/B com a classe congelada no mesmo mapa/pose, registrando tempo, pose, eventos de comando, POLLEN visivelmente ejetados e posição final.
- Em falha, não mover o robô com posição desconhecida; desligar tração e mecanismos.
- Manter arquivos originais e histórico, preservando possibilidade de reversão.

## Conclusão

A fase de **criação de estratégias L1/L2** foi encerrada, com a L2-03 excluída e a verificação automática de TIP por câmera adiada. Iniciamos a **POO modular** por hardware, geometria e odometria. Os módulos já existem e passaram na compilação e testes locais; a integração progressiva aos OpModes e o teste físico ainda não foram realizados.
