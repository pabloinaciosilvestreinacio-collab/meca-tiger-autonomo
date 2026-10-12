# Autônomo 3.2 — Sem câmera
Data: 2026-10-12.
Base preservada: `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/autonomo.java` (3.1, SHA Git blob `cccc44f9ddfd8b90b06fb826d35e21b19bdc4fcb`).
Novo arquivo: `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo32.java`.
Cópia histórica: `historico/Autonomo_3_2.txt`.
Referência: `.agents/skills/ftc-engineer/SKILL.md` e `references/validation.md`.

## Escopo funcional
Exatamente a sequência da 3.1: virar -50°, capturar rumo, deslocar 2.000 mm com MI ligado, virar +100°, acionar MS e MI por tempos para tentar lançar três peças. **Nenhuma câmera, AprilTag ou VisionPortal**. Motores `MD`, `ME`, `MI`, `MS`; IMU `imu`; tração diferencial, nenhum `RUN_TO_POSITION`.

## Modificações 3.1 → 3.2
1. Arquivo/classe/anotação novos: `Autonomo32.java`, `@Autonomous(name="Autonomo 3.2")`. Originais 3.1 e 4.x/5.x não foram sobrescritos.
2. A condição de percurso passou a considerar apenas a média das leituras dos dois encoders para a distância linear: para a tração ao atingir o alvo; se o rumo estiver fora de 5°, usa `virarParaRumo(rumoAlvo, 1.5)` parado. Evita avançar indefinidamente para equilibrar os ticks dos lados.
3. PID angular: derivada normalizada entre amostras de erro, sem salto artificial perto de ±180°. Mantidos os valores Kp/Ki/Kd e anti-windup da 3.1; não há ajuste empírico inventado.
4. Giro: watchdog aborta se erro absoluto maior que 10° não melhorar pelo menos 1° em 1,2 s; potência zero quando o erro entra na banda de 2°. Limites de giro anteriores (7 s) preservados.
5. Reserva de execução: durante giro/percurso, se faltarem menos que 2,45 s (lançamento de 3) + 0,35 s de margem, os movimentos abortam antes de consumir o tempo. É uma proteção de STOP/falha, **não garante execução do lançamento se movimento anterior falhar**.
6. Tratamento de exceções de runtime e de inicialização da IMU, com motores zerados em `finally`.
7. Validação de distância finita, escala positiva e limite de conversão para `int`; média dos encoders calculada em `double` evitando estouro na soma.
8. Funções `calcularTempoLancamento`, `tempoRestanteSeguro`, `tempoDisponivelMovimento`; espera ativa só retorna sucesso se transcorreu o período pedido.
9. `MI`/`MS` configurados em `RUN_WITHOUT_ENCODER` para controle temporal, preservando os nomes.
10. `FATOR_CALIBRACAO_DISTANCIA=1.0` separado, para futura calibração física sem esconder a relação. **`REDUCAO=20.0` foi mantida como hipótese do código 3.1; não é uma aferição mecânica.**

## Evidências realmente executadas
- Análise estrutural do código Java: 14/14 verificações satisfeitas (sem import de câmera, nomes, pacote, anotação, chaves/parênteses, STOP no finally, controle manual, sinal das fórmulas, reserva em loops, etc.).
- Script `tests/autonomo32_logic_tests.mjs`: 19/19 assertions matemáticas. Modelos sintéticos de 6.000 giros nominais (-50°, +100°), todos convergiram dentro de 7 s; 400 inversões da polaridade física do yaw, todas interrompidas pelo watchdog.
- Script `tests/autonomo32_travel_model.mjs`: 3.000 deslocamentos num modelo cinemático simplificado, todos atingiram limiar de distância dentro de 12 s, sem travamento; 300 falhas forçadas de motor MD, todas detectadas como stall.
- Valor teórico `TICKS_POR_MM=(28×20)/(π×90)=1.9805948473658086`, alvo de 2.000 mm ≈ 3.961 ticks (ainda depende da redução real).
- Duração planejada do lançamento: 0,80 + 3×0,45 + 2×0,15 = 2,45 s.
- Modelos artificiais usam física simplificada e hipóteses (ganho/inércia/atrito fictícios; para percurso, velocidade de 800–1650 mm/s em potência 1, entre-eixos 450 mm, assimetria e patinagem artificiais). **Essas amostras não são medições nem estimam taxa real de sucesso.**

Comandos locais para reproduzir:
```sh
node tests/autonomo32_logic_tests.mjs
node tests/autonomo32_travel_model.mjs
```

## Build FTC real
Criado workflow `.github/workflows/autonomo32-validation.yml`, com checkout de FTC SDK oficial, cópia do `Autonomo32.java` e tarefa Gradle `:TeamCode:assembleDebug`.
**Até obter um resultado explícito do workflow, compilação real = NÃO CONFIRMADA.**
Estes testes não compilam o Java com o FTC SDK, nem se conectam ao Control Hub.

## Limitações e próximos ensaios
- Sentidos de `MD`, `ME` e sinais de encoder/IMU precisam de verificação física; sinal errado é detectado em determinados cenários e causa ABORT.
- IMU do Control Hub declarado `LogoFacingDirection.LEFT`/`UsbFacingDirection.UP` ainda requer conferência de montagem.
- A razão total instalada nas rodas (20:1 no código) e diâmetro efetivo de roda devem ser aferidos: não converter 2 m em ticks de competição antes disso.
- Kp/Ki/Kd, potência estática de giro 0,08, alimentação MI e tiro MS 0,70 dependem de ajuste no robô.
- O método `lancarPecasPorTempo` conta **tentativas temporizadas**, não peças fisicamente detectadas ou lançamentos confirmados.
- O pior caso de timeouts de virada/percurso/correção excede a janela. A proteção de tempo interrompe movimento com segurança, mas **não garante execução de toda a estratégia** sob falhas lentas.
- Testar primeiro diagnóstico de yaw/encoders com robô suspenso; depois giros curtos em baixa potência; depois percurso curto, depois sequência completa. Nenhuma movimentação ou instalação no hardware foi executada nesta revisão.

## Critério de aceitação
A 3.2 é **candidata para bancada**, não homologada. Para homologar: compilação FTC SDK confirmada, sentido e odometria validados, medição real de distância e ângulos, disparos cronometrados e uma execução completa segura dentro de 30 segundos.
