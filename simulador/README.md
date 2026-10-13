# Meca Sim 0.1 — simulador FTC em duas vistas

Protótipo local funcional para testar **lógica Java real** de `LinearOpMode`, visualizar um chassi diferencial e explorar a geometria do lançador. Criado a partir do plano cartesiano do Desmos da Meca Tiger.

**Estado:** experimental. Java compila contra uma camada FTC parcial, não contra o SDK Android oficial. Sem ensaio físico ou certificação FIRST. Não valida precisão real nem pontuação.

## Abrir no Windows

1. Instale **Python 3.10 ou superior** e **JDK 17 ou superior**. Android Studio pode conter um JDK em `jbr`; configure `JAVA_HOME` para essa pasta se `javac` não estiver no PATH.
2. Baixe esta branch do repositório em **Code → Download ZIP** e extraia.
3. Abra a pasta `simulador` e execute `iniciar.bat`; mantenha o terminal aberto.
4. Abra **http://127.0.0.1:8765** no Chrome ou Edge do computador.
5. Escolha um exemplo ou cole uma classe Java pública completa, com pacote/imports e `extends LinearOpMode`.
6. Clique **Compilar / INIT**, espere `inicializando`, depois clique **START**.
7. Conecte o controle e pressione um botão para o navegador detectá-lo. Use STOP para encerrar.

Nenhuma biblioteca Python ou Node é necessária para usar. `java` sozinho não basta: confirme `javac -version` e `java -version`.

Linux/macOS: `python3 simulador/server.py`. Para outra porta, configure `SIM_PORT` antes de iniciar. O servidor escuta somente `127.0.0.1`; não expose na rede. **Execute apenas Java confiável: ele tem as mesmas permissões da sua conta. O processo não está em uma sandbox.** Esta arquitetura local não serve para executar código de visitantes em um servidor público.

## O que já funciona

| Recurso | Comportamento |
| --- | --- |
| Java real | Compilação com `javac`; processo JVM separado; erros de compilação/execução no console |
| Ciclo do OpMode | INIT, `waitForStart`, START, STOP, `opModeIsActive`, `idle`, `sleep`, limite de duração |
| Tração diferencial | Reta, ré, arco e giro no centro; não faz deslocamento lateral mecanum |
| Encoders | Ticks com direção lógica, reset, alvo e modelo ideal de `RUN_TO_POSITION` |
| Controle manual | `RUN_WITHOUT_ENCODER` para Java que calcula seu próprio P/PID |
| IMU | Yaw ideal normalizado, `resetYaw`, `AngleUnit`; pitch e roll fixos em zero |
| Gamepads 1/2 | Eixos, gatilhos, botões e D-pad com mapeamento `standard` do navegador |
| Vista de cima | Campo genérico 3,66 × 3,66 m, trilha, chassi, lançador e alvo editável |
| Vista lateral | Projeção no eixo de tiro, parábola ideal, alcance e altura na distância do alvo |
| Lançador | Offset lateral/frontal, altura, elevação e azimute; prévia manual animada |
| Diagnóstico | Telemetria, potência, ticks, status, console e exportação de resultado JSON |
| Persistência local | Código e configurações salvos no navegador; sem envio a terceiros |

Motores configuráveis esquerdo/direito; `MI` e `MS` auxiliares, IMU `imu`, servo `servo`. Nomes inexistentes falham explicitamente. `MS` e `MI` apenas registram comandos/ticks ideais; não geram peças ou disparos. O botão **Visualizar lançamento** é uma experiência balística manual independente do Java.

## Convenções e cálculos

Todas as distâncias em metros; ângulos da interface em graus. X cresce à direita, Y para cima. O yaw=0 aponta a frente para +Y; yaw positivo gira no sentido anti-horário.

Centro `(cx,cy)`, offset local do lançador `(u,v)` e yaw `a`:

```
Lx = cx + u cos(a) - v sin(a)
Ly = cy + u sin(a) + v cos(a)
```

Corresponde ao Desmos com `cx=p+R/2`, `cy=P+R/2`, `u=R/4`, `v=R/2`. Alterar o offset não altera a pose do robô. Vista lateral usa o mesmo ponto, projetado no eixo de tiro. Alvo vertical no plano é tratado com vetores, sem divisão por `deltaX`.

```
velocidade linear = (velocidade direita + velocidade esquerda) / 2
velocidade angular = (velocidade direita - velocidade esquerda) / bitola
h(d) = H + d tan(elevação) - 9,81 d² / (2 V² cos²(elevação))
```

