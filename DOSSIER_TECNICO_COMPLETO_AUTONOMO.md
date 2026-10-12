# DOSSIÊ TÉCNICO INTEGRAL — PROGRAMAÇÃO DO AUTÔNOMO MECA TIGER FTC #32578

**Engenharia de software, evolução histórica, controle, simulações, compilação, calibração, validação e transferência de conhecimento**  
**Temporada:** BIOBUZZ 2026–2027 | **Consolidação documental:** 12 de outubro de 2026  
**Âmbito:** Autônomo FTC do robô Mequinha; versões históricas 2 e 3, linha sem câmera 3.1–3.5 e linha com câmera 4.0–4.2 e 5.0–5.1.

> **Natureza do documento.** Este é o relatório-mestre de engenharia construído a partir dos arquivos Java, históricos, relatórios e execuções verificáveis do repositório. Ele não é um certificado de precisão física, homologação de competição nem substituto das regras de segurança e do manual de jogo. Quando uma medida não foi feita na bancada, ela é identificada como **hipótese/parâmetro teórico**.
>
> **Leitura essencial em conjunto:** [Manual Geral de Calibração e Testes](MANUAL_GERAL_CALIBRACAO_TESTES.md), [Relatório de Validação de Software](RELATORIO_GERAL_VALIDACAO_SOFTWARE.md), [Ficha de Ensaio](MODELO_REGISTRO_ENSAIO_AUTONOMO.md), [Índice de Códigos Integrais](INDICE_COMPLETO_CODIGOS_AUTONOMO.md).

---

## 1. Escopo, objetivo e estado final do desenvolvimento

A meta foi construir, de maneira evolutiva, um autônomo com **tração diferencial de duas rodas motrizes**, movimentação por leitura manual de encoders, manutenção do rumo por IMU e PID em Java, coleta/lançamento temporizados e, em uma linha separada, alinhamento com AprilTag Clusters da temporada por câmera USB Logitech C270.

A estratégia-base preservada nas famílias de execução é:

1. Inicializar quatro motores, IMU, sensores e relógio; validar configuração.
2. Girar **−50°** em relação ao yaw inicial.
3. Registrar o rumo para o trajeto retilíneo.
4. Avançar **2.000 mm** com potência-base nominal de **0,35**, acionando MI na etapa de coleta.
5. Parar a tração e, se necessário, fazer correção angular final do trajeto.
6. Girar **+100°** em relação ao yaw resultante.
7. Na linha **com câmera**, procurar e alinhar-se com CELL elegível da aliança; na ausência de leitura confiável, aplicar fallback conservador por IMU.
8. Acionar MS e alimentar MI durante **três ciclos temporizados** de lançamento.
9. Desligar motores e encerrar, ou abortar com diagnóstico em qualquer falha/STOP/timeout.

**Atenção de interpretação:** são giros **relativos**, não ângulos globais absolutos de campo. Não há navegação por pose X/Y, strafing mecanum, SLAM, câmera com profundidade ou detecção de obstáculos. A sequência acima não implica que o ponto final ou os disparos atendam automaticamente à estratégia de qualquer aliança e lado do campo.

### O que já foi comprovado

