# Validação e alterações — TeleOp 1.2 e 1.3

**Data:** 12/10/2026 (horário do Brasil; alguns logs do CI registram 13/10 em UTC).  
**Fontes:** `TeleOp12.java` e `TeleOp13.java`.  
**Histórico integral:** `historico_teleop/TeleOp_1_2.txt` e `TeleOp_1_3.txt`.  
**Base:** TeleOp 1.1 preservado exatamente, sem modificar seus métodos.

## 1. Diferenças da 1.1 para 1.2

| Componente | Antes (1.1) | Depois (1.2) |
|---|---|---|
| IMU | Não acessado | Inicialização tentativa; o retorno booleano de `IMU.initialize()` determina disponibilidade |
| BACK | Sem função | Heading hold proporcional apenas enquanto segurado, avançando/recuando, com x manual neutro |
| Segurança de sinal | Sem yaw | D-pad esquerdo = sinal -1, direito = +1; A confirma **por borda nova** durante INIT; sem confirmação ajuda bloqueada |
| Correção | Nenhuma | `xAssist = limitar(sinalYaw × 0.012 × erroRumo, ±0.15)`; atua no canal virtual X da mistura MD/ME |
| Perda de sensor | Não se aplica | Leitura não finita ou exceção desabilita a assistência e deixa controle manual |
| Falha de rumo | Não se aplica | Erro >12° bloqueia a assistência até soltar BACK |
| Giro do piloto | Chassi manual | Com joystick X, assistência é suspensa; o rumo é recapturado ao retomar a função |

**Não é PID completo**: o controle do BACK tem apenas termo proporcional, sem I nem D. Não repetir a falha histórica de injetar `MD += u` e `ME -= u` sem respeitar a mistura particular do chassi.

**Risco:** o sinal de yaw real e a montagem LEFT/UP ainda precisam ser medidos, então a função permanece experimental.

## 2. Diferenças da 1.2 para 1.3

| Componente | Novo comportamento |
|---|---|
| Hardware | Adiciona `MI` e `MS` do hardwareMap; `DcMotor`, modo RUN_WITHOUT_ENCODER |
| Armamento | Mecanismos desarmados ao iniciar; Y arma somente após nova pressão com comandos liberados |
| Corte | X bloqueia MI/MS em qualquer momento, até novo armamento |
| Intake | gamepad2 LB fornece MI +0,60 enquanto segurado |
| Reverso | gamepad2 B fornece MI −0,35 e **desliga MS**, mesmo com RB segurado |
| Lançador | gamepad2 RB fornece MS +0,70 enquanto segurado, desde que armado e sem reverso |
| Alimentação | gamepad2 A solicita MI +0,60 somente após RB manter MS por ≥0,80 s |
| Prioridade | X e desarmamento > B reverso > A condicionado a MS > LB coleta |
| STOP | `finally` zera todos MD, ME, MI e MS; MS em FLOAT pode continuar girando fisicamente por inércia |

**Botões são proposta de layout do TeleOp 1.3**, não um mapeamento confirmado pelo piloto. O arquivo `robo` original da escola não estava no repositório, portanto não foi substituído.

## 3. Ensaios de software

- Testes da 1.0/1.1: `tests/teleop_1x_logic.mjs` e `tests/teleop_1x_static.mjs`; compilação FTC confirmada no [run #38327706268](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/actions/runs/38327706268).
- Novas simulações: `tests/teleop_12_13_logic.mjs` cobre ambos os sinais de yaw, perturbações, bloqueio/desbloqueio do BACK, botão Y/X, comando B de reversão, RB/A e limitações de saída em ciclos aleatórios.
- Auditoria de código e histórico: `tests/repositorio_integridade.mjs` confere equivalência de snapshots, nomes, manuais, links, dependências e padrões comuns de segredo em arquivos atuais.
- Pipeline conjunta `.github/workflows/ftc-projeto.yml` compila todas as classes contra tag oficial **v12.0** do FTC SDK com `TeamCode:assembleDebug` e roda modelos Node.
- **O modelo JS não executa o Java nem controla o robô.** Confirmar resultados do workflow por commit, no GitHub Actions. Não atribuir taxa de sucesso física a amostras artificiais.

### Resultado de compilação FTC e testes sintéticos (13/10/2026 UTC)

[GitHub Actions #38334743988](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/actions/runs/38334743988) **2/2 jobs aprovados**. Todos os Java do Autônomo e TeleOp compilaram juntos com tag FTC SDK **v12.0**; `TeamCode:compileDebugJavaWithJavac` e `TeamCode:assembleDebug`, **BUILD SUCCESSFUL in 2m 30s**.

- Controle de heading P e mecanismos: **257.629 assertions**, 132.000 amostras matemáticas de yaw e 25.000 de MI/MS, tudo sintético.
- Integridade: **14 pares de Java/snapshot e 493 verificações**.
- Os blobs das classes Java compiladas são idênticos aos atuais: 1.2 `231f53a86b8e7ce818b5469c4aa86be6e200f196`; 1.3 `a83a8a82d38f8b364d8729a1496de873db84dec7`.
- **Não houve execução no robô**, tampouco medida real de sinais, RPM e velocidade.

## 4. Testes reais ainda obrigatórios

1. Confirmar `MD`, `ME`, `MI`, `MS`, `imu` no Driver Station; conferir yaw sem motor.
2. Verificar com TeleOp 1.0 se joystick X positivo aumenta ou reduz yaw e se joystick Y move para frente. Só então habilitar BACK em 1.2.
3. Medir correção BACK em avanço curto com sinal escolhido e confirmado. STOP imediatamente em caso de divergência.
4. Armamento Y/desarmamento X no gamepad2 sem peças; conferir saída zero inicial e prioridade entre comandos.
5. Verificar o sentido e a corrente de MI/MS, área de disparo protegida e tempo verdadeiro de aquecimento. **Motor em potência zero não elimina inércia.**
6. Calibrar rampa, potência e eventual controle de RPM sob carga; documentar com commit, log e vídeo.
7. Obter aprovação dos pilotos e gestão quanto aos botões propostos antes de nomear a 1.3 como TeleOp de competição.

## 5. Reversão e estado

Nenhuma classe 1.0/1.1 foi editada pela evolução 1.2/1.3. Retornar ao OpMode 1.1 para direção manual, ou à versão original da escola quando recuperada. As versões 1.2/1.3 não substituem automaticamente o TeleOp utilizado no Control Hub.

[Manual ilustrado](../teleop/MANUAL_COMPLETO.md) · [Relatório de reorganização](../docs/RELATORIO_REORGANIZACAO.md).
