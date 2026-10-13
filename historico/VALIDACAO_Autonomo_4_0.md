# Validação do Autônomo 4.0

Data: 2026-10-12

## Estado da versão
- Arquivo ativo: `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo4.java`
- SHA do código revisado: `d4a984f7d0f4b680ec1f16ab6d8426b325e2715b`
- O Autônomo 3.1 não foi alterado.
- O 4.0 é a linha com câmera/AprilTag Cluster.

## Dados oficiais verificados
- FTC SDK: 12.0 para a temporada 2026-2027.
- BIOBUZZ possui quatro AprilTag Clusters e nenhuma AprilTag individual de jogo.
- `AprilTagProcessor.getDetections()` usa `AprilTagDetection` e, no BIOBUZZ, os alvos válidos são `AprilTagClusterDetection`.
- O `ftcPose.roll` de um cluster com valor absoluto menor que 90 graus indica orientação correta para uso do alvo.
- A biblioteca da temporada atual é carregada por padrão pelo `AprilTagProcessor`.
- Logitech C270: resolução 640x480 possui calibração oficial embutida no SDK.
- HD Hex Motor: 28 counts/rev no eixo do motor.
- UltraPlanetary: cartucho nominal 4:1 = 3,61:1 real; nominal 5:1 = 5,23:1 real.
- Redução teórica combinada usada no 4.0: 18,8803:1.
- Período AUTO BIOBUZZ: 30 segundos.

## Alterações feitas durante esta revisão
1. Substituído o fator nominal 20:1 pelas relações reais 3,61 × 5,23.
2. Adicionado `FATOR_CALIBRACAO_DISTANCIA` para calibração física sem alterar a relação mecânica.
3. C270 fixada em 640x480 para usar a calibração oficial do SDK.
4. Adicionado `OFFSET_BEARING_CAMERA_GRAUS` para corrigir montagem física da câmera.
5. Removido suporte a `AprilTagSingleDetection` na estratégia BIOBUZZ.
6. Seleção de cluster definida por aliança (RED/BLUE) e lado (SCORING/AUDIENCE).
7. Rejeição de CELL invertida por `|roll| >= 90°`.
8. Falhas de câmera operam em modo fail-soft e retornam ao controle por IMU.
9. Falha de IMU e exceções inesperadas recebem diagnóstico e parada segura.
10. `normalizarAngulo()` deixou de usar loops e passou a usar módulo com validação de valor finito.
11. Corrigida a condição final do deslocamento: ao atingir a distância o robô para a translação e, se necessário, corrige o rumo girando no próprio eixo.
12. Potência mínima de giro e alinhamento visual elevada para 0,10 após teste de sensibilidade a atrito estático.
13. Adicionada validação de configuração antes do início da rotina.

## Simulações lógicas executadas

### Giro por IMU/PID
Modelo:
- ganho angular aleatório: 140 a 300 graus/s em potência plena;
- constante de tempo: 0,07 a 0,30 s;
- atrito estático equivalente: 0,03 a 0,09 de potência;
- ruído angular adicionado;
- loop equivalente a 10 ms.

Resultados:
- Giro -50°: 10.000 / 10.000 convergiram dentro de 7 s.
- Mediana -50°: ~1,91 s.
- P95 -50°: ~3,67 s.
- Giro +100°: 10.000 / 10.000 convergiram dentro de 7 s.
- Mediana +100°: ~2,44 s.
- P95 +100°: ~4,21 s.

### Deslocamento de 2000 mm
Modelo:
- velocidade máxima equivalente variada entre 1200 e 1800 mm/s;
- assimetria de motor: ±8%;
- escorregamento independente por lado: 0 a 4%;
- ruído no rumo;
- correção por IMU + diferença de encoder.

Problema encontrado antes da correção:
- 1.232 / 5.000 cenários ultrapassaram o alvo porque o código continuava avançando para tentar satisfazer simultaneamente distância, diferença de encoder e rumo.

Depois da correção:
- 10.000 / 10.000 cenários concluíram sem ultrapassagem lógica.
- Mediana: ~4,53 s.
- P95: ~5,51 s.
- Pior tempo observado: ~5,99 s.

