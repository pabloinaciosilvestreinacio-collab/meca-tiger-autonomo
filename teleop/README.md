# Histórico de engenharia — TeleOp 1.0 e 1.1

**Primeira etapa:** 12/10/2026  
**Branch original:** `desenvolvimento/teleop-1x`; integrado à linha de reorganização  
**Estado:** documentação histórica das versões manuais, compiladas e **não homologadas fisicamente**.

> **Para os controles atuais 1.2/1.3 e o desenho dos dois gamepads, consulte o [Manual Completo do TeleOp](MANUAL_COMPLETO.md).** Este arquivo preserva o contrato e a evolução originais da 1.0/1.1.

## Fonte e limitação importante

O arquivo mais recente da classe `robo` / `TeleOpAssistido`, que a equipe usou no Android Studio, **não estava no repositório GitHub auditado**. Portanto as classes 1.0/1.1 são uma **reconstrução nova conforme os comandos já acordados**, não uma cópia literal do código instalado no robô. Quando esse código for fornecido, deverá ser salvo como uma versão-base histórica ANTES de incorporar diferenças.

Esta branch não altera `Autonomo/autonomo.java`, nenhuma classe autonôma e nem a versão da câmera.

## Versões preservadas

| Versão | Arquivo | Diferença |
| --- | --- | --- |
| 1.0 | [TeleOp10.java](../TeamCode/src/main/java/org/firstinspires/ftc/teamcode/teleOp/TeleOp10.java) | Apenas MD/ME, joystick esquerdo Y para movimento e direito X para giro, dead zone 0,08, potência limitada a 0,35 |
| 1.1 | [TeleOp11.java](../TeamCode/src/main/java/org/firstinspires/ftc/teamcode/teleOp/TeleOp11.java) | Modos 35%/70%/100%, curva quadrática alternável por START, normalização proporcional e rampa de aceleração/frenagem sem inversão instantânea |

Cópias históricas completas em `historico_teleop/TeleOp_1_0.txt` e `historico_teleop/TeleOp_1_1.txt`. Classes têm nomes/OpModes distintos, então não sobrescrevem `robo`.

## 1. Contrato de controles

| Controle | TeleOp 1.0 | TeleOp 1.1 |
| --- | --- | --- |
| Gamepad1 joystick esquerdo Y | Frente/trás | Frente/trás |
| Gamepad1 joystick direito X | Giro diferencial conforme fórmula histórica | Mesmo giro |
| LB | Sem uso | Precisão, velocidade máx. 0,35 |
| RB | Sem uso | Turbo, velocidade máx. 1,00 |
| Sem LB/RB | 0,35 fixo | Normal, velocidade máx. 0,70 |
| LB e RB simultâneos | Sem uso | **Precisão tem prioridade** |
| START | Sem uso | Alterna curva quadrática; inicia ATIVADA |
| BACK | Sem uso | **Reservado** à futura assistência IMU, NÃO implementada |
| Gamepad2 / MI / MS | Não controlados | Não controlados |

Na versão 1.1, não há motor de coleta/lançamento mapeado. **Não pressupor que isso pare mecanismos energizados por outro sistema:** STOP o OpMode anterior e verificar potência dos mecanismos antes de iniciar um ensaio.

## 2. Mistura MD/ME — não alterar o sinal por intuição

A solicitação histórica da equipe foi:

```java
MD = -gamepad1.right_stick_x - gamepad1.left_stick_y;
ME = -gamepad1.right_stick_x + gamepad1.left_stick_y;
```

Essa convenção foi **preservada** em 1.0 e 1.1. As direções configuradas são `MD=REVERSE` e `ME=FORWARD`, como nos arquivos de referência, mas o efeito **físico** resultante ainda precisa ser medido. **O código é uma função de joystick/potência; sem conhecer montagem e polaridade real não é possível afirmar o sentido de giro.**

Exemplos de sinais de saída **ANTES da direção física do motor**, usando 1.0 em potência máxima 0,35 e sem combinações:

| Entrada | Potência MD | Potência ME |
| --- | ---: | ---: |
| Y = -1 (joystick à frente), X = 0 | +0,35 | -0,35 |
| Y = +1 (joystick para trás), X = 0 | -0,35 | +0,35 |
| Y = 0, X = +1 | -0,35 | -0,35 |
| Y = 0, X = -1 | +0,35 | +0,35 |

Na 1.1 os dois comandos são divididos pelo mesmo divisor `max(1, |MD|, |ME|)`, preservando a proporção ao acionar dois eixos juntos. A curva quadrática com sinal `sign(x) * x²` aumenta controle fino próximo do centro sem inverter o joystick. A dead zone rejeita `|x| <= 0,08` e também rejeita entradas não finitas.

## 3. Rampa temporal da 1.1

