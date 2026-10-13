# Estudo da assistência IMU do TeleOp — preparação para versão 1.2

**Histórico da análise que fundamentou o TeleOp 1.2/1.3.** A assistência já foi implementada em código experimental, mas seu sentido físico ainda NÃO foi validado. Para os controles realmente disponíveis consulte o [manual ilustrado](MANUAL_COMPLETO.md).

## 1. Problema observado

Em um teste anterior de assistência ao rumo, o piloto solicitava correção angular e o robô parecia avançar/recuar em vez de girar. A causa exata ainda **não foi medida em hardware**, mas a álgebra da mistura definida pela equipe mostra um erro provável: aplicar a correção ao canal errado.

## 2. Mistura histórica preservada

Com `y = gamepad1.left_stick_y` e `x = gamepad1.right_stick_x`:

```java
double MD = -x - y;
double ME = -x + y;
```

Rearranjando:

```
MD + ME = -2*x
MD - ME = -2*y
```

**Consequência matemática:** o comando de giro virtual controla a **soma** dos comandos Java MD/ME, enquanto a translação virtual controla a **diferença**. Isso é o inverso de uma mistura arcade convencional com direções e inversões diferentes. **Não confundir os valores dos comandos Java com os sentidos físicos dos eixos**, que dependem das direções declaradas nos motores e montagem.

Se um algoritmo altera as potências da forma `MD += u; ME -= u;`, a soma não muda e a diferença muda. Nesta convenção, isso modifica o **eixo de translação virtual**, não o eixo de giro virtual. Pode explicar o sintoma de avançar/recuar ao tentar corrigir yaw. Não classificar como causa física comprovada antes de realizar teste do robô.

## 3. Formulação de referência — adotada na versão experimental 1.2

Só depois de observar o sinal físico do yaw:

```java
double erro = normalizar(rumoAlvo - yawAtual);
double xAssistencia = SINAL_GIRO_YAW * kp * erro;

double xFinal = limitar(xPiloto + xAssistencia, -1.0, 1.0);
double comandoMD = -xFinal - yPiloto;
double comandoME = -xFinal + yPiloto;
```

- `SINAL_GIRO_YAW` deve ser +1 se **x do joystick positivo** aumentar o yaw (giro CCW em graus), ou -1 se diminuir.
- Sem medir o sinal físico, **não atribuir arbitrariamente +1 ou -1**.
- Ao ativar a assistência, guardar `rumoAlvo=yawAtual` para não haver salto.
- Durante giro comandado pelo piloto, suspender a correção automática e atualizar a referência de rumo. Ao soltar o joystick de giro, capturar o novo rumo para mantê-lo.
- Limitar e filtrar a assistência; tratar yaw NaN/Infinity, STOP e mudança de modo.
- Durante testes iniciais, assistência desativada por padrão e somente com potência reduzida. Verificar primeiro sem movimento longitudinal.
- Definir explicitamente se o modo segurará o rumo com joystick de avanço em zero. A opção **mais conservadora** para teste inicial é corrigir somente enquanto o piloto está avançando ou recuando, sem provocar giro inesperado quando ambos os joysticks estão neutros.

A equação é um **desenho matemático**, e a versão experimental 1.2 implementa seu princípio com captura de rumo, sinal de yaw confirmado e limites. Não foi validada fisicamente.

## 4. Teste físico mínimo e critério de aceite

1. Executar `DiagnosticoSinais` do repositório para verificar yaw CCW positivo.
2. Executar `TeleOp 1.0 - Chassi` em área livre a no máximo 35% e mover **somente x positivo** por um instante. Registrar se o yaw ficou positivo ou negativo.
3. Repetir com x negativo, e depois y positivo/negativo isolados. Conferir se girar, avançar e recuar correspondem à intenção do piloto.
4. Se a resposta física for incompatível, corrigir direção ou mapeamento de motor como problema separado antes do PID.
5. Só então criar/compilar `TeleOp12.java` com assistência BACK, **inicialmente desativada** e com limitação rigorosa da correção.
6. Repetir giros curtos e trajetos retilíneos; falha ou oscilação exige STOP e registro de parâmetros.

## 5. Como validar por software antes de teste com robô

Testes matemáticos obrigatórios da versão 1.2: yaw 179/-179, reativação do BACK sem salto, mudança da referência ao giro manual, correção limitada, entradas não finitas, prioridade do piloto, borda de botão, parada e ambas as hipóteses de sinal do yaw.

**Estado:** análise algébrica realizada, preparação documentada, ensaio físico de sinais **não executado**. A linha 1.0/1.1 é intencionalmente manual; nenhuma assistência IMU foi ativada sem sinal físico confirmado.
