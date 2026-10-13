# Checklist para transformar o repositório em público

**Estado atual:** `private`. Este documento é um **plano**, não uma autorização automática para mudar a visibilidade ou divulgar os arquivos.

## Antes de publicar

- [ ] Revisar o histórico completo de commits, branches, issues e PRs em busca de tokens, chaves, senhas, configurações de Wi-Fi, credenciais, arquivos .env, dados de menores, telefones e fotos com identificação.
- [ ] Conferir se a equipe e a gestão do SESI SENAI autorizam a publicação de código, textos e eventuais imagens oficiais.
- [ ] Decidir uma licença de software com quem detém os direitos. **Nenhuma licença foi atribuída automaticamente.** Um repositório público sem licença não concede, por si só, direitos de reprodução e modificação.
- [ ] Revisar atribuições de código de terceiros e requisitos das licenças do FTC SDK e bibliotecas.
- [ ] Confirmar que nenhum dado técnico é confidencial da estratégia de competição; considerar liberar versões apenas após testes/temporada conforme decisão da equipe.
- [ ] Rodar `node tests/repositorio_integridade.mjs` e verificar os alertas de segurança manualmente.
- [ ] Confirmar que README, manuais, diagramas, referências e links funcionam quando acessados sem login.
- [ ] Colocar contato público **somente com aprovação da equipe**; não expor dados pessoais por conveniência.
- [ ] Conferir se os resultados descritos têm links de build e distinguir testes artificiais de ensaios físicos reais.
- [ ] Revisar o conteúdo de `AGENTS.md` e as instruções de automação para verificar que não contêm informações sensíveis.
- [ ] Decidir nome e descrição definitivos do repositório. O nome atual `meca-tiger-autonomo` já contém TeleOp; considerar `meca-tiger-robotics` apenas se a equipe quiser mudar URLs/integrações.
- [ ] Só depois: alterar visibilidade para `public` com aprovação específica dos responsáveis.

## Estrutura voltada para leitores externos

A pasta `docs/` apresenta o índice editorial. Os históricos antigos permanecem no local original para preservar SHAs e links. Os diagramas SVG em `assets/` são editáveis e legíveis no GitHub.

## Limitações da auditoria automática

O script de integridade detecta **padrões comuns em arquivos atuais**, mas não substitui inspeção humana de todo o histórico Git e arquivos excluídos. Não interprete “checks passados” como garantia de ausência de segredos.

## Antes de aceitar contribuições externas

Definir quem revisa PRs, política de autoria, licença e processo de segurança. Consultar [CONTRIBUTING.md](../CONTRIBUTING.md), [SECURITY.md](../SECURITY.md) e [AGENTS.md](../AGENTS.md).
