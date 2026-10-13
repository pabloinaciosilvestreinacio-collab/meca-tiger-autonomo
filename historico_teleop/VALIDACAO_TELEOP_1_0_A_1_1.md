# TeleOp Meca Tiger — validação e histórico das versões 1.0 e 1.1

**Data:** 12/10/2026  
**Situação:** versões reconstruídas conforme requisitos conhecidos; **código `robo` real usado pela equipe não recuperado**.

## Inventário e integridade

| Versão | Classe Java | Histórico |
| --- | --- | --- |
| 1.0 | `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/teleOp/TeleOp10.java` | `historico_teleop/TeleOp_1_0.txt` |
| 1.1 | `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/teleOp/TeleOp11.java` | `historico_teleop/TeleOp_1_1.txt` |

Os arquivos são independentes e não alteram o autônomo. As cópias são verificadas por SHA de conteúdo em teste automatizado.

## Mudanças 1.0

- Introduz versão manual de potência reduzida a **0,35**, apenas MD/ME.
- Mantém a mistura definida pelo usuário: `MD = -right_stick_x - left_stick_y`; `ME = -right_stick_x + left_stick_y`.
- `MD=REVERSE`, `ME=FORWARD`, `BRAKE`, `RUN_WITHOUT_ENCODER`: configurações da linha de referência, **sem comprovação de montagem física**.
- `DEAD_ZONE = 0.08`, rejeição de entradas NaN/infinito.
- `finally` zera MD e ME no STOP e na saída normal.
- MI e MS não são mapeados nesta classe; **não afirmar que estão fisicamente desligados** se outro software ainda os estiver comandando.

## Mudanças 1.1

- Novo OpMode separado e versão preservada.
- LB = precisão 35%; RB = turbo 100%; sem bumpers = normal 70%. LB tem prioridade se ambos pressionados.
- Curva quadrática com sinal habilitada por padrão. START alterna por **borda de subida**, não em todos os loops de botão mantido.
- Normalização conjunta da mistura (`max(1, |MD|, |ME|)`) para evitar saturação não proporcional em dois eixos.
- Limites `RAMPA_ACELERACAO=2.0` e `RAMPA_FRENAGEM=4.0` unidades de potência/s; `dt` limitado a 100 ms; troca de sentido passa por zero antes de acelerar no sentido oposto.
- Saída do OpMode zera os dois motores imediatamente em `finally`, sem rampa.
- BACK/IMU e gamepad2/MI/MS **ainda não implementados**, pois a direção física de yaw e os controles de mecanismos precisam ser confirmados.

## Revisão de sinais e riscos

Os comandos históricos para frente (Y=-1) produzem MD=+1, ME=-1 **antes de aplicar velocidades e `setDirection()`**. Para giro com X=+1 produzem MD=-1, ME=-1. **O sentido físico depende do acoplamento e inversão dos motores** e deve ser verificado com roda livre e espaço seguro antes de habilitar assistência de rumo IMU.

Risco residual da rampa: a potência pode levar até algumas centenas de milissegundos para zerar após o eixo voltar ao centro; em STOP o `finally` desliga imediatamente. Risco residual de falta de mecanismos: o projeto completo requer garantir que MI/MS estejam controlados e interrompidos em um futuro OpMode completo.

## Validação de software

### Testes matemáticos

Script `tests/teleop_1x_logic.mjs` executado no ambiente JavaScript de análise em 12/10/2026:

- **105.049 assertions aprovadas**;
- **25.000 amostras de controle sintéticas**;
- **50.000 saídas de motor verificadas**;
- 17.939 inversões de sentido verificadas sem transição imediata para sinal oposto;
- 12.087 acelerações verificadas dentro do limite de slew;
- Casos particulares: dead zone ±0,08, valores não finitos, curva quadrática, mistura direita/esquerda, prioridade LB, borda de START e `dt` de zero/grande.

Esses números são da **execução de uma implementação matemática JavaScript equivalente**, e **NÃO** de teste com motores, FTC SDK nem gamepad físico. O script versionado permite reprodução exata pelo comando `node tests/teleop_1x_logic.mjs`.

### Compilação FTC

Workflow configurado `.github/workflows/ftc-teleop.yml` com `TeamCode:assembleDebug`, clonando a distribuição oficial FTC. **Estado: pendente de resultado observado no GitHub Actions**; não afirmar compilação real só por revisão estática.

### Ensaios físicos

**Nenhum ensaio físico desta linha foi realizado pelo assistente.** Necessários: sinais MD/ME, sentido do yaw no Hub, resposta do gamepad e tempo real da rampa, STOP e tensão. Consultar [manual de TeleOp](../teleop/README.md).

## Próxima versão 1.2

Implementar assistência de rumo por botão BACK somente depois de identificar o **sinal do eixo virtual de giro que realmente produz yaw positivo**. Corrigir a falha anterior em que assistência IMU moveu o robô para frente/trás em vez de girar: a equação de correção deve respeitar a própria mistura histórica, com sinais medidos. Não declarar essa correção concluída antes do ensaio.
