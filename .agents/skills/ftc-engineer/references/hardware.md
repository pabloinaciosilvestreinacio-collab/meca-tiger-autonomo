# Hardware e geometria — referência auditada (Autônomo 3.1)

> Dados abaixo são extraídos do código versionado e de informações de projeto; não constituem calibração física. Confirmar com a equipe antes de executar movimentos reais.

| Item | Valor documentado | Status |
| --- | --- | --- |
| Equipe | Meca Tiger FTC #32578 | Identificação de projeto |
| Cinemática | Diferencial, dois motores de tração traseira e apoio dianteiro passivo | Informação de projeto, conferir montagem atual |
| Motor direito | `MD`, direção `REVERSE` | Confirmado no código; sentido físico pendente |
| Motor esquerdo | `ME`, direção `FORWARD` | Confirmado no código; sentido físico pendente |
| Coleta/alimentação | `MI` | Nome no `hardwareMap` |
| Lançador | `MS` | Nome no `hardwareMap` |
| IMU | `imu` | Nome no `hardwareMap` |
| Hub, orientação no código | logo `LEFT`, USB `UP` | Montagem física pendente |
| Ticks por volta do motor | 28 | Constante do código, conferir motor real |
| Redução | 20:1 | Constante do código, conferir transmissão |
| Diâmetro da roda | 90 mm | Constante do código, aferição pendente |
| Ticks por mm | `(28*20)/(pi*90)` ≈ 1,9806 | Dedução matemática dos parâmetros acima |
| Modo da tração | `RUN_WITHOUT_ENCODER` | Código utiliza encoder para feedback manual |
| Temporização máxima do autônomo | 30 s, reserva de 1 s | Constantes do código |
| Webcam futura | Logitech C270, nome pretendido `Webcam 1` | Requisito de projeto; não existe no 3.1 |

## Conferências antes de calibração
1. Identificar etiqueta/especificação dos motores e a redução real instalada; registrar foto ou medição quando possível.
2. Com rodas suspensas, aplicar potência baixa por curto período **apenas com autorização presencial** e conferir avanço físico esperado.
3. Verificar se os encoders de ambos os lados aumentam no deslocamento que o código chama de positivo.
4. Mover o robô manualmente, observar sinal e continuidade do yaw, validar orientação do Control Hub.
5. Aferir distância percorrida e erro angular em circuito de testes seguro antes de alterar `TICKS_POR_MM` ou PID.
6. Para AprilTags, confirmar montagem da câmera, nome no Driver Hub e dados da temporada.

## Base de software
- Fonte auditada: `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/autonomo.java`.
- Hash Git do código 3.1: `cccc44f9ddfd8b90b06fb826d35e21b19bdc4fcb`.
- A versão está preservada em `historico/Autonomo_3_1.txt` com o mesmo hash.
- A raiz auditada **não contém** todos os arquivos Gradle do FTC SDK; não assumir compilação pronta.
