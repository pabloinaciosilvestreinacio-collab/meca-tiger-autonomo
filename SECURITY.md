# Segurança e divulgação responsável

Este projeto controla motores e mecanismos físicos de um robô FTC. Nem toda falha é digital: sinais incorretos, alimentação elétrica, bateria, atrito e fixação podem causar movimentação inesperada.

## Como reportar uma falha

Antes de criar issue pública sobre segredo vazado ou risco relevante, use **a opção de contato privado por GitHub Security Advisory**, se habilitada pelo proprietário, ou entre em contato diretamente com mantenedores do repositório. **Não publique senhas, tokens, credenciais, dados pessoais ou procedimentos perigosos em issues públicas.**

Para erros normais de simulação e documentação, issues e PRs são adequados quando não há dados sensíveis envolvidos.

## Política de segurança do código

- STOP deve zerar potência dos atuadores.
- Mudanças em IMU, yaw, limites de potência, alimentação ou lançador precisam de revisão e ensaio supervisionado.
- Simulação artificial, compilação bem-sucedida ou lint aprovado **não substituem** validação no robô.
- O gamepad2 da TeleOp 1.3 tem armamento deliberado Y e bloqueio imediato X, mas **não constitui um dispositivo físico de parada de emergência**.
- Antes de tornar público, revisar histórico, dados confidenciais e direitos de imagem/licença.

Veja [checklist de publicação](docs/PUBLICACAO.md), [testes](docs/TESTES.md) e [manual completo do TeleOp](teleop/MANUAL_COMPLETO.md).
