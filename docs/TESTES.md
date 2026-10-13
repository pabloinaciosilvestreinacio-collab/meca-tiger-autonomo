# Testes, automação e critérios de aceitação

## Níveis de evidência

| Nível | Exemplo | Aceitação possível |
|---|---|---|
| E0 | Fonte histórica salva | Integridade e proveniência |
| E1 | Inspeção estática | Possíveis erros detectados |
| E2 | Assertions Node/JS | Equações e estados em modelo sintético |
| E3 | Simulação dinâmica simplificada | Cenários artificiais dentro da hipótese |
| E4 | Build `:TeamCode:assembleDebug` | Compatibilidade Java/SDK usado no CI |
| E5 | Ensaio físico | Sinais, precisão, latências e dispositivos reais |

**Não atribuir E5 se não houver execução do robô.**

## Histórico já confirmado

- Autônomo 3.1–3.5: [build #38319766443](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/actions/runs/38319766443) aprovado.
- Autônomo 3.1–5.1 com câmera: [build integrado #38321732887](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/actions/runs/38321732887) aprovado.
- TeleOp 1.0/1.1: [build #38327706268](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/actions/runs/38327706268) aprovado, **105.049 assertions sintéticas**, 25.000 amostras e 25 verificações estáticas.
- TeleOp 1.2/1.3: código experimental; o [workflow integrado](../.github/workflows/ftc-projeto.yml) é a fonte de verdade para resultados novos. Não alegar build/ensaios sem run bem-sucedido.

## Como reproduzir localmente (Node.js 22)

```bash
node tests/autonomo32_logic_tests.mjs
node tests/autonomo32_travel_model.mjs
node tests/autonomo_sem_camera_3x.mjs
node tests/auditoria_autonomo_3x.mjs
node tests/autonomo41_logic_tests.mjs
node tests/autonomo50_synthetic_tests.mjs
node tests/autonomo51_cell_tests.mjs
node tests/autonomo51_pid_model.mjs
node tests/auditoria_documentacao_geral.mjs
node tests/teleop_1x_logic.mjs
node tests/teleop_1x_static.mjs
node tests/teleop_12_13_logic.mjs
node tests/repositorio_integridade.mjs
```

Os scripts de JavaScript **não são um emulador FTC SDK**. Eles ajudam a detectar bugs lógicos e regressões em uma implementação matemática equivalente, mas não demonstram resposta física do robô.

## Build integrado fixado à temporada

O workflow `.github/workflows/ftc-projeto.yml` obtém o SDK oficial pela **tag v12.0** e copia ambas as famílias de classes `Autonomo/` e `teleOp/` antes de compilar `:TeamCode:assembleDebug`. O log deve mostrar `compileDebugJavaWithJavac` e `BUILD SUCCESSFUL`.

O repositório armazena apenas `TeamCode` parcial, não o projeto Gradle completo. Um build local válido exige abrir o projeto oficial FTC no Android Studio.

## Critérios mínimos de bancada

1. Inspeção de rodas, fios, fixação do Hub, bateria e STOP.
2. Medida de yaw positivo por giro anti-horário e direção dos encoders com a tração desligada.
3. Giros +20°/−20° e avanço curto a potência limitada; medir comportamento real.
4. TeleOp 1.2: comparar sinal do joystick de giro com yaw do IMU; habilitar BACK somente se coerente.
5. TeleOp 1.3: manter X desarmado, armar deliberadamente com Y e comprovar que MI/MS respondem apenas aos botões e interlocks definidos, sem peças primeiro.
6. Depois: teste de MI/MS em zona de lançamento protegida e medição de RPM/tempo de alimentação real.
7. Autônomo 5.1: validar câmera USB, roll dos clusters, bounding de bearing, iluminação, FPS e offset óptico.
8. Registrar **todas** as tentativas na [ficha](../MODELO_REGISTRO_ENSAIO_AUTONOMO.md), inclusive falhas.

## Relatórios de origem

[Validação geral do autônomo](../RELATORIO_GERAL_VALIDACAO_SOFTWARE.md) · [Validação TeleOp 1.0/1.1](../historico_teleop/VALIDACAO_TELEOP_1_0_A_1_1.md) · [Relatório de integração reorganizada](RELATORIO_REORGANIZACAO.md).