- `RAMPA_ACELERACAO = 2,0` unidades normalizadas de potência por segundo.
- `RAMPA_FRENAGEM = 4,0` unidades por segundo.
- Tempo `dt` limitado a 0–0,10 s.
- Se o motor estava positivo e o alvo ficou negativo (ou vice-versa), passa **primeiro por zero**, sem transição direta num único loop.
- STOP chama `setPower(0)` em MD e ME via bloco `finally`, sem aplicar rampa na parada de emergência.

**Atenção:** uma rampa de parada pode manter potência por alguns milissegundos após soltar joystick durante operação normal. O botão STOP deve permanecer acessível. Valores de rampa ainda são **parâmetros iniciais**, não medidos no robô.

## 4. Ensaios seguros antes de habilitar IMU

1. Conferir `hardwareMap` para MD e ME e se os nomes coincidem exatamente.
2. Usar o [DiagnosticoSinais](../TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/DiagnosticoSinais.java) já existente: com motores sem potência, girar chassi e rodas manualmente para verificar yaw/ticks.
3. Selecionar **TeleOp 1.0 - Chassi**; área livre e operador com STOP. Aplicar joystick esquerdo lentamente e observar **sentido físico real**, anotar MD e ME.
4. Testar só o joystick direito, observar se a potência com mesmo sinal gera realmente giro e em qual direção. Anotar yaw quando gira para direita e esquerda. Se não corresponder à intenção, **não habilitar IMU**.
5. Depois de corrigir/comprovar sinais, testar **TeleOp 1.1**, primeiro com LB, depois normal, finalmente turbo apenas em espaço seguro. Confirmar START alterando a curva **uma vez por pressão**.
6. Registrar tensão da bateria, condição de piso, tempo de desaceleração, deriva retilínea e resposta ao STOP.

**Critério mínimo:** potência 0 após STOP, duas rodas com sentidos coerentes, nenhuma inversão repentina na troca de joystick, e ausência de comportamento inesperado. Tolerâncias de tempo/erro devem ser definidas ANTES do ensaio.

## 5. Evoluções desenvolvidas depois deste registro

**1.2 — Assistência IMU experimental:** foi implementada em `TeleOp12.java`, com sinal de yaw escolhido e confirmado no INIT e correção P limitada no canal virtual X. **Ainda não validada fisicamente.**

**1.3 — Integração proposta de MI/MS:** foi implementada em `TeleOp13.java`, com armamento Y, bloqueio X, RB para MS, LB para MI, B para reversão e A para alimentação após aquecimento. O layout dos botões **ainda depende de aceite dos pilotos e ensaio físico**.

A descrição exata dos dois códigos novos está no [manual ilustrado](MANUAL_COMPLETO.md) e no [relatório por versão](../historico_teleop/VALIDACAO_TELEOP_1_2_A_1_3.md).

**Futuro:** registrar telemetria real, aferir sinais/rampa, testar mecanismos e somente depois definir TeleOp de competição.

## 6. Evidência de software

**Teste lógico reproduzível:** `node tests/teleop_1x_logic.mjs`, que modela somente matemática de joysticks/potências e rampas — não roda FTC SDK ou robô.

**Build real:** CI `.github/workflows/ftc-teleop.yml` clona FTC SDK oficial, copia apenas `TeleOp10.java` e `TeleOp11.java` e executa `./gradlew :TeamCode:assembleDebug`. **Build aprovado no [run #38327706268](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/actions/runs/38327706268): TeamCode:assembleDebug = BUILD SUCCESSFUL in 1m 46s.** As duas classes compilam no SDK oficial utilizado no CI, mas ainda não foram testadas no robô real.

**Documentação oficial de referência:** [FTC — Creating and Running an OpMode](https://ftc-docs.firstinspires.org/en/latest/programming_resources/tutorial_specific/android_studio/creating_op_modes/Creating-and-Running-an-Op-Mode-%28Android-Studio%29.html), [FTC — Motor Modes](https://ftc-docs.firstinspires.org/en/latest/tech_tips/tech-tips/tech-tip-motor-modes/tech-tip-motor-modes.html).

### Análise matemática da assistência IMU

Veja também o [estudo que embasou a versão experimental IMU 1.2](ESTUDO_ASSISTENCIA_IMU.md). A fórmula atual cria um **eixo virtual de giro pela soma dos comandos de motores**. Injetar uma correção com sinais opostos modifica o outro eixo virtual, o que pode explicar o problema anterior de movimento linear indesejado.

## 7. Ficha mínima por teste

```text
Data:
Versão e SHA:
MD/ME configuração / portas:
Bateria:
Piso e montagem:
Joystick testado e direção física observada:
Yaw IMU antes/depois:
Tempo para parar:
Falha e condições de reprodução:
Correção proposta:
Revisão e aceite por:
```

**Reversão:** escolha o OpMode anterior no Driver Station. Não substituir a classe `robo` que estiver instalada até que seu código exato seja recuperado e comparado com esta nova linha.