- Os fontes e cópias históricas das doze versões estão versionados, com hashes de conteúdo identificados.
- **Sem câmera, 3.1–3.5:** compilação integrada ao FTC SDK oficial aprovada no [GitHub Actions #38319766443](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/actions/runs/38319766443).
- **Com e sem câmera, 3.1–5.1:** compilação conjunta aprovada no [GitHub Actions #38321732887](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/actions/runs/38321732887), incluindo as classes de diagnóstico.
- Testes matemáticos, modelos sintéticos e auditoria documental executados no CI; [auditoria documental 12 versões/87 verificações](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/actions/runs/38321932005) aprovada.
- **Não há neste repositório registros de testes físicos com medidas, telemetria e decisão de aceite vinculados aos códigos atuais.** Não foram medidos com este trabalho a direção física correta, a precisão do deslocamento, a velocidade real de MS, o FPS da C270 no robô ou a pontuação real.

### Documentos já integrantes do projeto

| Arquivo | Conteúdo |
| --- | --- |
| [Este dossiê](DOSSIER_TECNICO_COMPLETO_AUTONOMO.md) | Visão consolidada de toda a programação e do trabalho realizado |
| [Manual Geral de Calibração](MANUAL_GERAL_CALIBRACAO_TESTES.md) | Procedimentos de ensaio, fórmulas, inspeção, parâmetros e comparação |
| [Relatório Geral de Validação](RELATORIO_GERAL_VALIDACAO_SOFTWARE.md) | Auditoria de builds, modelos, hashes e níveis de evidência |
| [Ficha de Teste](MODELO_REGISTRO_ENSAIO_AUTONOMO.md) | Registro preenchível de cada execução física |
| [Guia detalhado sem câmera](GUIA_AUTONOMO_SEM_CAMERA.md) | Procedimentos específicos da família 3.x |
| [Índice dos códigos integrais](INDICE_COMPLETO_CODIGOS_AUTONOMO.md) | Acesso a todos os arquivos Java/históricos e documentos por versão |
| [CHANGELOG](CHANGELOG.md) | Registro incremental dos commits e alterações |
| [historico/](historico/) | Backups de versões e relatórios individuais |

---

## 2. Linha do tempo técnica — versão por versão

**Há duas linhas complementares, não uma única classe sendo sobrescrita.** A linha 3.x foi mantida **sem visão**, enquanto a 4.x–5.x evoluiu com visão. Os históricos 2 e 3 devem ser lidos separadamente e não instalados junto com classe homônima da 3.1.

### Versão 2 — histórico do controle inicial

**Fonte integral:** [historico/Autonomo_2.txt](historico/Autonomo_2.txt)

- Conjunto de quatro motores MD, ME, MI e MS; IMU para rumo; tração diferencial, leitura dos encoders e alimentação/lançamento por tempo.
- O arquivo histórico **já possuía constantes para P, I e D** tanto de giro quanto de deslocamento, além de correção por diferença de ticks, limites de potência e timeout.
- Conversão de deslocamento embutida inicialmente na expressão equivalente a **28 × 20 / (π × 90)** ticks/mm.
- Controle e método de sequência ainda diferentes da arquitetura consolidada na 3.1; o arquivo serve como evidência histórica, não como candidata compilada na árvore atual.
- **Ponto técnico:** não é correto afirmar que PID com os três termos surgiu somente na 3.1; a versão 2 já registrava termos I. O estágio intermediário 3 usou outro controlador.

### Versão 3 — revisão intermediária da lógica

**Fonte integral:** [historico/Autonomo_3.txt](historico/Autonomo_3.txt)

- Divisão explícita da conversão mecânica em **TICKS_POR_REV_MOTOR = 28**, **REDUCAO = 20** e **DIAMETRO_RODA_MM = 90**.
- Controle de giro intermediário com **KP_VIRADA = 0,018** e **KD_VIRADA = 0,003**. Nesse arquivo, não estão definidos os ganhos integrais do PID completo da 3.1; portanto, o controle não deve ser descrito indistintamente como o da versão 2 ou 3.1.
- Introduziu/registrou combinações próprias de tolerâncias de diferença de ticks, ultrapassagem e correções de rumo, mantendo a coleta e o lançamento temporizados.
- Preserve este arquivo para comparar a arquitetura intermediária e as razões que levaram à consolidação posterior.

### Versão 3.1 — base de referência preservada

**Java integral:** [autonomo.java](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/autonomo.java)  
**Backup:** [Autonomo_3_1.txt](historico/Autonomo_3_1.txt)

- Estabelece uma classe única de execução com funções explícitas para **inicialização, tarefas sequenciais, giro relativo, deslocamento, coleta, lançamento, IMU, encoders e desligamento**.
- PID manual com P, I e D nos dois controladores: giro (**0,012 / 0,00012 / 0,0015**) e rumo durante a translação (**0,018 / 0,00015 / 0,0012**).
- **Integral limitada a 250**, **filtro da derivada de 0,08 s** e mecanismo de anti-windup por desfazer a integração ao saturar no sentido do erro.
- Curvas de aceleração/desaceleração e vigilância de progresso de motor, potência limitada, tolerâncias, tempo global e parada segura em blocos de finalização.
- A estratégia de −50°, 2 m e +100° está explicitamente parametrizada na classe. Esta versão foi preservada como referência, sem ser reescrita para introduzir câmera.
- **Risco que motivou a 3.2:** a condição de chegada do trajeto poderia insistir na translação mesmo após a distância média ser suficiente caso os requisitos de rumo/diferença de encoders não fossem simultaneamente satisfeitos.

### Versão 3.2 — correções essenciais SEM câmera

**Java integral:** [Autonomo32.java](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo32.java)  
**Relatório:** [Validação 3.2](historico/VALIDACAO_Autonomo_3_2.md)

- A conclusão da distância linear passou a usar a **média dos encoders das duas rodas**. Ao alcançar a distância, a tração é parada; erro angular residual é corrigido **girando parado** por até 1,50 s, não continuando a avançar para satisfazer múltiplas condições.
- A derivada angular do PID passou a usar **diferença normalizada** entre erros, evitando um salto artificial de aproximadamente 358° ao cruzar o limite −180°/+180°.
- Watchdog no giro: interrompe se o erro persistir acima de 10° sem melhoria mínima por 1,20 s; evita insistir cegamente quando sinal do IMU ou dos motores está invertido.
- Reservou o tempo previsto de lançamento durante movimentos; validou finitude de ângulos, escala de ticks e distâncias, e fez tratamento de exceções/STOP.
- Introduziu fator de calibração separado (**FATOR_CALIBRACAO_DISTANCIA = 1,0**) sem falsificar a redução mecânica.
- **Resultado esperado:** menor risco de ultrapassagem do deslocamento e de giros sem progresso, mas sem prova de precisão física.

### Versão 3.3 — confiabilidade SEM câmera

**Java integral:** [Autonomo33.java](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo33.java)  
**Relatório:** [Validação 3.3](historico/VALIDACAO_Autonomo_3_3.md)

- Reduziu timeout por giro **7,0 → 5,0 s** e tempo máximo do deslocamento **12,0 → 10,0 s**.
- Introduziu **validarConfiguracao()**, que verifica valores finitos, potência e constantes mecânicas admissíveis antes de armar os movimentos.
- Computa **tempoPiorCaso() = 24,30 s**, envolvendo dois giros, deslocamento, eventual correção de rumo, alimentação e margem de segurança, abaixo dos 29 s de execução previstos.
- Acrescentou interrupção preventiva se **|ticksMD − ticksME| > 220 ticks** ou se **|erro de rumo| > 35°** durante translação.
- Telemetria registra diferença de encoders; o lançamento passa a ser descrito com precisão como **“ciclos de alimentação comandados”**, não como prova de três peças disparadas.
- Os limiares **220 ticks** e **35° são valores provisórios**, não resultados de calibração. O limite de 220 ticks pode detectar erro bruto sem ser uma tolerância de posicionamento final.

### Versão 3.4 — engenharia por ensaios isolados SEM câmera

**Java integral:** [Autonomo34.java](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo34.java)  
**Relatório:** [Validação 3.4](historico/VALIDACAO_Autonomo_3_4.md)

- Acrescentou enum de modos selecionáveis no **INIT**, sem precisar modificar Java para cada teste:
  - **B:** giro relativo **+20°**, seguido de **−20°**, sem mecanismos MI/MS.
  - **X:** avanço **350 mm** com potência-base configurada de **0,22**, sem mecanismos.
  - **Y:** rotina completa −50°, 2 m, +100°, coleta e lançamento.
  - **A:** confirma a seleção antes do START; iniciar sem confirmação bloqueia a rotina.
- Mantém matemática do PID, guardas temporais, encoders e critérios de erro da 3.3.
- Finalidade: detectar sinais incorretos e calibrar subsistemas sem executar imediatamente a rotina completa.

### Versão 3.5 — bancada completa SEM câmera

**Java integral:** [Autonomo35.java](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo35.java)  
**Relatório:** [Validação 3.5](historico/VALIDACAO_Autonomo_3_5.md)

- Mantém **B**, **X**, **Y**, com novo **D-pad CIMA** para **TESTE_LANCADOR**, que interrompe a tração e aciona apenas MS/MI pelos três ciclos temporizados.
- Melhorou a confirmação pré-START: o botão **A requer uma NOVA pressão depois de alterar o modo**; segurar A durante a troca não confirma acidentalmente o novo modo.
- Passou a medir **tempo e resultado por tarefa** na telemetria (ElapsedTime dentro do executor de tarefas).
- Exceções incluem a etapa ativa no motivo de falha.
- Candidata preferida para **calibração física sem câmera**. A presença de um modo de teste do lançador **não significa que MS só dispara quando há alvo**; esse modo pode lançar peças reais e exige ambiente protegido.

### Versão 4.0 — introdução da câmera e AprilTags

**Java integral:** [Autonomo4.java](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo4.java)  
**Relatório:** [Validação 4.0](historico/VALIDACAO_Autonomo_4_0.md)

- Novo OpMode sem substituir a 3.1. Integra **Logitech C270**, **VisionPortal**, **AprilTagProcessor**, webcam **Webcam 1**, resolução **640 × 480** e detecções **AprilTagClusterDetection** da temporada.
- Processa nomes de clusters, bearing, range, yaw e roll; rejeita CELL invertida por **|roll| ≥ 90°** e alvos de outra estratégia.
- Adiciona alinhamento angular visual por potência diferencial, **offset do eixo da câmera** e retorno por IMU quando a visão está indisponível.
- Emprega redução teórica dos cartuchos REV **3,61 × 5,23 = 18,8803:1**, a confirmar fisicamente.
- Problemas detectados posteriormente: leitura por **getDetections()** pode reutilizar detecções anteriores; soma pessimista de timeouts excede janela autônoma segura; seleção originalmente fixa de cluster.
- O relatório histórico da 4.0 contém modelos/simulações descritos à época; **nem todas as contagens antigas foram reproduzidas por script verificável**. O build Java atual foi confirmado em CI integrado, não a precisão visual real.

### Versão 4.1 — visão com dados frescos e correções geométricas

**Java integral:** [Autonomo41.java](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo41.java)  
**Relatório:** [Validação 4.1](historico/VALIDACAO_Autonomo_4_1.md)

- Substitui **getDetections() por getFreshDetections()**, impedindo que o código confunda a repetição de resultados antigos com novas observações reais.
- Sem novo alvo: para de comandar giro visual após **0,08 s** desde a última detecção, invalida a confirmação após **0,18 s**, e tenta fallback ao IMU após **0,35 s** de perda já tendo iniciado movimento.
- Corrige a derivada angular no wrap, como na linha 3.2.
- Substitui a redução aproximada por razão de dentes: **(76/21) × (68/13) ≈ 18,930403:1**, multiplicada por **REDUCAO_TRANSMISSAO_RODAS**, inicialmente 1,0.
- Cria diagnóstico independente para verificar C270/clusters antes de integrar visão e movimento.
- O limite total pessimista **ainda excedia a janela útil** (registro: 34,05 s), o que motivou 4.2.

### Versão 4.2 — orçamento temporal e watchdog para visão

**Java integral:** [Autonomo42.java](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo42.java)

- Timeout dos giros reduzido a **4,50 s cada**, deslocamento para **9,50 s**; preserva correção final de rumo (até 1,50 s), busca visual (até 2,50 s) e eventual retorno IMU (até 1,60 s).
- Watchdog de rumo interrompe giro que não reduza erro angular de forma suficiente por **1,20 s** quando erro permanece grande.
- Durante visão, verifica o **tempo disponível para retorno de rumo e lançamento**; não deve consumir a janela final apenas tentando detectar alvo.
- **Orçamento teórico somado com margem: 26,90 s**, menor que 29 s operacionais. Isso NÃO garante que a rotina terminará fisicamente, pois uma etapa pode abortar, o scheduler pode atrasar, e peças não possuem confirmação de saída.

### Versão 5.0 — configuração por aliança e lado no INIT

**Java integral:** [Autonomo50.java](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo50.java)

- D-pad ESQUERDA escolhe **RED**, DIREITA escolhe **BLUE**.
- D-pad CIMA escolhe preferência **SCORING**, BAIXO **AUDIENCE**.
- A telemetria mostra a configuração antes do START, sem recompilar para trocar aliança/lado.
- Atenção: **escolher outro lado não espelha automaticamente a trajetória geométrica**. Os ângulos −50°/2 m/+100° permanecem constantes.

### Versão 5.1 — escolha dinâmica de CELL pontuável

**Java integral:** [Autonomo51.java](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo51.java)  
**Relatório:** [Validação consolidada 4.2–5.1](historico/VALIDACAO_VERSOES_4_2_A_5_1.md)

- Exige **A para confirmar** aliança e lado durante INIT; se não houver confirmação, a rotina não parte.
- Busca AprilTag Cluster cujo nome pertence à **aliança escolhida** e cujo roll descreve CELL pontuável (**|roll| < 90°**).
- Com **BUSCAR_CELL_PONTUAVEL = true**, o código aceita a CELL elegível de qualquer um dos dois lados da **mesma aliança**, escolhendo aquela cujo bearing tem menor diferença para o offset óptico declarado.
- Mantém watchdog, limites de quadro novo, retorno IMU, reserva de tempo e lançamento temporal.
- **Diferença importante em relação à 3.5:** na versão 5.1, a confirmação A é checada por nível no INIT; manter o botão pressionado ao alterar a seleção pode reconfirmá-la. O mecanismo de nova borda do A implementado na 3.5 **ainda não foi aplicado na 5.1**. Isso é uma oportunidade concreta para futura revisão de segurança, sem adulterar a versão histórica.
- A câmera mede direção relativa do alvo; **não** calcula automaticamente distância ideal do lançador, velocidade da peça ou correção de trajetória X/Y.

---

## 3. Arquitetura Java e ciclo de vida

### 3.1 Organização lógica do projeto

A família de produção mantém as responsabilidades na própria classe de OpMode, para facilitar a implantação inicial sem exigir uma árvore grande de subsistemas:

| Elemento Java | Responsabilidade | Evidência no código |
| --- | --- | --- |
| Anotação @Autonomous e extends LinearOpMode | Registrar o modo no Driver Station e controlar execução síncrona | Classes autonomo, Autonomo32–35, Autonomo4/41/42/50/51 |
| runOpMode() | Mapear hardware, inicializar, aguardar START, rodar sequência, encerrar | Todas as classes instaláveis |
| configurarHardware() | Buscar MD, ME, MI, MS; definir direção, zero power e RunMode | 3.1+ |
| configurarImu() / obterImu() | Aplicar orientação do Hub e verificar IMU; fallback de nome em alguns códigos com câmera | 3.1+ e 4.x+ |
| validarConfiguracao() / tempoPiorCaso() | Barrar constantes inválidas e tempos inviáveis | 3.3+ e 4.x+ |
| sequencia(), tarefa() | Executar etapas ordenadas e interromper quando uma retornar false | 3.1+ |
| virarRelativo() / virarParaRumo() | Girar pelo yaw usando PID e potência diferencial | 3.1+, virarParaRumo de 3.2+ |
| andarComRumo() | Avançar por média de ticks e manter heading por PID | Todas as linhas |
| coletarDuranteDeslocamento() | Acionar MI durante avanço e interromper em finally | 3.1+ |
| lancarPecasPorTempo() | Aquecer MS, alimentar MI em ciclos, respeitar tempo/STOP | Todas as linhas |
| ControladorPID.calcular() | Computar P, I e D, filtro e saturação | 3.1+ e versões correspondentes |
| lerYaw(), normalizarAngulo(), reiniciarEncoders() | Leitura e funções auxiliares de geometria | Todas as linhas |
| pararTracao(), pararMecanismos(), pararTudo() | Zerar atuadores; evitar movimento residual | 3.1+ |
| configurarVisao(), fecharVisao() | Criar AprilTagProcessor, VisionPortal e liberar câmera | 4.0+ |
| cameraEstaStreaming(), lerMelhorAlvo(), alinharComVisaoOuFallback() | Verificar câmera, validar clusters, comandar mira e fallback | 4.0+ |
| clusterAlvoDaEstrategia(), ehClusterBiobuzz(), dadosVisaoValidos() | Seleção e integridade do alvo da temporada | 4.0+ |
| DiagnosticoSinais | Mostrar yaw e ticks sem movimentação motorizada | OpMode independente |
| DiagnosticoAprilTag | Mostrar leitura dos clusters sem deslocar o robô | OpMode independente |

Todos os mecanismos físicos têm nomes de configuração de hardware do Driver Station. **Nomes Java iguais não garantem que a porta configurada no Control Hub corresponde ao motor certo**. A conferência do hardwareMap é obrigatória.

### 3.2 Fluxo nominal do OpMode

~~~text
INIT
  ├─ hardwareMap MD, ME, MI, MS, imu
  ├─ direção de motores, BRAKE, RUN_WITHOUT_ENCODER
  ├─ orientação IMU LEFT/UP; preparar PID / encoders
  ├─ validar constantes e orçamento (3.3+)
  ├─ câmera VisionPortal, quando aplicável (4.x/5.x)
  └─ seleção e confirmação do modo/aliança (3.4+ e 5.0+)

START
  ├─ reiniciar referência yaw e relógio
  ├─ virar -50° (PID, timeout, STOP e watchdog conforme versão)
  ├─ fixar rumo de percurso
  ├─ andar 2.000 mm e coletar com MI
  ├─ corrigir heading sem translação, se necessário (3.2+)
  ├─ virar +100°
  ├─ [só com câmera] procurar CELL, alinhar ou fallback por IMU
  ├─ aquecer MS e alimentar MI em 3 ciclos por tempo
  └─ encerrar

FALHA / STOP / TIMEOUT
  ├─ registrar etapa e motivo
  ├─ interromper movimentos dependentes
  └─ finally: potência zero em tração e mecanismos; fechar visão
~~~

**Advertência sobre “árvore de comportamentos”:** a organização aqui possui **sequência de tarefas com condições e abort**, inspirada em composição comportamental; **não é um motor genérico de árvore com nós Parallel/Selector/Repeat como o simulador**. Não reivindicar capacidade que não aparece nas classes.

## 4. Cinemática diferencial e convenção de sinais

Robô de duas rodas motrizes laterais, um apoio passivo, sem rodas mecanum. Deslocamento lateral direto (*strafe*) não é fisicamente comandável pela tração atual.

No código, após configurar as direções Java:

- **Avanço:** potência normalizada em MD e ME com o mesmo sinal, ambos em avanço físico **se a montagem corresponder à convenção**.
- **Giro no próprio eixo:** MD recebe +u e ME recebe −u.
- **Avanço com correção:** MD recebe uma potência-base acrescida de correção e ME recebe a mesma base diminuída dessa correção.
- Diferença de potência/caminho entre lados gera variação de yaw. O sinal angular físico deve ser medido.

A relação cinemática ideal de yaw, com entre-eixos de tração b, é:

\[
\dot\theta = \frac{v_D-v_E}{b}
\]

A equação é uma **aproximação** de tração diferencial sem escorregamento. Na bancada, raio efetivo, separação entre pontos de contato, atrito e montagem podem alterar a resposta.

**Risco conhecido:** em testes anteriores de assistência de rumo no TeleOp, houve comportamento angular inesperado. Portanto, não assumir que o sinal de MD/ME versus yaw está fisicamente validado só porque o Java compilou ou o modelo converge. Conferir com chassi movimentado manualmente e testes curtos supervisionados.

## 5. Fundamentos e implementação do PID manual

### 5.1 Por que PID — e por que ele é manual

A linha 3.1+ usa uma implementação própria em Java para calcular potência, e **não** um PID interno de motor comandado por RUN_TO_POSITION. Os encoders são realimentação medida com RUN_WITHOUT_ENCODER.

Termos:

\[
e(t)=\operatorname{normalizar}(\theta_\mathrm{alvo}-\theta_\mathrm{medida})
\]

\[
u(t)=K_p e(t)+K_i \int e(t)\,dt+K_d \frac{de(t)}{dt}
\]

**P — Proporcional:** reage à magnitude do erro; **I — Integral:** compensa erro acumulado/residual; **D — Derivativo:** amortece a aproximação e reage à mudança do erro.

Valores da 3.1–5.1 para o PID manual de rumo são iguais nos arquivos examinados (apesar de outros limites terem mudado):

| Ganho | Giro no próprio eixo | Correção de rumo durante o trajeto |
| --- | ---: | ---: |
| Kp | 0,012 | 0,018 |
| Ki | 0,00012 | 0,00015 |
| Kd | 0,0015 | 0,0012 |

**Isso NÃO representa ajuste físico concluído**. São valores-base históricos utilizados nos testes sintéticos.

### 5.2 Tratamento numérico real do controlador

Na classe ControladorPID de 3.5 e 5.1:

1. Inicializa o erro anterior igual ao erro medido na primeira amostra.
2. Calcula a diferença angular entre erros; desde 3.2/4.1 esse delta é normalizado para evitar salto perto de ±180°.
3. Limita o intervalo de amostragem usado pelo controle para a faixa **0,001–0,10 s**, evitando divisão por zero e picos por pausas longas.
4. Filtra a derivada usando coeficiente \(\alpha=dt/(\tau+dt)\), com \(\tau=0,08\) s, e atualização \(D_f \gets D_f+\alpha(D_\mathrm{bruta}-D_f)\).
5. Acumula a integral e a limita em **−250 a +250**.
6. Calcula a saída livre P+I+D e a **satura** à potência máxima do modo.
7. Se a saída saturou no sentido do erro, desfaz o último incremento da integral (**anti-windup por rollback**).
8. Guarda o erro atual para a próxima amostra e disponibiliza estimativa da velocidade de yaw como o negativo da derivada filtrada do erro sob alvo fixo.

A derivada angular correta para amostras de erros +179° e −179° deve perceber variação de cerca de +2° ou −2°, conforme ordem, não um salto de 358°. Esse foi um defeito numérico corrigido na linha 3.2 e depois na 4.1.

**Limitação:** getVelocidadeYaw() estima a velocidade do robô a partir da derivada de erro. Não é leitura direta do sensor de velocidade angular nem uma medida calibrada. Filtragem, ruído e target variável afetam seu significado.

### 5.3 Tolerâncias, potência mínima e watchdog

| Parâmetro | Sem câmera 3.5 | Com câmera 5.1 | Significado |
| --- | ---: | ---: | --- |
| Potência mínima de giro | 0,08 | 0,10 | Vencer atrito estático presumido |
| Potência máxima de giro | 0,50 | 0,50 | Limitar autoridade de correção |
| Tolerância de erro de giro | 2,0° | 2,0° | Faixa angular de chegada |
| Tolerância da velocidade estimada | 8,0°/s | 8,0°/s | Evitar dar sucesso em passagem rápida |
| Tempo estável de giro | 0,12 s | 0,12 s | Confirmação contínua da estabilidade |
| Watchdog de giro | 1,20 s sem melhoria suficiente | 1,20 s | Aborta se erro continuar grande e sem progresso |
| Tolerância final de rumo do trajeto | 5,0° | 5,0° | Aceitar ou corrigir yaw ao terminar distância |
| Saturação da correção diferencial | ±0,25 | ±0,25 | Evitar correção excessiva |

Os limiares são **configurações atuais** e precisam ser confrontados com atrito, polaridade e inércia reais. Não alterar ao mesmo tempo Kp, Ki, Kd e potência mínima: o resultado deixaria de apontar uma causa.

## 6. Distância, encoder, odometria relativa e matemática

### 6.1 Transformação de distância em ticks

\[
\mathrm{ticks/mm} = \frac{28\,G_\mathrm{total}}{\pi D_\mathrm{efetivo}}\times F_\mathrm{distância}
\]

Onde **28** é contagem no eixo do motor, **G_total** é a redução efetiva até a roda, **D** o diâmetro em mm e **F** o fator fino de calibração (inicial 1,0).

| Famílias | Hipótese de redução | Ticks/mm | Alvo teórico 2.000 mm |
| --- | ---: | ---: | ---: |
| 3.1–3.5 | 20:1 | 1,980595 | **3.961 ticks** |
| 4.0 | 3,61×5,23 = 18,8803:1 | 1,869711 | **3.739 ticks** |
| 4.1–5.1 | (76/21)×(68/13) = 18,930403:1, transmissão externa 1,0 | 1,874673 | **3.749 ticks** |

A mesma distância nominal pode significar **alvos de encoder diferentes** nas duas famílias. Isso não prova que alguma versão esteja errada; significa que é necessário **confirmar a transmissão única realmente instalada** e equalizar as premissas antes de comparação empírica.

### 6.2 Por que usar média dos dois encoders

Para dois deslocamentos de roda medidos em ticks, o centro do robô percorre idealmente distância proporcional a:

\[
s_\mathrm{centro} \propto \frac{\mathrm{ticks}_{MD}+\mathrm{ticks}_{ME}}{2}
\]

No Java, o cálculo em double evita estouro na soma de ints; valores negativos incompatíveis com avanço são tratados como falha. Na versão 3.2+, ao alcançar o alvo médio (com tolerância de 25 ticks), o programa **para a translação**, mesmo que a diferença entre as rodas ou o rumo demande uma correção; o heading residual é corrigido pelo giro separado.

**O que melhorou:** a versão 3.1 podia continuar avançando apenas para satisfazer mais de uma condição de chegada, aumentando risco de ultrapassagem. A mudança 3.2 elimina essa causa lógica.

**O que não resolve:** se uma roda escorrega, o encoder mede rotação do motor, não distância física exata. Trajetória real e pose X/Y permanecem sujeitas a derrapagem.

### 6.3 Rampas e correções

- A potência-base é modulada por rampa de aceleração inicial (**0,40 s**) e frenagem ao longo da fração final do trajeto (**0,20** da distância).
- A correção total combina o PID de yaw com pequena contribuição da diferença de encoders, limitada para preservar movimento positivo das rodas.
- O PID evita saturação excessiva, mas não substitui a conferência de motores que andam em velocidades físicas diferentes por parafusos soltos, atrito, engrenagens ou bateria.
- Em 3.3/3.5, diferença superior a **220 ticks** ou erro de heading superior a **35°** interrompe o deslocamento. Esses são limites de proteção, não critério de precisão fina.
- Um encoder que não progride é monitorado por timeout; a regra exata e temporização podem variar entre versões, consultar o Java da versão utilizada.

## 7. Coleta, MS e três ciclos de alimentação

**Motores:** MI faz coleta/alimentação; MS é o lançador. No caminho de coleta, MI é acionado durante os 2.000 mm e desligado ao final (inclusive em falha). Para lançamento, MS acelera primeiro, MI alimenta em ciclos.

Constantes dos arquivos mais recentes:

| Variável | Valor | Observação |
| --- | ---: | --- |
| POTENCIA_INTAKE | 0,60 | Potência normalizada, não velocidade |
| POTENCIA_LANCAMENTO | 0,70 | Potência normalizada, não RPM |
| TEMPO_ACELERACAO_LANCADOR | 0,80 s | Aquecimento temporizado |
| TEMPO_ALIMENTACAO_POR_PECA | 0,45 s | Comando MI por ciclo |
| TEMPO_ENTRE_PECAS | 0,15 s | Pausa entre ciclos |
| QUANTIDADE_LANCAMENTOS | 3 | Quantidade de ciclos comandados |

A duração nominal é **0,80 + 3×0,45 + 2×0,15 = 2,45 s**.

**Limitação crítica:** o código não detecta a saída de cada peça nem monitora a velocidade mecânica efetiva do lançador. Potência 0,70 **não** é sinônimo de velocidade final constante: a tensão, a carga e o atrito mudam a rotação. Declarar três peças pontuadas ou lançadas sem observação real seria incorreto.

O teste exclusivo de MI/MS da 3.5 deve ser conduzido somente em bancada de lançamento protegida. **Nenhuma opção de teste deve ser iniciada com pessoas na linha de tiro.**

## 8. Câmera, AprilTag Clusters e lógica de alvo BIOBUZZ

### 8.1 Infraestrutura de visão

A linha 4.x/5.x usa:

- Logitech **C270** pelo hardwareMap com nome **Webcam 1**, USB no Control Hub ou caminho previsto pelo SDK.
- **AprilTagProcessor.Builder** para detecção, decimação configurável e **VisionPortal.Builder** para gerenciar o fluxo e a resolução **640 × 480**.
- **AprilTagClusterDetection** com metadata, ftcPose e atributos **range, bearing, yaw, roll**; filtro de nomes da temporada.
- **Estado STREAMING** para habilitar controle visual. Indisponibilidade causa fallback, não erro irrecuperável obrigatório.
- Leitura de resultados **frescos** em 4.1+ por getFreshDetections(), evitando validar uma imagem antiga repetida como se fossem quadros distintos.

### 8.2 Os quatro clusters e seleção

| Aliança | CELL | Nome no código |
| --- | --- | --- |
| RED | SCORING | RED SCORING |
| RED | AUDIENCE | RED AUDIENCE |
| BLUE | SCORING | BLUE SCORING |
| BLUE | AUDIENCE | BLUE AUDIENCE |

O programa rejeita candidatos sem dados coerentes, com aliança errada ou com **|roll| ≥ 90°**; a 5.1 permite escolher dinamicamente a CELL adequada de sua aliança e minimiza o erro de bearing relativo ao offset óptico.

A aliança e lado são selecionados no INIT do Driver Station pela 5.0+, e a 5.1 exige confirmação A. **Selecionar o lado não recalcula automaticamente as coordenadas/giros do percurso**, portanto a geometria de partida precisa ser validada para cada configuração.

### 8.3 Controle visual e segurança

- Corrige bearing por potência diferencial (robô gira sobre o eixo, não anda lateralmente).
- Kp visual nominal **0,010**, potência mínima **0,10**, potência máxima **0,25**, tolerância **±2°**, confirmação de detecção por **0,18 s** e estabilidade de alinhamento por **0,20 s**.
- Na 4.1–5.1, uma detecção ausente impede manter comandos antigos indefinidamente: **0,08 s** de idade do último comando, **0,18 s** para invalidar confirmação e **0,35 s** de perda do alvo após movimento para retornar rumo pelo IMU.
- Timeout máximo de visão **2,50 s**; retorno IMU até **1,60 s**.
- O fallback preserva a segurança e a continuidade planejada, **não demonstra que a mira ficou correta**.

**Questões pendentes:** a câmera pode não estar alinhada com o eixo de MS; montagem tem offset e paralaxe; visão sem posição absoluta não informa distância ótima; iluminação/FPS reais não foram medidos. O alinhamento ótimo depende de ensaio de campo real.

## 9. Orçamento de tempo da fase AUTO

Autônomo da temporada: 30 s no projeto; o software reserva 1 s e considera até 29 s operacionais.

| Família | Giro 1 | Avanço | Correção final | Giro 2 | Visão | Retorno IMU | Alimentação | Margem | Soma pessimista indicada |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| 3.3–3.5 | 5,0 | 10,0 | 1,5 | 5,0 | — | — | 2,45 | 0,35 | **24,30 s** |
| 4.0–4.1 | 7,0 | 12,0 | 1,5 | 7,0 | 2,5 | 1,6 | 2,45 | — | **34,05 s** |
| 4.2–5.1 | 4,5 | 9,5 | 1,5 | 4,5 | 2,5 | 1,6 | 2,45 | 0,35 | **26,90 s** |

São **somas dos limites configurados e de tempos temporizados**, não medição do robô. As guardas também precisam impedir tentar um lançamento se não restar a janela mínima. O sucesso da lógica não implica sucesso físico: se o giro demora demais, sai da tolerância, câmera perde quadros ou MI emperra, a sequência pode abortar de forma correta, sem lançar as três peças.

## 10. Provas de software: o que de fato foi testado

### 10.1 Compilação FTC — evidência real de integração Java

**Sem câmera:** [GitHub Actions #38319766443](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/actions/runs/38319766443) — copiou 3.1–3.5 para o projeto FTC oficial, compilou **TeamCode:compileDebugJavaWithJavac** e **TeamCode:assembleDebug**, finalizando com **BUILD SUCCESSFUL in 2m 16s**.

**Conjunta com câmera:** [GitHub Actions #38321732887](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/actions/runs/38321732887) — copiou **todas as classes Java instaláveis e diagnósticos** para o TeamCode oficial; ambas as tarefas Java/Gradle concluíram com **BUILD SUCCESSFUL in 2m 42s**.

**Auditoria reexecutada:** [GitHub Actions #38321932005](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/actions/runs/38321932005), jobs de software concluídos; script de documentação conferiu **12 versões e 87 verificações**.

**Limite de evidência:** build em runner Linux com JDK 17 e checkout oficial do SDK não prova que o projeto Android Studio local da equipe usa a mesma revisão, nem instalação bem-sucedida no Control Hub. O workflow não fixa SHA exato do SDK público; registrar essa informação para release reproduzível.

### 10.2 Testes matemáticos e simulações executados

| Bateria | Evidência | O que testa | Limitação |
| --- | --- | --- | --- |
| 3.2: lógica e trajetórias | tests/autonomo32_logic_tests.mjs; tests/autonomo32_travel_model.mjs | Ângulos, ticks/mm, PID/modelo, erros e progresso | Modelos JS artificiais |
| 3.3–3.5: regressão | tests/autonomo_sem_camera_3x.mjs | **21 verificações** e **5.000 cenários** em cinco condições, incluindo falhas forçadas | Não representa hardware real |
| Auditoria código 3.x | tests/auditoria_autonomo_3x.mjs | **58 verificações** de estrutura/backup/ausência de câmera | Não prova desempenho |
| 4.1: lógica/visão | tests/autonomo41_logic_tests.mjs | **7 assertions**, **7.500 cenários sintéticos** (normal, perda de frames, sinal invertido) | Sem C270 real |
| 5.0: tempo/controle | tests/autonomo50_synthetic_tests.mjs | **22 assertions**, cronogramas e modelo de giro | Tempos e dinâmica artificiais |
| 5.1: CELL | tests/autonomo51_cell_tests.mjs | **10.000 listas de detecção simuladas** e verificações de aliança/roll/bearing | Dados inventados pelo gerador de teste, não frames |
| 5.1: PID | tests/autonomo51_pid_model.mjs | **20.000 cenários de giro com planta PID simulada** | Ruído/atrito/inércia artificiais |
| Documentação | tests/auditoria_documentacao_geral.mjs | **12 versões/87 verificações** (hash, código histórico, links) | Não substitui leitura e ensaio humano |

O fato de um modelo artificial obter 100% de sucesso nominal **não é estatística real de competição**. Seu papel é procurar contradições de algoritmo, casos-limite numéricos, tempos inviáveis e regressões.

### 10.3 Exemplos importantes de falhas modeladas

1. Sinal do IMU invertido: watchdog identifica falta de aproximação ao alvo e interrompe.
2. Motor direito bloqueado: encoders divergem e o percurso é interrompido, sem declarar chegada.
3. Encoder congelado: progresso e coerência dos ticks não atendem às guardas.
4. IMU com leitura inválida: abort por valor não finito.
5. Quadros visuais intermitentes: interrupção de comando antigo, invalidação da confirmação e fallback quando necessário.
6. Alvo da aliança adversária ou CELL invertida: deve ser rejeitado pela lógica de seleção.
7. Estouro de tempo: rotina não deve consumir a janela reservada ao lançamento sem decisão explícita.

A validação física ainda precisa testar como **realmente** se comportam essas falhas e interrupções.

## 11. Manual resumido de calibração — ordem de bancada

**O [Manual Geral de Calibração e Testes](MANUAL_GERAL_CALIBRACAO_TESTES.md) contém o protocolo detalhado, por isso o fluxo abaixo é a versão executiva que deve acompanhar este dossiê.**

### A. Preparação e segurança
Inspecionar transmissão, parafusos, rodas, fiação, bateria, Control Hub, Driver Station, área livre, STOP e segurança da linha de tiro. Registrar versão e commit que serão instalados. Nenhum teste físico deve ser conduzido sem operador responsável presente.

### B. Sinais e IMU, sem potência
Executar [DiagnosticoSinais.java](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/DiagnosticoSinais.java). Mover rodas e chassi à mão e verificar se yaw anti-horário cresce e se ticks MD/ME crescem no sentido de avanço esperado. Corrigir hardware/parametrização antes de liberar giros.

### C. Giro curto 3.5
INIT → B → soltar A → A → START. Medir separadamente +20° e −20° com marcador externo, yaw do IMU, tempo de convergência, oscilação e sentido. Fazer vários ensaios comparáveis, documentando erros e STOP. Só depois testar ângulos da estratégia completa.

### D. Deslocamento curto 3.5
INIT → X → soltar A → A → START. Percurso nominal de 350 mm. Marcar posição inicial e final do **centro do robô**, desvio lateral, ticks MD/ME, yaw, tensão, erro e tempo. Repetir; não usar apenas a telemetria de ticks como “medição real” do deslocamento.

### E. Calibrar distância real
Confirmar redução completa até a roda e diâmetro efetivo. Com motor/encoder/IMU coerentes, ajustar FATOR_CALIBRACAO_DISTANCIA em **nova versão** após medir trajetos de pelo menos duas distâncias. Aproximação inicial: novo fator = fator anterior × distância comandada ÷ distância real. Nunca mascarar uma roda escorregando ou motor solto com ganho global.

### F. Ajustar PID
Primeiro sinal e potência mínima; depois Kp, Kd e por último Ki, alterando **um parâmetro por vez**. Medir assentamento, tempo de giro, ultrapassagem angular, oscilação e diferença entre lados sob carga. Tolerância e critério de aceite devem ser fixados ANTES da rodada.

### G. Lançador isolado 3.5
Em área protegida, INIT → D-pad CIMA → soltar A → A → START. Medir tempo até velocidade útil, recuperação entre peças, número real de saídas e travamento. Os motores de tração não são comandados para deslocamento nesse modo. Nunca colocar pessoas na linha de lançamento.

### H. C270 isolada
Executar [DiagnosticoAprilTag.java](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/DiagnosticoAprilTag.java). Medir STREAMING, nomes corretos, roll da CELL, range em polegadas, bearing em graus, FPS, latência e offset câmera-lançador. Testar sem alvo e com aliança oposta. **Só depois** passar a giro motorizado guiado por visão.

### I. AUTO completo e comparação
Depois dos itens anteriores, comparar **3.5 sem câmera vs. 5.1 com câmera** usando mesmo ponto/heading inicial, mesma transmissão calibrada, piso, quantidade de peças e bateria em condições próximas. Registrar tempo por etapa, desvio real, acertos e falhas de todas as execuções — não apenas das boas.

**Ficha padronizada:** [MODELO_REGISTRO_ENSAIO_AUTONOMO.md](MODELO_REGISTRO_ENSAIO_AUTONOMO.md).

## 12. Como manter, testar e continuar o código

### 12.1 Códigos a estudar primeiro

- **Autonomo35.java:** começo recomendado para entender controle manual com testes curtos e mecanismo isolado.
- **Autonomo51.java:** extensão do controle para inicialização de câmera, seleção de alvos e fallback; estudar depois que o IMU e tração estiverem compreendidos.
- **autonomo.java (3.1):** fonte histórica de referência para comparar mudanças; **não sobrescrever**.
- **Arquivos antigos 2/3:** evolução e decisões anteriores; não copiar para o mesmo pacote com o mesmo nome de classe.
- **Índice geral:** [INDICE_COMPLETO_CODIGOS_AUTONOMO.md](INDICE_COMPLETO_CODIGOS_AUTONOMO.md).

### 12.2 Reproduzir scripts de análise localmente

Executar em um checkout que contenha as duas linhas:

~~~bash
node tests/autonomo32_logic_tests.mjs
node tests/autonomo32_travel_model.mjs
node tests/autonomo_sem_camera_3x.mjs
node tests/auditoria_autonomo_3x.mjs
node tests/autonomo41_logic_tests.mjs
node tests/autonomo50_synthetic_tests.mjs
node tests/autonomo51_cell_tests.mjs
node tests/autonomo51_pid_model.mjs
node tests/auditoria_documentacao_geral.mjs
~~~

Compilar o código com o projeto FTC SDK completo e ambiente Android configurado:

~~~powershell
.\gradlew.bat :TeamCode:assembleDebug
~~~

No Linux/CI, usar ./gradlew :TeamCode:assembleDebug.

**Importante:** a raiz do repositório de históricos não é, sozinha, o projeto completo Gradle. Use a pasta TeamCode no projeto FTC oficial correto. Uma compilação aprovada não instala o app automaticamente no Control Hub, nem verifica nomes/configurações físicas de hardware.

### 12.3 Ao detectar um problema

Registrar **versão, SHA, modo, sintomas, entradas, telemetria, expectativa e resultado**. Formar hipóteses distintas para software/hardware. Fazer alteração isolada, nova versão, testes matemáticos/CI novamente e ensaio físico comparável.

Nunca ajustar simultaneamente redução, diâmetro, fator de calibração, direção de motor e ganhos PID. Isso tornaria impossível atribuir melhora a uma causa específica.

## 13. Limitações identificadas e prioridades para versões futuras

**P0 — Bloqueios antes de competir**
1. Conferir sinal físico de MD/ME, yaw e encoders.
2. Aferir transmissão real, diâmetro efetivo e trajeto de 2.000 mm.
3. Confirmar geometria legal e eficaz da rota em cada ponto de partida.
4. Validar confiabilidade de MI/MS, contagem de peças, velocidade sob carga e STOP.
5. Para 5.1, confirmar detecções reais da C270, latência, sentido do bearing e offset câmera-lançador.
6. Identificar o commit do Java e a versão do FTC SDK instalados no robô antes de atribuir resultado de teste a uma versão.

**P1 — Melhorias de software mensuráveis após a bancada**
1. Aplicar à seleção de aliança/lado da 5.1 **confirmação de A por nova borda** como já ocorre na 3.5.
2. Registrar telemetry detalhada por etapa em arquivo/log com timestamp para comparação estatística, em vez de depender apenas da tela.
3. Separar escolha de posição inicial e cálculo de estratégia por RED/BLUE e SCORING/AUDIENCE, com espelhamento/rota **geometricamente validado**, sem inferir mapa automaticamente.
4. Se houver encoder confiável em MS, adotar realimentação de velocidade e critério de prontidão do lançador, em vez de apenas potência 0,70 e aquecimento de 0,80 s.
5. Introduzir confirmação física de peça quando sensores adequados existirem; não inventar sensor que o robô não possui.
6. Fixar revisão exata do FTC SDK e Android Gradle Plugin no CI para builds repetíveis.
7. Repetir testes de falha com câmera real: desligamento USB, perda de alvo, frame lento, CELL virada.

**P2 — Evoluções estratégicas futuras**
1. Navegação por pose e estimativa de heading/distância baseada em calibração física, sensores reais e geometria oficial, não apenas encoder + IMU.
2. Correção de mira compensando deslocamento óptico do eixo de MS e distância física real.
3. Adição de autonomia de escolha de objetivos somente após validação confiável dos subsistemas, evitando complexidade prematura.
4. Testes de regressão usando logs capturados do robô (*replay*), que aproximam os modelos matemáticos das respostas físicas medidas.

**Não gerar versões 3.6 ou 5.2 apenas por estética**. Cada versão deve conter mudança objetiva, teste, medição/hipótese e histórico.

## 14. Matriz final de evidências, sem confundir compilação com ensaio físico

| Grupo | Preservação de código | Revisão e modelos | Compilação FTC | Validação física |
| --- | --- | --- | --- | --- |
| 2 e 3 | Arquivo histórico preservado | Histórico consultado | Não recompilado como código instalável atual | Sem relatório físico vinculado |
| 3.1–3.5 sem câmera | Fontes/backup preservados | Scripts e modelos sintéticos aprovados no CI | **APROVADA** (#38319766443 e #38321732887) | **PENDENTE** |
| 4.0–5.1 com câmera | Fontes/backup preservados | Scripts e modelos sintéticos aprovados no CI (com ressalvas sobre antigos relatos da 4.0) | **APROVADA** (#38321732887) | **PENDENTE** |
| Documentação geral | Históricos/links, 12 versões | **87 verificações de hashes/links aprovadas** | Não aplicável | Modelos prontos, ainda não preenchidos com ensaios |

**Conclusão técnica:** o projeto está documentado e suas classes compilam no FTC SDK oficial utilizado pelo CI. A parte ainda não demonstrada é o desempenho real do Mequinha: tração, IMU/encoders, C270, mecanismos, tempo, precisão e pontuação. Esse limite é uma questão de honestidade de evidência, não uma falha escondida.

---

## 15. Índice e referências verificáveis

### Códigos completos, por família

**Sem câmera:** [2 histórico](historico/Autonomo_2.txt) · [3 histórico](historico/Autonomo_3.txt) · [3.1](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/autonomo.java) · [3.2](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo32.java) · [3.3](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo33.java) · [3.4](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo34.java) · [3.5](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo35.java).

**Com câmera:** [4.0](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo4.java) · [4.1](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo41.java) · [4.2](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo42.java) · [5.0](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo50.java) · [5.1](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo51.java).

### Cópias históricas integrais (SHA preservado)

Para reconstruir toda a programação sem depender da classe Java ativa, a pasta de histórico contém também o **conteúdo integral** de cada versão, exatamente como estava ao ser registrada:

| Versões sem câmera | Versões com câmera |
| --- | --- |
| [Autônomo 2](historico/Autonomo_2.txt) | [Autônomo 4.0](historico/Autonomo_4_0.txt) |
| [Autônomo 3](historico/Autonomo_3.txt) | [Autônomo 4.1](historico/Autonomo_4_1.txt) |
| [Autônomo 3.1](historico/Autonomo_3_1.txt) | [Autônomo 4.2](historico/Autonomo_4_2.txt) |
| [Autônomo 3.2](historico/Autonomo_3_2.txt) | [Autônomo 5.0](historico/Autonomo_5_0.txt) |
| [Autônomo 3.3](historico/Autonomo_3_3.txt) | [Autônomo 5.1](historico/Autonomo_5_1.txt) |
| [Autônomo 3.4](historico/Autonomo_3_4.txt) | — |
| [Autônomo 3.5](historico/Autonomo_3_5.txt) | — |

A equivalência entre cada backup 3.1–5.1 e sua classe Java atual é verificada pelo [script de auditoria](tests/auditoria_documentacao_geral.mjs).

**Diagnósticos:** [Sinais e IMU](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/DiagnosticoSinais.java) · [C270 e AprilTag](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/DiagnosticoAprilTag.java).

**Documentos de validação:** [3.2](historico/VALIDACAO_Autonomo_3_2.md) · [3.3](historico/VALIDACAO_Autonomo_3_3.md) · [3.4](historico/VALIDACAO_Autonomo_3_4.md) · [3.5](historico/VALIDACAO_Autonomo_3_5.md) · [4.0](historico/VALIDACAO_Autonomo_4_0.md) · [4.1](historico/VALIDACAO_Autonomo_4_1.md) · [4.2–5.1](historico/VALIDACAO_VERSOES_4_2_A_5_1.md) · [Build 3.x](historico/RELATORIO_CI_SEM_CAMERA_3X.md).

**Normas internas:** [AGENTS.md](AGENTS.md) · [Skill FTC Engineer](.agents/skills/ftc-engineer/SKILL.md) · [Hardware](.agents/skills/ftc-engineer/references/hardware.md) · [Validação](.agents/skills/ftc-engineer/references/validation.md).

**Fontes externas:** [FTC SDK oficial](https://github.com/FIRST-Tech-Challenge/FtcRobotController) · [FTC documentação AprilTag Clusters](https://ftc-docs.firstinspires.org/en/latest/tech_tips/tech-tips/tech-tip-apriltag-clusters/tech-tip-apriltag-clusters.html) · [REV UltraPlanetary](https://docs.revrobotics.com/rev-crossover-products/ultraplanetary/cartridge-details).

### Convenção de alteração e reversão

1. Medir e registrar a necessidade (sem alterar código histórico).
2. Criar nova classe/versão na branch apropriada, com anotação OpMode distinta.
3. Atualizar testes matemáticos e documentação, incluindo resultado real da execução.
4. Executar build FTC; identificar commit exato.
5. Testar fisicamente em progressão segura e preencher a ficha, anotando inclusive falhas.
6. Incorporar a branch à main **somente após revisão e autorização específica**.
7. Para reverter, selecionar a OpMode anterior e, se necessário, restaurar o commit/teste correspondente. Não apagar histórico.

**Status do dossiê:** edição consolidada de engenharia; todos os códigos íntegros mantidos nos arquivos de origem e referenciados aqui sem duplicar mais de dez mil linhas Java dentro de um relatório de leitura. O índice separado reúne os arquivos com hashes de conteúdo, permitindo auditar todo o projeto e estudar qualquer versão integralmente.