# Evidências do protótipo 0.1

Execução em 13/10/2026 UTC (noite de 12/10 em São Paulo). Ambiente: Linux, Python 3.12.14, OpenJDK/javac 17.0.20 e Node 24.19.0. Fonte de integração do robô: commit `afbe127f1b0e2e35f07fef5ea319443ce88c29f8`.

## Resultado confirmado por logs locais

| Verificação | Resultado | Limite da evidência |
| --- | --- | --- |
| Python unittest | **22 testes passaram**, nenhum pulado | Física ideal, servidor local e JVM com camada FTC simulada |
| Node test | **6 testes passaram** | Geometria, balística e conversão de gamepad sintético |
| Sintaxe Python e JavaScript | Aprovada | Não prova layout/renderização da interface |
| Compilação de fontes reais da equipe | **9 classes compilaram** | SDK simulado: não é build Android `assembleDebug` |
| Autonomo35 completo | Concluiu em aproximadamente **12,39 s**; yaw **49,92°**, motores zerados | Modelo ideal, sem peças ou pontuação |
| TeleOp13 | Entrada sintética, rampa, neutralização e STOP passaram | Hipótese específica de montagem; não controle USB nem robô |
| Integridade do repositório | **499 verificações no original e 540 após a inclusão**, 14 pares fonte/histórico | Inventário local; sem auditoria de todo histórico Git |

Classes compiladas: `autonomo`, `Autonomo32`, `Autonomo33`, `Autonomo34`, `Autonomo35`, `TeleOp10`, `TeleOp11`, `TeleOp12` e `TeleOp13`. Os fontes e históricos dessas classes não foram editados.

Testes significativos: reta de 1 m, ré, giro no centro, arco analítico, diagonal após orientar o robô, sentido/reset dos ticks, RUN_TO_POSITION positivo/negativo, yaw cruzando ±180°, offsets rígidos do lançador, alcance balístico analítico, saturação/NaN, hardware inexistente, compilação inválida, dois gamepads, STOP durante loop Java infinito, término forçado por limite de duração e `sleep` durante INIT.

## Cenário completo do Autônomo 3.5

Configuração: bitola 0,45 m, velocidade máxima ideal 1 m/s, 1980,6 ticks/m; X inicial 1,83 m, Y 0,65 m; montagem esquerda +1/direita -1. Seleção Y=COMPLETO e confirmação A no INIT, depois START. Sem modificar fonte, sequência terminou com:

```
Autonomo 3.5 concluido
Ultima etapa: Lancar pecas
Modo executado: COMPLETO
Tempo total: 12.34 s
Yaw final: 49.92
Encoder MD: 4700
Encoder ME: 3175
```

Posição final sintética: aproximadamente X=3,310 m e Y=1,977 m. Tempo do mundo inclui pequenos intervalos de inicialização da execução (12,39 s); cronômetro interno do OpMode reportou 12,34 s. Resultado depende do agendamento da JVM; pequenas variações entre rodadas são esperadas.

MI/MS receberam comandos e foram desligados; a frase “Lancar pecas” é a etapa do código, **não evidência de três peças lançadas**. A prévia balística da interface continua independente.

Um primeiro ensaio sem confirmação foi recusado com `Modo nao confirmado: selecione e aperte A no INIT`. O teste foi corrigido para reproduzir o procedimento de operação; a proteção do código não foi removida.

## Sinais do TeleOp

O TeleOp13 conserva a mistura histórica `MD=-x-y; ME=-x+y`. Para o cenário de avanço, o teste explicita ambos os fatores de montagem como -1; para o autônomo, usa esquerda +1/direita -1. Essa diferença é uma hipótese de teste que evidencia a importância de conferir sinais. **Não prova que as duas configurações descrevam o mesmo robô físico.** A 0.1 não resolve nem altera essa divergência histórica; oferece parâmetros para investigá-la.

## Pendências

- Revisão visual/interação no navegador não executada: o ambiente gerenciado não disponibiliza a skill de controle de browser exigida pelo fluxo de preview. Não há screenshot ou inspeção visual confirmada nesta entrega.
- Windows, controles USB/Bluetooth reais, drivers e mapeamentos do navegador: não testados fisicamente.
- Não foi executado build FTC Android para o simulador; esta pasta não faz parte do aplicativo do Control Hub.
- Nenhum teste físico no robô. Colisão, atrito, bateria, câmera, ruído e alimentação física de peças não modelados.
- Workflow de CI adicionado para repetir testes; resultado remoto deve ser consultado no PR. Resultado local não é prova de job remoto aprovado.

## Reproduzir e reverter

Execute os comandos de [README](README.md). Para repetir os cenários da equipe, rode `python simulador/tests/repository_compatibility.py` a partir de um checkout completo da branch.

Para interromper: STOP e fechar o terminal Python. Para descartar o protótipo: voltar à branch principal ou reverter o commit do simulador. Não há alteração na programação instalada no robô.
