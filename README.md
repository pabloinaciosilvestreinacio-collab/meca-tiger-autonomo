# Meca Tiger - Autonomo FTC

## Documentação geral — todas as versões (2 a 5.1)

**Comece por estes documentos** — cobrem todas as versões já registradas, com e sem câmera:

- **[Manual geral de calibração e testes](MANUAL_GERAL_CALIBRACAO_TESTES.md)**: inspeção de hardware, motor/encoder/IMU, ajuste de PID, odometria, lançador MI/MS, câmera C270, AprilTags, ensaios graduais e comparação entre versões.
- **[Relatório geral de validação de software](RELATORIO_GERAL_VALIDACAO_SOFTWARE.md)**: matriz das 12 versões, mudanças, SHA de cada código, scripts, simulações, evidências de compilação FTC e pendências físicas.
- **[Ficha preenchível de ensaio](MODELO_REGISTRO_ENSAIO_AUTONOMO.md)**: registrar medições reais de cada teste, falhas, condição da bateria, calibração e decisão de aceite.
- **[Guia específico sem câmera](GUIA_AUTONOMO_SEM_CAMERA.md)**: detalhes operacionais adicionais das versões 3.1–3.5.
- **[Relatórios individuais e históricos](historico/)**: cópias exatas por versão e evidências registradas.

