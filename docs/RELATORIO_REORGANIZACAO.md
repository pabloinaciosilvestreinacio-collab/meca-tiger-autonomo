# Relatório de reorganização e evolução TeleOp — Meca Tiger FTC #32578

**Início:** 12/10/2026 (Brasília)  
**Branch de integração:** `organizacao/repositorio-ftc-integrado`  
**Base:** `documentacao/calibracao-validacao-todas-versoes-20261012`  
**Meta:** repositório intuitivo e apto a uma **futura revisão de publicação pública**, preservando todo o código e o histórico.

## 1. Diagnóstico antes de alterar

A `main` tinha um README escrito em várias etapas, com estados de CI desatualizados e caminhos repetidos. As versões recentes do autônomo, o dossiê e o TeleOp estavam em **três branches/PRs distintos**, dificultando descobrir qual arquivo usar.

O código TeleOp `robo` instalado anteriormente na escola não estava presente no GitHub; por isso as novas classes 1.x são **reconstruções baseadas na mistura MD/ME conhecida** e não cópias do último software físico da equipe.

**Risco de tornar público:** licença não definida, titularidade de identidade gráfica ainda a confirmar, histórico Git e configurações que precisam ser auditados humanamente. **Nenhuma mudança de visibilidade foi realizada.**

## 2. Consolidação sem perdas

- Copiadas para esta branch as classes do autônomo 3.3–3.5 e documentos da branch `desenvolvimento/autonomo-sem-camera-3x-20261012` pela base de documentação.
- Preservado dossiê das versões 2–5.1, todas as classes originais, testes, histórico e relatórios.
- Integrados arquivos TeleOp 1.0/1.1 e backups originalmente em `desenvolvimento/teleop-1x`, sem modificar conteúdo original (shas iguais).
- Adicionadas **TeleOp 1.2** (assistência IMU experimental) e **TeleOp 1.3** (gamepad2 para MI/MS com armamento seguro), como classes novas, mantendo as anteriores intactas.
- Adicionados snapshots integrais 1.2/1.3 em `historico_teleop/`.

## 3. Melhorias de código TeleOp

| Versão | Mudança | Risco/limite |
|---|---|---|
| 1.0 | MD/ME a 35%, dead zone e STOP em finally | Sentido físico não comprovado |
| 1.1 | 35/70/100%, curva quadrática START, rampa e inversão segura | Parada por joystick usa rampa; STOP é imediato |
| 1.2 | BACK mantém rumo **apenas durante movimento**, P limitado 0,15, sinal do yaw escolhido e confirmado antes de START, falha IMU desativa ajuda | `KP_RUMO=0.012`, eixo/IMU não calibrados; requer teste de sinal |
| 1.3 | Gamepad2: Y arma, X bloqueia, RB shooter segurado, A alimenta só após RB por 0,8 s, LB intake, B reverso; `finally` zera quatro motores | Aquecimento sem RPM real, sentidos MI/MS ainda não verificados; teclas são proposta |

**Não há mudança no autônomo em razão destas melhorias de TeleOp**; comparar SHAs de fontes originais entre branches.

## 4. Reorganização editorial

| Arquivo/pasta | Motivo |
|---|---|
| `README.md` | Entrada única, navegação curta, estado verificável, instruções reais de compilação |
| `assets/meca-tiger-banner.svg` | Identidade visual grafite/laranja com geometria abstrata, **sem fingir ser o logo oficial** |
| `assets/teleop-gamepads.svg` | Desenho vetorial dos dois controles, com função dos botões |
| `docs/README.md` | Portal por assunto |
| `docs/ARQUITETURA.md` | Mapa de classes, pastas e hardware |
| `docs/TESTES.md` | Evidência estática, simulação, build e ensaio físico |
| `docs/PUBLICACAO.md` | Checklist jurídico, técnico, segurança e privacidade |
| `teleop/MANUAL_COMPLETO.md` | Mapa detalhado dos gamepads, limitações, procedimento de teste |
| `CONTRIBUTING.md`, `SECURITY.md` | Orientação a novos contribuidores e divulgação responsável |
| `.gitignore`, `.gitattributes` | Reduzir risco de arquivos locais e padronizar EOL/diffs |
| `tests/repositorio_integridade.mjs` | Verificar snapshots, links internos, fontes e padrões óbvios de segredo |
| `.github/workflows/ftc-projeto.yml` | Compilação em FTC SDK **v12.0** fixado, junto às duas famílias de OpModes |

**Arquivos de histórico foram mantidos nos locais anteriores**, pois uma movimentação destrutiva invalidaria referências e dificultaria comparar hashes.

## 5. Validação e evidências

### Evidências anteriores verificadas
- Autônomo 3.1–3.5: [CI #38319766443](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/actions/runs/38319766443), build aprovado.
- Autônomo 3.1–5.1 + diagnósticos: [CI #38321732887](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/actions/runs/38321732887), build aprovado.
- TeleOp 1.0/1.1: [CI #38327706268](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/actions/runs/38327706268), build aprovado, 25 mil cenários sintéticos de controles.

### Nova integração
- `node tests/teleop_12_13_logic.mjs`: testa injeção no eixo de yaw para ambos os sinais, rearmamento de BACK, interrupções, bloqueios MI/MS e aquecimento no modelo de software.
- `node tests/repositorio_integridade.mjs`: compara fontes Java e snapshots históricos, confere links do manual, rótulos da OpMode e busca padrões típicos de segredos nos arquivos atuais.
- `.github/workflows/ftc-projeto.yml`: executa testes em Node.js e compila as classes do Autônomo e TeleOp contra o **SDK oficial v12.0**. **Resultado específico desta branch:** verificar a execução Actions vinculada ao commit; não declarar PASS sem job.
- **Nenhum teste físico foi realizado** pelo assistente; o fluxo de bancada está descrito no [Manual do TeleOp](../teleop/MANUAL_COMPLETO.md) e [Manual Geral](../MANUAL_GERAL_CALIBRACAO_TESTES.md).

## 6. Riscos pendentes

1. Recuperar o TeleOp `robo` atualmente usado no Android Studio e compará-lo com 1.3 antes de instalação no Control Hub.
2. Verificar sinal de yaw provocado pelo joystick direito X positivo e orientação física LEFT/UP do Hub.
3. Testar giro e translado em potência baixa antes de liberar BACK assistido.
4. Testar polaridade e potência MI/MS, tempo de aquecimento, alimentação sem travamento e mecanismo com área isolada.
5. Aprovar com pilotos a proposta dos botões do gamepad2 antes de considerá-la configuração oficial.
6. Revisar direitos de imagens/logotipos, licença, dados privados e commits antigos antes da futura publicação.
7. Atualizar relatórios com logs físicos vinculados a commit exato; simulação não substitui ensaio.
8. Fixar ambiente Java/Android e obter release da equipe apenas após ensaios.

## 7. Reversão

- As versões antigas permanecem preservadas no GitHub com classes e OpModes independentes.
- Esta entrega não torna o repositório público automaticamente.
- Se necessário, recuperar o `README.md` ou código anterior por commit/branch Git; nenhum histórico foi apagado.
- As classes antigas do autônomo não são alteradas nesta branch; a publicação na `main` deve ocorrer somente após revisão do diff e estado CI.

**Resumo:** reorganização de documentação + nova linha TeleOp experimentais, com segurança priorizada, provas de software discriminadas e checklist explícito para utilização física e publicação futura.
