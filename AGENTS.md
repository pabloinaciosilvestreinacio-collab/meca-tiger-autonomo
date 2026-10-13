# Meca Tiger FTC #32578 — instruções de engenharia para agentes

## Fonte da verdade e escopo
- Repositório de código Java FTC (Autônomo **e** TeleOp), documentação, snapshots e simulações matemáticas da equipe Meca Tiger FTC #32578.
- **Autônomo 3.1** original: `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/autonomo.java`. Classes posteriores existem separadas até 5.1 com câmera; a linha 3.2–3.5 não usa câmera.
- **TeleOp**: versões 1.0–1.3 em `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/teleOp/`, com manual de botões em `teleop/MANUAL_COMPLETO.md`. Versões 1.2/1.3 são experimentais, não calibradas em hardware.
- Todos os arquivos históricos em `historico/` e `historico_teleop/` devem ser preservados byte a byte. Os snapshots 2 e 3 do autônomo compartilham nome de classe `autonomo` e não são fontes instaláveis simultaneamente com 3.1.
- Consulte código/commit efetivo antes de inferir comportamento a partir de memória, README ou chat; em conflito sinalize explicitamente.

## Autorização e segurança
- Ler, inspecionar, simular e propor melhorias são permitidos.
- Implementações solicitadas devem ocorrer preferencialmente em **branch isolada**, com documentação das alterações, scripts de teste e PR de revisão.
- **Não forçar push, apagar históricos, modificar visibilidade, definir licença ou realizar operações destrutivas sem aprovação específica.** Não declarar release físico sem ensaio.
- Não acionar motores/intake/lançador reais sem supervisão, plano seguro, área livre e STOP acessível.
- Nunca afirmar que um build FTC provou precisão de motores, yaw, câmera, lançador ou pontuação real.

## Mecânica, APIs e sinais
- Tração diferencial de dois motores MD (direito), ME (esquerdo), não mecanum. MI é intake/alimentação e MS lançador. IMU é `imu`. As classes com visão usam Logitech C270 `Webcam 1`.
- FTC SDK de referência: **v12.0 BIOBUZZ 2026–2027**, Java 17, Android Studio Narwhal 3 Feature Drop ou posterior. Pipeline integrada: `.github/workflows/ftc-projeto.yml`.
- MD REVERSE, ME FORWARD, IMU logo LEFT/USB UP e relações de redução são **hipóteses do código**, não sinais medidos. Não trocar polaridade, Kp/Ki/Kd ou geometria por suposição.
- **TeleOp** preserva mistura histórica `MD = -x - y; ME = -x + y`. Uma correção de yaw deve atuar no eixo virtual X, não sobre `MD += u; ME -= u` sem análise de sinais. A 1.2/1.3 exige confirmação da polaridade do yaw no INIT para habilitar BACK.
- Autônomo usa `RUN_WITHOUT_ENCODER`, leitura manual dos ticks e PID de giro/rumo. Não substituir automaticamente por `RUN_TO_POSITION`.
- MI/MS não possuem no código atual sensores de peça nem feedback real de RPM. Não afirmar lançamento ou pontuação porque houve comando temporizado.
- Com AprilTag, validar nomes e roll da CELL, detecções frescas, bearing, latência, offset câmera-lançador e fallback sem assumir localização global.

## Critérios de código e revisão
- Simplicidade: classes independentes até confirmar em bancada; não criar árvore grande de subsistemas sem necessidade.
- Conferir STOP/try-finally, hardwareMap, timeouts aplicáveis, NaN, saturação, normalização de ângulo, anti-windup e limites dos motores.
- Mudança de botão TeleOp **obriga atualizar manual e diagrama SVG do gamepad**.
- Cada versão nova deve manter arquivo Java distinto, backup exato, entrada no CHANGELOG, testes de regressão e relatório de evidências/riscos.
- Documentação pública deve ser navegável e diferenciar código histórico, candidato, build confirmado e ensaio físico.

## Evidências obrigatórias
1. Revisão estática: inspecionado, não necessariamente compilado.
2. Testes Node: matemática/sensores sintéticos, não robô.
3. CI real: `TeamCode:assembleDebug` no projeto FTC SDK completo, com log e commit rastreável.
4. Ensaio físico: somente com ficha preenchida, foto/vídeo, telemetria, condições, responsável e aceite.

Reproduzir `node tests/repositorio_integridade.mjs` e os scripts aplicáveis. Use o [portal](docs/README.md), [manual de TeleOp](teleop/MANUAL_COMPLETO.md), [relatório geral](RELATORIO_GERAL_VALIDACAO_SOFTWARE.md) e [checklist de publicação](docs/PUBLICACAO.md).

## Entrega de alterações
Informe: arquivos modificados, versão antes/depois, razões técnicas, evidências executadas, falhas ainda abertas, procedimento de reversão e o que será validado fisicamente. Não descrever um teste como PASS antes de conferir o log real.
