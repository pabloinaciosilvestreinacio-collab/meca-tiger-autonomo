# Validação do software FTC — níveis de evidência

## Classificação obrigatória
- **Revisão estática:** inspeção de código; encontra defeitos potenciais, mas não prova compilação.
- **Compilação FTC real:** `assembleDebug` em projeto Android Studio/FTC SDK completo, com versões e ambiente registrados.
- **Testes automatizados:** assertions com resultado reproduzível, mocks/stubs ou funções puras isoladas; explicitar se não incluem SDK ou hardware.
- **Simulação:** modelo da cinemática/sensores e cenários artificiais; indicar aproximações e limites do modelo.
- **Teste físico:** ensaio documentado no robô, com condições, logs, resultado e responsável presente.

Nunca trocar um nível de evidência por outro no relatório.

## Procedimento por risco
1. **Reprodução:** definir entrada, estado inicial e resultado esperado e observado.
2. **Revisão:** verificar API, sinais, unidades, STOP, timeout, valores NaN, saturação, anti-windup e encerramento seguro.
3. **Compilação:** somente após obter um projeto FTC SDK completo. No Windows, conferir as tarefas disponíveis via `gradlew.bat tasks` e executar `gradlew.bat :TeamCode:assembleDebug` se a tarefa existir. Não alegar execução se o ambiente não permitir.
4. **Teste de lógica:** normalização angular perto de -180°/+180°, erro de yaw de ambos os sinais, conversão ticks/mm, dt perto de zero, PID em saturação, encoder negativo/parado, STOP durante as tarefas, timeout e leituras inválidas do IMU.
5. **Cenários simulados:** atraso de sensor, velocidade diferente entre rodas, oscilações do yaw, perda de progresso, parada antecipada e atraso de lançamento.
6. **Ensaio físico seguro:** inspeção mecânica e bateria; primeiro movimento curto de baixa potência com rodas suspensas, depois trajetos pequenos e somente por último sequência completa, com área livre e intervenção rápida disponível.

## Critérios de aceite sugeridos para a V3.1
- Nenhum atuador permanece energizado após STOP ou falha recuperável.
- Direções de giro e sinais de encoders batem com a convenção do código.
- O robô não executa movimento indefinidamente caso IMU/encoder não progrida.
- Os resultados medidos de distância/ângulo e o tempo total são registrados; tolerâncias devem ser acordadas antes do teste, não inventadas após o resultado.
- Um build bem-sucedido não significa PID ajustado.

## Para a V4 com câmera
- Antes da integração, obter frames/detecções consistentes da C270 em um OpMode de diagnóstico.
- Confirmar ID de tag, metadados e unidades de pose da temporada; verificar convenção de eixos câmera/robô/campo.
- Definir comportamento sem detecção e com leituras inconsistentes (timeout/abort/fallback conservador).
- Testar visão isolada, visão+movimento em baixa potência, depois sequência integrada.

## Modelo de relatório
```text
Objetivo:
Commit/arquivos:
Ambiente/SDK:
Revisão estática: [realizada | não realizada]
Compilação real: [aprovada | falhou | não executada] + evidência
Testes automatizados: [aprovados | falharam | não executados] + evidência
Simulação: [executada | não executada] + hipóteses
Teste físico: [executado | não executado] + registros
Riscos restantes:
Próxima ação segura:
```
