# Autônomo 3.3 — Controle robusto SEM câmera
Data: 2026-10-12
Base imutável: `Autonomo32.java` (cópia em `historico/Autonomo_3_2.txt`).
Novo arquivo: `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo33.java`.
Backup integral: `historico/Autonomo_3_3.txt`.
Padrão: `.agents/skills/ftc-engineer/SKILL.md`, `AGENTS.md` e `references/validation.md`.

## Contrato funcional
Preserva a estratégia original da 3.1 e da 3.2: virar **-50°**, registrar o rumo, percorrer **2.000 mm** coletando com **MI**, virar **+100°**, ligar **MS** e alimentar por **MI** por três ciclos temporizados. Movimentação diferencial de **MD** e **ME**, `RUN_WITHOUT_ENCODER` com controle manual PID (P, I e D). Não há webcam, AprilTag nem visão.

## Mudanças da 3.2 para a 3.3
1. Limite por giro reduzido de **7,0 para 5,0 s**; deslocamento de **12,0 para 10,0 s**. Não muda Kp, Ki, Kd nem potência mecânica da base.
2. Checagem de **orçamento temporal antes de iniciar**: `tempoPiorCaso()` soma dois giros (5 + 5), translação (10), possível giro final (1,5), lançamento de três peças por tempo (2,45) e margem (0,35): **24,30 s**, inferior aos **29 s** disponíveis após reserva global de 1 s. Isso é uma soma de limites e não garantia de movimento concluído.
3. Guardas da tração: se `abs(ticksMD - ticksME)>220`, cancela o percurso com motivo. Se `abs(erroRumo)>35°`, também cancela. **Ambos são valores de segurança propostos, não calibrados**, para evitar falso sucesso com roda/encoder divergente.
4. Na preparação, `validarConfiguracao()` rejeita valores inválidos (não finitos, transmissão negativa, potências fora de limites, ângulos/dados incorretos, orçamento inviável). O programa não executa a rotina se a configuração falhar.
5. Telemetria de diferença entre encoders; na alimentação o texto foi alterado para **"ciclos de alimentação comandados"**. Sem sensor de peça e sem controle de RPM não é válido afirmar que lançou/pontuou três peças.
6. Mantida, sem alteração, a derivada angular já corrigida na **3.2**, assim como o anti-windup, filtragem D, detector de giro sem progresso, STOP e `finally`.
7. `REDUCAO=20.0`, diâmetro de roda de 90 mm, 28 ticks por giro de motor e `FATOR_CALIBRACAO_DISTANCIA=1.0` permanecem hipóteses até aferição; não se presume cartucho específico.

## Validação executada
- Regressão sintética registrada em `tests/autonomo_sem_camera_3x.mjs` (Node.js sem bibliotecas externas). Comando: `node tests/autonomo_sem_camera_3x.mjs`.
- **17 assertions** aprovadas (normalização angular, derivada no wrap, ticks/mm, orçamento temporal, confirmação do modo, guardas de falhas).
- **5.000 cenários** em modelo cinemático de dois motores: 1.000 nominais completaram, 1.000 com sinal IMU invertido terminaram pelo watchdog do primeiro giro, 1.000 com motor direito bloqueado interromperam na divergência dos encoders, 1.000 com encoder congelado interromperam na divergência e 1.000 com IMU inválido abortaram. A física foi simplificada com ruído, inércia e assimetria aleatórias, não corresponde a ensaio físico.
- **Atenção:** o modelo de teste é uma implementação JS paralela das equações de Java. Os resultados **NÃO demonstram** que o FTC SDK real compilou ou que o robô responderá da mesma forma.

## Verificações ainda pendentes
- Compilação do FTC SDK 12.0: workflow `.github/workflows/ftc-sem-camera.yml` preparado; **não afirmamos build aprovado sem status/log**.
- Verificação de orientação `LEFT/UP` do hub, polaridade yaw e polaridade de ambos os encoders.
- Redução efetiva e diâmetro carregado; roda pode patinar, especialmente durante as manobras.
- Teste real de 2 metros, curva de aceleração/frenagem, efeitos de carga e nível de bateria; limite de 220 ticks corresponde a ~111,08 mm de diferença teórica entre rodas para 1,9806 ticks/mm.
- Teste de `MI`/`MS`, velocidade efetiva do lançador e cadência. **Não há confirmação automática do número de peças**.
- Legalidade/segurança da rota no campo, colisões, posição inicial e repetibilidade.
- Nenhum teste físico ou envio de código ao Control Hub foi efetuado.

## Reversão
Manter `Autonomo32.java` e `autonomo.java` intactos e selecionar a OpMode anterior no Driver Station. Não exige rollback de outros arquivos.


### Atualização de validação CI — 12/10/2026
**Compilação FTC real aprovada:** GitHub Actions [run #38319766443](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/actions/runs/38319766443), `compileDebugJavaWithJavac` e `assembleDebug` com `BUILD SUCCESSFUL in 2m 16s`. Teste Node: 21 assertions e 5.000 cenários; auditoria: 58 verificações. **Não representa teste físico**. Ver `historico/RELATORIO_CI_SEM_CAMERA_3X.md`.
