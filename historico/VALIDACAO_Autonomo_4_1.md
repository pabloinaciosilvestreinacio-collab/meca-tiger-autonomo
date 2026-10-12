# Relatório de revisão e testes — Autônomo 4.1
Data: 2026-10-12
Branch: `revisao/autonomo-4.1-validacao`

## Versões e escopo
- Referência estável/ativa na main: `Autonomo/autonomo.java` (3.1), NÃO alterada.
- Candidato anterior: `Autonomo/Autonomo4.java` (4.0), NÃO alterado nesta revisão.
- Novo candidato: `Autonomo/Autonomo41.java` (4.1), com `@Autonomous(name="Autonomo 4.1")`.
- Diagnóstico independente: `Autonomo/DiagnosticoAprilTag.java`, sem controle de motor.
- Testes reproduzíveis: `tests/autonomo41_logic_tests.mjs`.

## Causa / mudança / risco
1. `getDetections()` pode repetir detecções antigas e validar falsamente estabilidade. O 4.1 lê apenas `getFreshDetections()`, disponível na API de visão FTC. Se não houver novas detecções, para de aplicar comando visual após 0,08 s, invalida confirmações após 0,18 s e faz fallback após 0,35 s de perda durante movimento. **Risco residual**: FPS insuficiente torna controle intermitente e pode aumentar timeouts; mensurar FPS real.
2. PID de ângulo usava a diferença linear entre erros `-179` e `+179`; isso gera derivada de ~358° em uma amostra. Derivada do 4.1 usa delta angular normalizado. **Risco residual**: ganhos de PID precisam de ajuste físico.
3. O 4.0 usa 3,61 × 5,23 aproximados; o 4.1 utiliza as razões de dentes publicadas pela REV: 76/21 × 68/13 = 18,9304029304. `REDUCAO_TRANSMISSAO_RODAS=1.0` é parâmetro explícito para engrenagens/correntes posteriores, caso existam. **Risco residual**: transmissão REAL ainda não aferida.
4. Acrescentado modo de diagnóstico de clusters da C270, sem movimentação, para verificar `shortName`, `bearing`, `roll`, `range` e estado `STREAMING` antes de operar o alinhamento integrado.

## Resultados realmente executados
Ambiente: Node.js 22.16.0 no contêiner Linux de testes; não utiliza dependências externas.
Executar: `node tests/autonomo41_logic_tests.mjs`.

### Assertions puras (7/7 aprovadas)
- normalização angular perto de ±180°, entradas não finitas;
- derivada angular sem salto em wrap;
- razão de dentes, ticks/mm e alvo de 2.000 mm;
- parada linear pela média de encoders, com giro final independente;
- tempo máximo de comando visual sem novo frame e perda de alvo;
- convenção de sinal positivo do bearing no modelo diferencial (hipótese, não medição);
- orçamento de tiro do AUTO, inclusive pior caso maior que 29 s.

### Simulações probabilísticas determinísticas (7.500 cenários executados)
Modelo **simplificado**, com planta angular contínua, controle discreto de 10 ms, constante de tempo e ganho angular variáveis e ruído de bearing. Não executa o FTC SDK, não utiliza frames reais e não prova confiabilidade física.

| Cenário | Configuração do modelo | Resultado |
|---|---|---|
| Nominal (5.000) | Ganho 95–205°/s a potência plena; tau 0,05–0,18 s; frames a cada 30–60 ms; perda 3–10% | 5.000 alinhados / 0 fallback |
| Stress (2.000) | Frames a cada 70 ms com 45% de perdas | 916 alinhados; 744 timeout; 336 alvo perdido; 4 nenhum alvo confirmado |
| Sinal invertido (500) | Inverte o sentido de resposta física do yaw | 41 alinhados; 459 timeout |

No cenário nominal, o erro verdadeiro residual dos alinhamentos bem-sucedidos não excedeu ~1,965°, para bearing medido com ruído de ±0,25° no modelo.

### Limite importante do tempo
Cada etapa tem timeout, mas a soma dos piores casos do código é:
`7 + 12 + 1,50 + 7 + 2,50 + 1,60 + 2,45 = 34,05 s`, maior que a janela segura de 29 s. Portanto, **a execução total com lançamento não é garantida por software ainda**. O limite global interrompe o programa, mas a estratégia precisa ser replanejada com tempos medidos e prioridades antes da competição.

## Níveis de evidência (skill ftc-engineer)
- Revisão estática: REALIZADA sobre o código e API/documentação.
- Testes automatizados puramente matemáticos: EXECUTADOS (7 assertions).
- Simulação de planta simplificada: EXECUTADA (7.500 cenários).
- Compilação FTC real `assembleDebug`: NÃO EXECUTADA; o repositório não contém um projeto SDK/Gradle completo e o ambiente de execução não contém o SDK Android.
- Teste com C270 e motores reais: NÃO EXECUTADO. Nenhum comando foi enviado ao robô.

## Pendências antes de liberar para jogo
- Confirmar relação de TODAS as engrenagens, diâmetro efetivo da roda e contagem real dos encoders.
- Confirmar direção física dos motores `MD`/`ME` e se aumento de yaw corresponde a giro anti-horário.
- Confirmar posição/orientação real do Control Hub, camera C270 voltada no mesmo sentido do lançamento, e offset angular.
- Rodar `DiagnosticoAprilTag` sem movimento para validar o cluster correto e FPS/frescor.
- Rodar build `:TeamCode:assembleDebug` com FTC SDK 12.0.
- Fazer testes curtos de baixa potência, medir oscilações e trajetos antes de liberar a sequência completa.
- Medir tempos reais e corrigir o orçamento da estratégia (giro, deslocamento, visão e lançamentos).
- Validar autonomia funcional do intake/lançador; tempos atuais são apenas constantes, sem detecção de peça/RPM.

## Referências oficiais consultadas
- FTC SDK 12.0: https://github.com/FIRST-Tech-Challenge/FtcRobotController
- Sample FTC 12 para AprilTags Tank: https://github.com/FIRST-Tech-Challenge/FtcRobotController/blob/master/FtcRobotController/src/main/java/org/firstinspires/ftc/robotcontroller/external/samples/RobotAutoDriveToAprilTagTank.java
- Javadoc `getFreshDetections`: https://javadoc.io/static/org.firstinspires.ftc/Vision/11.1.0/org/firstinspires/ftc/vision/apriltag/AprilTagProcessor.html
- REV razões exatas de engrenagem: https://docs.revrobotics.com/rev-crossover-products/ultraplanetary/cartridge-details
- AprilTag clusters BIOBUZZ: https://ftc-docs.firstinspires.org/en/latest/tech_tips/tech-tips/tech-tip-apriltag-clusters/tech-tip-apriltag-clusters.html

## Reversão
O 3.1 e o 4.0 não foram sobrescritos. Descartar a branch `revisao/autonomo-4.1-validacao` retorna ao comportamento `main` sem impacto no autônomo 3.1. Para usar o 4.1, copiar apenas `Autonomo41.java` para o projeto SDK real, após conclusão dos ensaios.
