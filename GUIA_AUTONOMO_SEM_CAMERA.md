# Manual da linha 3.x — Autônomo FTC Meca Tiger SEM câmera

**Objetivo:** fornecer uma base auditável para testes, calibração física e comparação controlada com a linha de autônomos 4.x/5.x com câmera. **Não é um manual que garante vitória ou pontuação**: os mecanismos MI/MS não possuem feedback de objeto/velocidade nesta implementação.

## Versões e arquivos
| Versão | Classe | Finalidade | Câmera |
| --- | --- | --- | --- |
| 3.1 | `autonomo.java` | Base original intocada | Não |
| 3.2 | `Autonomo32.java` | Derivada angular correta, proteção de travamento, distância pela média | Não |
| 3.3 | `Autonomo33.java` | Proteção contra divergência de motores/rumo, limites e orçamento global auditável | Não |
| 3.4 | `Autonomo34.java` | 3.3 + modos de teste sem acionamento de mecanismos | Não |

Cada arquivo tem backup imutável correspondente em `historico/Autonomo_3_1.txt`, `...3_2.txt`, `...3_3.txt`, `...3_4.txt`; relatórios individuais `VALIDACAO_Autonomo_3_N.md`.

## Fluxo de execução do autônomo completo
1. Mapear `MD` (direito), `ME` (esquerdo), `MI` (coleta/alimentação), `MS` (lançador) e `imu` (REV Hub).
2. Inicializar IMU na orientação declarada `LogoFacingDirection.LEFT` e `UsbFacingDirection.UP`; validar configuração; resetar encoders antes do START. **Estas orientações devem corresponder à instalação real.**
3. Na partida, zerar yaw e iniciar relógio de 30 s.
4. Girar **-50° relativos** usando PID de yaw e watchdog; parar.
5. Capturar o yaw do robô como referência de heading, percorrer **2.000 mm** pela média dos dois encoders, com correção PID de yaw e diferença de encoders, mantendo `MI=+0.60`. Se distância atingida com erro angular acima de 5°, parar deslocamento e corrigir rumo girando, não continuar avançando.
6. Parar MI. Girar **+100° relativos** usando IMU.
7. Acionar `MS=+0.70`, aguardar 0,80 s, alimentar `MI=+0.60` durante 0,45 s por tentativa e aguardar 0,15 s entre elas, três vezes; desligar mecanismos.
8. Interromper com STOP, sensor inválido, falta de progresso, desvio ou timeout. `finally` interrompe os quatro motores.

**Observação:** o heading de deslocamento deriva do yaw relativo, não de posição global em campo. Sem câmera e sem sensor de localização absoluta, não há correção por referência externa, nem detecção de obstáculos, nem garantia de acertar o alvo.

## Estrutura do controle manual
- Todos os motores de tração operam com `RUN_WITHOUT_ENCODER`. O Java lê os encoders e calcula a potência; **não usa `RUN_TO_POSITION`**.
- **Giro:** `erro = normalizar(rumoAlvo - yawMedido)`; PID retorna comando; `MD=+comando`, `ME=-comando` (*esta convenção só será verdadeira fisicamente depois do teste de sinais*).
- **Translação:** `MD = potenciaBase + correcao`, `ME = potenciaBase - correcao`; a correção combina PID de rumo e diferença de ticks. A velocidade média usa rampas de aceleração e desaceleração.
- **PID:** `P=kp*erro`, `I=ki*integral`, `D=kd*derivadaFiltrada`; integrador limitado e rollback quando saturado, derivada angular corrigida no cruzamento ±180°. Controladores reinicializados no início de cada manobra.
- Se eixo do IMU ou motores físicos tiverem polaridade invertida, esses comandos **podem divergir**; nunca 'resolver' só trocando sinais sem registrar o teste.

## Parâmetros atuais e confirmação necessária
| Variável | Valor 3.3/3.4 | Status |
| --- | --- | --- |
| `TICKS_POR_REV_MOTOR` | 28 | Informação do projeto, conferir motor |
| `REDUCAO` | 20.0 | **Hipótese pendente**, inclui toda redução até a roda |
| `DIAMETRO_RODA_MM` | 90 | Diâmetro nominal, medir efetivo |
| `FATOR_CALIBRACAO_DISTANCIA` | 1.0 | Medir após testar percurso |
| `TICKS_POR_MM` | 1,980594847... | Cálculo dos valores teóricos acima |
| `KP_VIRADA, KI_VIRADA, KD_VIRADA` | 0.012, 0.00012, 0.0015 | Ajustes iniciais do 3.1; calibrar |
| `KP_RUMO, KI_RUMO, KD_RUMO` | 0.018, 0.00015, 0.0012 | Ajustes iniciais do 3.1; calibrar |
| `MIN_POTENCIA_VIRADA` | 0.08 | Atrito estático real pendente |
| `VELOCIDADE_COLETA` | 0.35 | Potência normalizada, não mm/s |
| `LIMITE_DIFERENCA_ENCODER_TICKS` | 220 | **Limite provisório de segurança**, ~111 mm entre rodas pela escala nominal |
| `LIMITE_DESVIO_RUMO_GRAUS` | 35 | **Limite provisório de segurança**, não tolerância final |
| `TOLERANCIA_RUMO_DESLOCAMENTO` | 5° | Tolerância de aceitação angular ao terminar translação |
| `TEMPO_LIMITE_VIRADA` | 5 s por giro | Orçamento provisório |
| `TEMPO_LIMITE_DISTANCIA` | 10 s | Orçamento provisório |
| `TEMPO_CORRECAO_FINAL_RUMO` | 1,5 s | Orçamento provisório |
| `POTENCIA_LANCAMENTO` | 0.70 | Sem controle RPM, calibrar |
| `QUANTIDADE_LANCAMENTOS` | 3 | Ciclos de alimentação comandados, não tiros confirmados |

