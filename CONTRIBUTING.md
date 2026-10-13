# Como contribuir — Meca Tiger FTC #32578

Obrigado pelo interesse em engenharia e robótica. Este repositório documenta código de **controle físico**, então segurança e rastreabilidade têm prioridade sobre mudanças rápidas.

## Princípios

1. Consulte o [portal](docs/README.md), a [arquitetura](docs/ARQUITETURA.md) e o [manual do TeleOp](teleop/MANUAL_COMPLETO.md) antes de modificar o comportamento do robô.
2. **Não sobrescreva classes históricas** nem os arquivos de `historico/` e `historico_teleop/`. Crie versão nova com classe/OpMode distintos.
3. **Não altere a polaridade de MD/ME/IMU nem ganhos PID sem evidência física**; indique hipótese e ensaio proposto.
4. Toda mudança no TeleOp deve atualizar o desenho do gamepad, tabela de botões e documentação de parada/segurança.
5. Não registrar simulação como teste de campo; inclua logs/commit/versão do FTC SDK para builds.
6. Não adicione chaves, senhas, certificados, fotos pessoais, arquivos de configuração Wi-Fi ou conteúdo privado da equipe.

## Fluxo sugerido

1. Abrir uma issue com problema, condição de reprodução, código/versão e comportamento esperado.
2. Criar branch `desenvolvimento/<assunto>` ou `correcao/<problema>`.
3. Alterar somente o código necessário e, quando for controle físico, criar uma nova versão de OpMode.
4. Adicionar testes matemáticos/estáticos, atualizar `CHANGELOG.md` e um relatório com hipóteses e limitações.
5. Rodar os scripts relevantes em `tests/` e compilar FTC `:TeamCode:assembleDebug` com o SDK v12.0.
6. Abrir PR com os resultados executados, prints de telemetria se houver e como reverter; não considerar completo antes de ensaio físico aprovado.

## Revisão de pull requests

PR precisa explicar: **qual comportamento mudou, qual classe e botão, por quê, como testar, quais riscos e como reverter**. Aprovação de código não é autorização para motorizar o robô sem supervisão. Mudanças de publicação/licença/visibilidade exigem decisão específica da equipe.

## Exemplo de título

`fix(teleop): corrigir normalização de potência no modo turbo` ou `docs: registrar calibração de encoder a 350 mm`.

Referências: [AGENTS.md](AGENTS.md), [Guia de validação](docs/TESTES.md), [Política de segurança](SECURITY.md).
