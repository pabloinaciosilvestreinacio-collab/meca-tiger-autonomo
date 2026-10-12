# Autônomo 3.5 — Bancada e telemetria de etapas (SEM câmera)
Data: 2026-10-12
Código: `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/Autonomo35.java`
Cópia integral: `historico/Autonomo_3_5.txt`
Origem: `Autonomo34.java`. Arquivos 3.1, 3.2, 3.3 e 3.4 preservados.

## Mudança funcional em relação à 3.4
- Novo modo `TESTE_LANCADOR` selecionável com **D-pad para cima** durante o INIT, além dos modos pré-existentes: B = giro +20/-20, X = percorrer 350 mm, Y = rotina completa.
- O novo modo comanda **somente MI e MS** por meio de `lancarPecasPorTempo(3)`, sem solicitar movimento de MD ou ME. Antes de executá-lo, o código chama `pararTracao()`.
- **Risco físico importante:** `TESTE_LANCADOR` poderá lançar peças. Não usar perto de pessoas e nunca em ambiente sem supervisão.
- O modo padrão continua `GIRO_CURTO`. Um modo só executa após **A confirmar** e depois **START**. Mudar de modo remove a confirmação. O toque de A deve acontecer **depois** da mudança de modo; manter A pressionado durante a troca não confirma.
- A função `sequencia` agora mede o tempo de cada tarefa por `ElapsedTime` e mostra `Tempo ultima etapa` e `Resultado ultima etapa` na telemetria.
- Falhas de `RuntimeException` registram a etapa que estava ativa, em vez de substituí-la pelo rótulo genérico de falha.
- Limites, PID, watchdog, parada da tração, reserva de tempo, coleta e lançamento da 3.3 foram preservados integralmente.

## Ensaio do lançador
1. Preparar zona segura de lançamento, sem pessoas na frente e com retenção para peças.
2. Com Control Hub e Driver Station conectados, selecionar `Autonomo 3.5 - Bancada` e INIT.
3. Escolher **D-pad para cima**, conferir a mensagem `TESTE_LANCADOR` e os riscos, apertar **A** e somente então START.
4. Os motores MD/ME continuam sem comandos de movimento. MS aquece por 0,80 s em potência 0,70; MI alimenta por 0,45 s por ciclo, em três ciclos, com 0,15 s de intervalo. Ambos param no `finally`.
5. Medir número de peças **efetivamente** lançadas, distância, estabilidade do mecanismo, travamentos e estado após STOP. Registro `Ciclos alimentacao comandados` **não é** uma contagem de peças confirmadas.
6. Se a alimentação não funciona, investigar geometria de entrada, direção elétrica, velocidade do lançador e tempo de ação. Não aumentar potência por suposição.

## Evidência de software
- `tests/auditoria_autonomo_3x.mjs`: verifica que 3.5 não importa câmera, usa classe/anotação corretas, tem backup idêntico e apresenta modo de lançamento com tração parada, diagnóstico por etapa e limites.
- `tests/autonomo_sem_camera_3x.mjs`: testes matemáticos e simulação de PID/movimento; não mede MI/MS real.
- Workflow `.github/workflows/ftc-sem-camera.yml`: instalado para compilar 3.1–3.5 no FTC SDK oficial. **Build real ainda depende de resultado dos jobs.**
- **Nenhum teste físico/lançamento real executado.**

## Limites permanentes
O robô não possui no código feedback de velocidade de MS nem contagem de peças/saída. Portanto, sem sensores e sem calibração física, a 3.5 não garante velocidade constante do lançador ou três lançamentos efetivos. `REDUCAO=20:1`, orientação IMU, polaridade dos motores, limites e PID continuam parâmetros iniciais não aferidos.

## Reversão
A 3.4 continua no próprio arquivo `Autonomo34.java`, e a 3.3 completa em `Autonomo33.java`. Basta selecionar outra OpMode. Não sobrescrever versões antigas ou `main` sem aprovação específica.


### Atualização de validação CI — 12/10/2026
**Compilação FTC real aprovada:** GitHub Actions [run #38319766443](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/actions/runs/38319766443), `compileDebugJavaWithJavac` e `assembleDebug` com `BUILD SUCCESSFUL in 2m 16s`. Teste Node: 21 assertions e 5.000 cenários; auditoria: 58 verificações. **Não representa teste físico**. Ver `historico/RELATORIO_CI_SEM_CAMERA_3X.md`.
