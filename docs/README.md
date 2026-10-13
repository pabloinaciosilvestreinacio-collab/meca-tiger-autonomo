# Portal da documentação — Meca Tiger FTC #32578

O objetivo deste índice é permitir que **alguém novo na equipe entenda o projeto em minutos**, consiga localizar os fontes completos e diferencie código validado de código experimental.

## 1. Primeiros passos

- [Página principal do repositório](../README.md): visão geral, como compilar e estado do projeto.
- [Arquitetura e organização](ARQUITETURA.md): motores, packages, arquivos, fluxos e estratégia.
- [Manual de calibração do autônomo](../MANUAL_GERAL_CALIBRACAO_TESTES.md): protocolo de bancada.
- [Manual ilustrado do TeleOp](../teleop/MANUAL_COMPLETO.md): imagem dos dois controles, todos os comandos e proteções.
- [Matriz de testes e evidências](TESTES.md): o que foi realmente comprovado.

## 2. Autônomo (sem e com câmera)

| Material | Finalidade |
|---|---|
| [Dossiê técnico integral](../DOSSIER_TECNICO_COMPLETO_AUTONOMO.md) | Evolução completa desde a versão 2 |
| [Índice de códigos originais](../INDICE_COMPLETO_CODIGOS_AUTONOMO.md) | Os 12 fontes/snapshots e hashes |
| [Guia específico sem câmera](../GUIA_AUTONOMO_SEM_CAMERA.md) | Calibração da série 3.x |
| [Relatório geral de validação](../RELATORIO_GERAL_VALIDACAO_SOFTWARE.md) | Builds, testes e lacunas de evidência |
| [Histórico do autônomo](../historico/) | Relatórios individuais 3.2, 3.3, 3.4, 3.5, 4.0, 4.1, 4.2–5.1 |

## 3. TeleOp

| Material | Finalidade |
|---|---|
| [Manual completo do TeleOp](../teleop/MANUAL_COMPLETO.md) | Botões dos gamepads, diagrama e rotina de teste |
| [Versões anteriores 1.0/1.1](../teleop/README.md) | Histórico dos comandos manuais |
| [Estudo de assistência IMU](../teleop/ESTUDO_ASSISTENCIA_IMU.md) | Análise algébrica do erro antigo |
| [Códigos Java](../TeamCode/src/main/java/org/firstinspires/ftc/teamcode/teleOp/) | TeleOp10, 11, 12 e 13 |
| [Históricos originais](../historico_teleop/) | Snapshots e relatórios |
| [Validação TeleOp 1.2–1.3](../historico_teleop/VALIDACAO_TELEOP_1_2_A_1_3.md) | Assistência IMU, gamepad2, mudanças e ensaios pendentes |

## 4. Desenvolvimento e manutenção

- [Como contribuir](../CONTRIBUTING.md): branches, convenção de nomes, documentação obrigatória e revisões.
- [Relatório da reorganização](RELATORIO_REORGANIZACAO.md): mudanças, riscos, validação e reversão.
- [Publicação futura](PUBLICACAO.md): checklist de licença, segurança e privacidade.
- [Política de segurança](../SECURITY.md): divulgação responsável e procedimentos para incidentes.
- [Ficha de registro de ensaio](../MODELO_REGISTRO_ENSAIO_AUTONOMO.md): uma ficha para cada tentativa física.
- [CHANGELOG](../CHANGELOG.md): registro incremental de mudanças.

## 5. Regra de ouro para os relatórios

| Tipo de evidência | Pode-se afirmar |
|---|---|
| Revisão estática | Inspecionado, sem garantia de compilação |
| Teste JavaScript | Lógica testada em modelo artificial |
| Build FTC SDK | Java/SDK compilados naquele ambiente |
| Ensaio físico | Comportamento medido, com parâmetros, vídeo/telemetria e aceite |
| Competição | Resultado observado e registrado em campo |

**Não** marcar o hardware como “100% pronto” com base apenas em simulação ou GitHub Actions.