### Alinhamento por visão
Modelo:
- erro inicial de bearing: -15° a +15°;
- ganho mecânico, inércia e atrito variados;
- 2% de perda aleatória de frames;
- timeout visual de 2,5 s.

Resultados:
- 9.201 / 10.000 alinharam dentro do timeout.
- 799 / 10.000 chegaram ao timeout de visão.
- Esses 799 casos não representam falha total do AUTO: o código usa fallback de IMU e preserva o lançamento.
- Mediana dos alinhamentos bem-sucedidos: ~1,10 s.
- P95: ~2,14 s.

### Seleção de cluster
- 28 / 28 combinações lógicas testadas selecionaram somente o cluster definido por aliança/lado.
- Cluster da aliança oposta é ignorado mesmo quando possui menor |bearing|.
- Cluster invertido é rejeitado pelo filtro de roll.

## Auditoria estrutural
- Nenhum uso de `RUN_TO_POSITION`.
- Controle de movimento continua manual por loop/PID.
- Todos os loops operacionais possuem `opModeIsActive()`, limite global e/ou timeout.
- Chaves e parênteses do arquivo balanceados.
- Câmera é fechada no `finally`.
- Motores são zerados no `finally`.
- Leitura inválida de IMU retorna NaN e é rejeitada nas etapas de movimento.
- Leitura/estado inválido de visão não é permitido comandar movimento.

## Valores ainda dependentes de teste físico
Estes valores não devem ser considerados “confirmados” por simulação:
- `DIAMETRO_RODA_MM = 90.0`: o diâmetro efetivo muda com compressão e desgaste.
- `FATOR_CALIBRACAO_DISTANCIA = 1.0`: deve ser ajustado após medir percurso real.
- `MIN_POTENCIA_VIRADA = 0.10`: confirmar atrito real.
- `MIN_POTENCIA_ALINHAMENTO = 0.10`: confirmar atrito real.
- `OFFSET_BEARING_CAMERA_GRAUS = 0.0`: medir depois de instalar a C270.
- Orientação física do Control Hub: confirmar LEFT/UP no robô.
- Sinal real de -50° e +100°: confirmar em teste sobre o piso de competição.
- Exposição/ganho da C270: otimizar no ambiente real.
- `POTENCIA_LANCAMENTO = 0.70` e tempos de alimentação: precisam de RPM/ensaio físico.
- O código pressupõe que os cartuchos UltraPlanetary 4:1 e 5:1 estejam ambos montados no drivetrain.

## Conclusão
O 4.0 está coerente com o FTC SDK 12.0 e com a API BIOBUZZ atual, e as falhas lógicas encontradas nesta revisão foram corrigidas. As simulações reduzem riscos de sinal, timeout, overshoot e seleção incorreta de alvo, mas não substituem validação física do drivetrain, IMU, C270 e lançador.


---

## Atualização de evidência de compilação — 12/10/2026

**FTC SDK integrado: BUILD APROVADO.** O [GitHub Actions run #38321732887](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/actions/runs/38321732887) compilou conjuntamente **todas as classes Java de autônomo com e sem câmera**, incluindo esta versão 4.0, com `TeamCode:compileDebugJavaWithJavac`, `TeamCode:assembleDebug` e a mensagem **BUILD SUCCESSFUL in 2m 42s**.

O job de testes matemáticos do mesmo run terminou com **SUCCESS**. Simulações e contagens originalmente descritas em seções anteriores continuam sendo um registro histórico; não foram todas reconstruídas por script independente neste novo CI.

**Limite:** compilação do código no FTC SDK oficial obtido no CI NÃO significa montagem/calibração da C270, motores ou lançador validada fisicamente. Antes de instalar, conferir o projeto FTC SDK local, hardwareMap, sinais e campo. Consulta consolidada: [RELATORIO_GERAL_VALIDACAO_SOFTWARE.md](../RELATORIO_GERAL_VALIDACAO_SOFTWARE.md).