O chassi usa integração exata de arco a cada 10 ms. `setDirection` e polaridade de montagem são fatores separados; o encoder reporta posição lógica. **Os valores iniciais são hipóteses editáveis, não calibração do robô.** A relação inicial 1980,6 ticks/m corresponde aproximadamente a 28 ticks × redução 20:1 / roda de 90 mm, descrita no código do repositório.

O exemplo TeleOp é um novo exemplo didático com mistura diferencial convencional. Não modifica a mistura histórica dos TeleOps da equipe. Ao colar um TeleOp existente, confira as polaridades na aba Robô; o mesmo nome de motor não prova o mesmo sinal físico.

## Teclado / gamepad

| Entrada | Campo no Java |
| --- | --- |
| W / S | `gamepad1.left_stick_y` negativo / positivo |
| A / D | `gamepad1.right_stick_x` negativo / positivo |
| Espaço | `gamepad1.a` |
| B / X / Y | `gamepad1.b` / `gamepad1.x` / `gamepad1.y` |
| Setas | `gamepad1.dpad_*` |
| Controle conectado | Substitui o teclado para gamepad1; segundo controle alimenta gamepad2 |

Ao perder foco, teclado zera. Ao ocultar a página, STOP é enviado. Desconectar controle zera seus comandos na próxima leitura. Mapeamentos não padrão são recusados nesta versão. Não há garantia de suporte a todo controle/driver; o teste USB físico está pendente.

## Compatibilidade e limites

Compatível com uma classe pública que estenda diretamente `LinearOpMode` e classes auxiliares no mesmo arquivo. APIs implementadas estão em `sdk.py`: `DcMotor`, subconjunto herdado em `DcMotorEx`, `HardwareMap`, `Gamepad`, `IMU`, `ElapsedTime`, `Range`, `Telemetry`, `Servo`, anotações e tipos de orientação/ângulo.

Métodos externos à camada (por exemplo velocidade/PIDF do `DcMotorEx`), câmera, AprilTag, Android, Road Runner, bibliotecas de terceiros e projetos com múltiplos arquivos **ainda não são suportados**. Erros não são substituídos por resultados inventados. A orientação do Hub é aceita na assinatura, mas não modifica o yaw virtual. `BRAKE`/`FLOAT` são armazenados; ambos param instantaneamente neste modelo sem inércia. Servo guarda posição lógica, sem mecanismo físico. Telemetria implementa o subconjunto usado pelos exemplos; não toda a API oficial.

Não há colisões, atrito, aceleração, escorregamento, bateria, ruído/atraso de sensores, obstáculo, campo oficial ou regra de pontuação. A vista lateral é uma projeção do tiro, não um solver de dinâmica 3D. A velocidade da bola é manual, não derivada de MS. Temporização tem passo fixo, mas o agendamento da JVM/OS introduz variação: este protótipo não é um replay determinístico e não calibra PID real.

## Testes reproduzíveis

Na raiz do repositório, com JDK instalado:

```sh
python -m unittest discover -s simulador/tests -v
node --test simulador/tests/model.test.mjs
node --check simulador/web/app.js
```

Node só é necessário para os testes JavaScript. Python pula os testes de integração Java se o JDK estiver ausente — esse resultado não confirma a execução Java. Após editar exemplos: `python simulador/build_examples.py`.

Compatibilidade com nove OpModes existentes e cenários completos: `python simulador/tests/repository_compatibility.py`. O Autônomo 3.5 exige selecionar Y e depois confirmar com A **no INIT**, antes de START. O teste envia essas entradas sintéticas; o código original permanece intacto.

Veja [evidências da 0.1](VALIDACAO.md) e [plano de evolução](PLANEJAMENTO.md). O código do robô permanece fora desta pasta. Para desfazer, feche o servidor e volte à branch principal; nada é instalado no Control Hub.

## Referências

- [FIRST — SDK FTC, referência de código](https://github.com/FIRST-Tech-Challenge/FtcRobotController)
- [LinearOpMode — Javadoc FTC](https://javadoc.io/static/org.firstinspires.ftc/RobotCore/10.3.0/com/qualcomm/robotcore/eventloop/opmode/LinearOpMode.html)
- [Gamepad API — MDN](https://developer.mozilla.org/en-US/docs/Web/API/Gamepad_API/Using_the_Gamepad_API)