### Conversão e calibração de percurso
Com as hipóteses de projeto: `TICKS_POR_MM = 28 * 20 / (PI * 90) ≈ 1,980595`.
- 350 mm: **~693 ticks** na média dos motores.
- 500 mm: **~990 ticks**.
- 2.000 mm: **~3.961 ticks**.
- Para calibrar percurso após aferir transmissão: comandar distância segura e medir distância **real**. Se comandou 500 mm e andou 470 mm, uma primeira aproximação é `FATOR_CALIBRACAO_DISTANCIA = 500 / 470 ≈ 1,06383`. Validar em distâncias diferentes e não alterar redução e fator simultaneamente sem necessidade.

### Temporização máxima planejada
Giro 5 s + deslocamento 10 s + eventual correção 1,5 s + giro 5 s + lançamento 2,45 s + margem 0,35 s = **24,3 s** de etapas previstas. Limite operacional do código = **29 s**, com 1 s reservado da fase autônoma de 30 s. Folga teórica = **4,7 s**. Ao falhar uma etapa, a sequência é **abortada**; não inventar pontuação por 'continuar' após uma falha.

## Protocolo físico passo a passo
**Pré-requisito:** adulto/mentor presente, campo ou bancada isolados, bateria em boas condições, Driver Station com STOP acessível, robô firme e mecanismo desobstruído. Sem pessoas na frente do lançador.

| Ensaio | Procedimento | O que registrar | Critério preliminar |
| --- | --- | --- | --- |
| A — Sinais sem potência | Usar `DiagnosticoSinais.java` já existente, com rodas movimentadas manualmente | yaw anti-horário, ticks MD/ME em avanço manual, montagem Hub | Sinais correspondem à convenção do código |
| B — Giro curto | `Autonomo 3.4 - Testes`, B + A, START; giro +20/-20 em área livre | yaw e ângulo real, timeout, sentido | Giro no sentido esperado, sem falha |
| C — Deslocamento curto | `Autonomo 3.4 - Testes`, X + A, START; 350 mm | distância real, ticks esquerdo/direito, yaw e derrapagem | Dentro da tolerância que a equipe definir previamente |
| D — Percurso 2 m | Executar versão controlada na configuração completa após aprovar B/C | diferença de ticks, heading e desvio lateral | Sem colisão, sem watchdog, repetível |
| E — MI e MS | Sem deslocamento, com proteção mecânica, verificar alimentação e RPM/velocidade se houver sensor | tempo de aquecimento, peças realmente alimentadas e saída | Três ciclos confiáveis, sem travamento |
| F — Sequência completa | `Autonomo 3.4 - Testes`, Y + A, START | logs, tempo total e trajetórias, peças efetivamente lançadas | Conclusão em até 29 s com segurança |
| G — Comparação com câmera | Mesmo ponto de partida, piso, peças, bateria e alinhamento; executar autônomo com C270 separadamente | erros por trajetória, taxa real de acerto e tempo | Comparação empírica, não apenas simulação |

Definir tolerâncias **antes** dos ensaios e registrar cada execução, inclusive falhas. Três execuções bem-sucedidas são um começo, não certificação estatística.

## Testes de software e build
```bash
node tests/autonomo32_logic_tests.mjs
node tests/autonomo32_travel_model.mjs
node tests/autonomo_sem_camera_3x.mjs
node tests/auditoria_autonomo_3x.mjs
```
O workflow `.github/workflows/ftc-sem-camera.yml` tenta compilar as versões **3.1–3.4** no projeto FTC SDK oficial usando `./gradlew :TeamCode:assembleDebug`. Só declarar compilação real aprovada após obter o log e status de sucesso do GitHub Actions ou Android Studio. Os testes JS são **modelos artificiais**.

## Modelo para diário de testes
```text
Data:
Versão:
Commit:
Modo (giro curto / distância curta / completo):
Redução real e diâmetro de roda:
Bateria:
Piso:
Ângulo inicial e final medidos:
Distância comandada/real:
Ticks MD/ME:
Tempo da etapa/total:
Quantidade de peças realmente lançadas:
Telemetria/falha:
Mudança proposta para próxima versão:
```

## Reversão e versionamento
Não sobrescrever 3.1 ou 3.2 nem descartar logs históricos. Uma versão nova só deve aumentar o número quando alterar o comportamento ou corrigir problema concreto, com `CHANGELOG`, teste e cópia histórica. A branch de trabalho é `desenvolvimento/autonomo-sem-camera-3x-20261012`; integrar à `main` apenas após revisão/consentimento conforme `AGENTS.md`.