**Status de evidência:** a compilação FTC das versões 3.1–3.5 foi confirmada no [GitHub Actions #38319766443](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/actions/runs/38319766443). Para as versões com câmera, consultar o relatório geral e os logs do workflow de auditoria; não inferir compilação sem log. Testes físicos ainda precisam ser registrados.


## Linha 3.x — Autonomo SEM camera

**Versao candidata mais recente: Autonomo 3.5 - Bancada (na branch de desenvolvimento).**

- `autonomo.java` = 3.1 de referencia, intacta.
- `Autonomo32.java` = 3.2, ja existente na main, sem camera.
- `Autonomo33.java` = 3.3, com limites adicionais de seguranca, diagnostico e orcamento de tempo.
- `Autonomo34.java` = 3.4, com B = teste de giro (+20 e -20), X = teste de avanco (350 mm), Y = sequencia completa; **A confirma** no INIT.
- `Autonomo35.java` = 3.5, acrescenta **D-pad para cima = teste somente do lancador MI/MS** e tempo/resultado de cada etapa; os testes anteriores continuam.
- Todas as versoes permanecem separadas, com backups em `historico/`.
- **Manual operacional:** [GUIA_AUTONOMO_SEM_CAMERA.md](GUIA_AUTONOMO_SEM_CAMERA.md).
- **Historico de testes:** `historico/VALIDACAO_Autonomo_3_3.md`, `...3_4.md` e `...3_5.md`.
- **Teste reproduzivel:** `node tests/autonomo_sem_camera_3x.mjs` e `node tests/auditoria_autonomo_3x.mjs`.
- **FTC SDK build:** **APROVADO** no [GitHub Actions run #38319766443](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/actions/runs/38319766443), com `BUILD SUCCESSFUL`. **Hardware real ainda não testado.** Relatório: `historico/RELATORIO_CI_SEM_CAMERA_3X.md`.

A linha 3.x e independente da 4.x/5.x, que utiliza camera. Nenhum arquivo original foi sobrescrito.


## Autonomo 3.2 (sem camera) — candidato para testes

A versão sem câmera foi criada em `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo32.java`. O arquivo original `autonomo.java` continua sendo a 3.1 intacta. Nenhuma funcionalidade de visão integra o 3.2.

Mantem giro -50°, avança 2000 mm coletando, gira +100° e tenta lançar 3 peças. Corrige condição de parada por encoder, continuidade angular do PID, watchdog de giro e reserva de tempo. Todos os resultados até aqui são testes estáticos, matemáticos e simulações sintéticas; `20:1`, IMU e ganhos de PID aguardam medição real.

- Historico: `historico/Autonomo_3_2.txt`
- Validacao detalhada: `historico/VALIDACAO_Autonomo_3_2.md`
- Testes: `node tests/autonomo32_logic_tests.mjs` e `node tests/autonomo32_travel_model.mjs`
- Workflow de compilacao: `.github/workflows/autonomo32-validation.yml` (resultado ainda pendente).

## Candidato mais novo: Autonomo 5.1 (branch de revisao)

- Arquivo: `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo51.java`
- Mantem: giro -50 graus, coleta a 2000 mm, giro +100 graus, alinhamento por cluster da propria alianca e tentativa de lancar 3 elementos.
- INIT: D-pad esquerda RED / direita BLUE; cima SCORING / baixo AUDIENCE; A confirma estrategia. Sem confirmacao o AUTO nao move motores.
- Detecta automaticamente a CELL pontuavel da propria alianca pelo `roll`, mesmo que mude de orientacao.
- Limites somados + margem: 26,90 s teoricos, dentro de 29 s utilizaveis.
- Veja `historico/VALIDACAO_VERSOES_4_2_A_5_1.md`. O CI foi configurado mas a compilacao real ainda nao foi confirmada; este codigo **NAO** foi testado fisicamente.
- Scripts: `node tests/autonomo50_synthetic_tests.mjs`, `node tests/autonomo51_cell_tests.mjs`, `node tests/autonomo51_pid_model.mjs`.
- Diagnosticos de campo (NAO acionam motores): `DiagnosticoAprilTag.java` e `DiagnosticoSinais.java`.

### Histórico completo
- 3.1 estavel: `Autonomo/autonomo.java` (arquivo 3.1 em main; nao alterar)
- 4.0: `Autonomo4.java`
- 4.1: `Autonomo41.java`
- 4.2: `Autonomo42.java`
- 5.0: `Autonomo50.java`
- 5.1: `Autonomo51.java`
- Cada versao evolutiva esta preservada em `historico/Autonomo_X_Y.txt` na branch.

Repositorio do codigo autonomo da equipe Meca Tiger FTC #32578.

## Versoes e status

- **3.1 (base estavel)**: `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/autonomo.java`.
- **4.0 (revisao anterior)**: `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo4.java`.
- **4.1 (candidata em testes, apenas na branch de revisao)**: `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo41.java`.
- **Teste separado da camera**: `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/DiagnosticoAprilTag.java`.

A 4.1 usa `getFreshDetections()` e melhora o tratamento de perda de frames, delta angular do PID e conversao de engrenagens. Ainda depende de build no FTC SDK e testes fisicos. Consulte `historico/VALIDACAO_Autonomo_4_1.md` e reproduza a simulacao com `node tests/autonomo41_logic_tests.mjs`.

## Autonomo estavel

`Autonomo 3.1`

O codigo atual executa:

1. Virada relativa de -50 graus.
2. Captura do rumo pelo IMU.
3. Coleta durante um deslocamento de 2000 mm.
4. Virada relativa de +100 graus.
5. Lancamento de 3 pecas por temporizacao.

O controle de tracao usa encoders e IMU em `RUN_WITHOUT_ENCODER`, com PID
calculado manualmente pelo programa.

## Codigo

O arquivo principal esta em:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/autonomo.java`

Para usar no projeto FTC SDK, coloque esse arquivo no mesmo caminho dentro do
modulo `TeamCode`.

## Hardware configurado

| Nome | Funcao |
| --- | --- |
| `MD` | Motor direito da tracao |
| `ME` | Motor esquerdo da tracao |
| `MI` | Intake/alimentador |
| `MS` | Lancador |
| `imu` | IMU do Control Hub |

## Aviso de teste

A logica foi simulada e a sintaxe foi verificada com interfaces equivalentes
as chamadas do FTC SDK. A validacao final deve ser feita no Android Studio e
no robo, conferindo direcoes dos motores e orientacao fisica do Control Hub.
