# ÍNDICE DOS CÓDIGOS INTEGRAIS — AUTÔNOMO MECA TIGER FTC #32578

**Revisão de documentação:** 12/10/2026.  
**Objetivo:** permitir localizar, comparar, ler e restaurar **todo o código original das 12 versões** sem misturar classes históricas ou perder a rastreabilidade.

**Documento principal:** [Dossiê técnico integral](DOSSIER_TECNICO_COMPLETO_AUTONOMO.md)  
**Manual:** [Calibração e testes](MANUAL_GERAL_CALIBRACAO_TESTES.md)  
**Relatório de CI:** [Validação de software](RELATORIO_GERAL_VALIDACAO_SOFTWARE.md)

> **Sobre integridade:** hash abaixo é SHA-1 de **blob Git** (bytes do conteúdo do arquivo), não commit SHA. Cada backup 3.1–5.1 foi comparado ao arquivo Java da classe correspondente no GitHub. As versões 2 e 3 são arquivos históricos originais e não devem ser colocadas junto à classe 3.1, pois compartilham o nome de classe Java autonomo.

## A. Linha histórica e SEM câmera

| Versão | Código integral | Backup histórico | SHA blob de conteúdo | Situação |
| --- | --- | --- | --- | --- |
| **2** | [Código 2 — 608 linhas](historico/Autonomo_2.txt) | Próprio arquivo | 65072b9b18a42438df625deca4722596126b006d | Histórico não reinstalado |
| **3** | [Código 3 — 704 linhas](historico/Autonomo_3.txt) | Próprio arquivo | 91c6d4ebadcf974df24bd5a9eafeaf502c0c2bba | Histórico não reinstalado |
| **3.1** | [autonomo.java — 766 linhas](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/autonomo.java) | [Autonomo_3_1.txt](historico/Autonomo_3_1.txt) | cccc44f9ddfd8b90b06fb826d35e21b19bdc4fcb | Referência original |
| **3.2** | [Autonomo32.java — 856 linhas](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo32.java) | [Autonomo_3_2.txt](historico/Autonomo_3_2.txt) | ac3d183421553b9adeba620e6299cdb1504baeb2 | Corrige rumo, trajetória e PID |
| **3.3** | [Autonomo33.java — 938 linhas](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo33.java) | [Autonomo_3_3.txt](historico/Autonomo_3_3.txt) | 7103e0b86180def8967814960995637ea136e3c8 | Guardas, tempo e encoders |
| **3.4** | [Autonomo34.java — 1.006 linhas](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo34.java) | [Autonomo_3_4.txt](historico/Autonomo_3_4.txt) | 62228bf535c382505328a386d70cda85a67b8b2d | Modos de teste da tração |
| **3.5** | [Autonomo35.java — 1.029 linhas](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo35.java) | [Autonomo_3_5.txt](historico/Autonomo_3_5.txt) | 37cc2ac051f014487d45702958e46f0bc4e3455b | Bancada completa sem câmera |

**Status do build:** [GitHub Actions #38319766443](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/actions/runs/38319766443) validou a compilação integrada dos arquivos **3.1–3.5**. Não constitui teste físico.

### Observações de estudo

- **2:** arquivo original contém PID P/I/D e testes/limites de encoders e yaw conforme código.
- **3:** configurações de redução divididas; giro com P/D e parâmetros temporários de tolerância.
- **3.1:** PID manual P/I/D, sequência de tarefas, referências de estratégia; fonte-base preservada em main.
- **3.2:** derivada angular contínua perto de ±180°, chegada pela média de ticks e correção final parada.
- **3.3:** orçamento de tempo, validação de constantes e verificações de divergência entre rodas.
- **3.4:** interface no INIT com B, X, Y e A para testar modos isolados.
- **3.5:** D-pad CIMA testa MI/MS isolados, medição por etapa e confirmação do A por nova pressão.

## B. Linha COM câmera Logitech C270

| Versão | Código integral | Backup histórico | SHA blob de conteúdo | Situação |
| --- | --- | --- | --- | --- |
| **4.0** | [Autonomo4.java — 1.399 linhas](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo4.java) | [Autonomo_4_0.txt](historico/Autonomo_4_0.txt) | d4a984f7d0f4b680ec1f16ab6d8426b325e2715b | Visão inicial e fallback IMU |
| **4.1** | [Autonomo41.java — 1.421 linhas](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo41.java) | [Autonomo_4_1.txt](historico/Autonomo_4_1.txt) | df6c87f201687018c9138ce4eeb905a07eaa6378 | Detecções por quadros frescos |
| **4.2** | [Autonomo42.java — 1.460 linhas](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo42.java) | [Autonomo_4_2.txt](historico/Autonomo_4_2.txt) | dfd441704ea442aa7776fbe8e64dea9220e16ecb | Orçamento 26,9 s e watchdog |
| **5.0** | [Autonomo50.java — 1.485 linhas](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo50.java) | [Autonomo_5_0.txt](historico/Autonomo_5_0.txt) | bffe2ce8e23a905a76301854300270a2c5232970 | Escolha de aliança e lado |
| **5.1** | [Autonomo51.java — 1.514 linhas](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo51.java) | [Autonomo_5_1.txt](historico/Autonomo_5_1.txt) | 91081923a507cec0379b8c5a98d41e0817b4b76c | CELL pontuável dinâmica |

**Status do build:** [GitHub Actions #38321732887](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/actions/runs/38321732887) compilou todos os arquivos Java acima, além dos sem câmera, no FTC SDK integrado. A câmera **não foi aferida fisicamente**.

### Componentes de diagnóstico (não integram contagem das 12 versões)

- [DiagnosticoSinais.java](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/DiagnosticoSinais.java): telemetria de yaw e ticks com potência zero nos motores.
- [DiagnosticoAprilTag.java](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/DiagnosticoAprilTag.java): C270 e clusters, sem comando de deslocamento.

## C. Mapa rápido dos principais métodos Java

| Objetivo de estudo | Classes recomendadas | Métodos específicos |
| --- | --- | --- |
| Ciclo de vida OpMode | 3.5, 5.1 | runOpMode, configurarHardware, configurarImu |
| Máquina de sequência | 3.1, 3.5, 5.1 | sequencia, tarefa, interface Tarefa |
| Giro por IMU e PID | 3.2, 3.5, 5.1 | virarRelativo, virarParaRumo, ControladorPID.calcular |
| Correção linear | 3.2, 3.5, 5.1 | andarComRumo, reiniciarEncoders, lerYaw |
| Coleta e lançamento | 3.5, 5.1 | coletarDuranteDeslocamento, lancarPecasPorTempo, esperarAtivo |
| Segurança, STOP e diagnósticos | 3.3, 3.5, 5.1 | validarConfiguracao, tempoDisponivel, pararTudo, mostrarFalha |
| Inicialização C270 | 4.0–5.1 | configurarVisao, cameraEstaStreaming, fecharVisao |
| AprilTags e mira | 4.1, 5.1 | lerMelhorAlvo, dadosVisaoValidos, alinharComVisaoOuFallback |
| Estratégia de aliança | 5.0, 5.1 | clusterAlvoDaEstrategia, ehClusterBiobuzz |
| Calibração de bancada | 3.5, diagnósticos | ModoExecucao, DiagnosticoSinais, DiagnosticoAprilTag |

## D. Como inspecionar a diferença entre duas versões

No GitHub, use o histórico de commits ou o comando diff local após clonar a branch correta:

~~~bash
git diff --no-index historico/Autonomo_3_2.txt historico/Autonomo_3_3.txt
git diff --no-index historico/Autonomo_3_4.txt historico/Autonomo_3_5.txt
git diff --no-index historico/Autonomo_4_0.txt historico/Autonomo_4_1.txt
git diff --no-index historico/Autonomo_4_2.txt historico/Autonomo_5_0.txt
git diff --no-index historico/Autonomo_5_0.txt historico/Autonomo_5_1.txt
~~~

O Git pode retornar status 1 ao encontrar diferenças; isso é normal para git diff --no-index.

Para reprodução completa de testes, consulte [Relatório Geral](RELATORIO_GERAL_VALIDACAO_SOFTWARE.md) e os scripts na pasta [tests/](tests/). A auditoria automática confere se o hash de cada cópia histórica ainda corresponde ao reportado.

## E. Regras de proteção do histórico

1. Não mudar os .txt da pasta historico quando for calibrar a próxima versão.
2. Não criar simultaneamente versões 2, 3 e 3.1 como classes Java autonomo dentro do mesmo pacote.
3. Ao alterar hardware/constantes, criar versão de desenvolvimento e guardar o valor anterior, o novo, o motivo e o ensaio que o sustentou.
4. Não declarar novo build aprovado sem log TeamCode:assembleDebug; não declarar desempenho físico aprovado sem ficha real.
5. Respeitar [AGENTS.md](AGENTS.md): merge, substituição histórica e alteração da main requerem aprovação específica.

**Para estudar o projeto inteiro sem perder a sequência:** ler o [Dossiê Técnico Completo](DOSSIER_TECNICO_COMPLETO_AUTONOMO.md) e abrir o Java integral de cada versão pela tabela acima.