# Meca Tiger FTC #32578 — instruções de engenharia para agentes

## Escopo e fonte da verdade
- Este repositório conserva o autônomo FTC e seu histórico. O arquivo de execução atual é `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Autonomo/autonomo.java`.
- A referência atual é o **Autônomo 3.1**. `historico/Autonomo_2.txt`, `Autonomo_3.txt` e `Autonomo_3_1.txt` são versões preservadas, não arquivos para refatorar ou sincronizar automaticamente.
- Consulte o código do repositório antes de assumir detalhes presentes em conversas, memória ou documentos. Em conflito, sinalize a divergência e peça confirmação quando ela puder afetar o robô.
- Quando uma tarefa envolver FTC, Java do robô, motores, sensores, PID, navegação ou AprilTags, use a Skill `$ftc-engineer`, se disponível.

## Limites de autorização
- Leitura, inspeção, comparação e propostas são permitidas quando solicitadas.
- Não edite arquivos, gere commits, crie branches, abra PRs, faça merges ou altere configurações remotas enquanto a tarefa for apenas de análise/revisão.
- Depois de autorização explícita para implementação, faça somente as alterações dentro do escopo autorizado, preferencialmente em branch separada; não presuma autorização para merge ou publicação.
- Requeira aprovação específica antes de modificar `main`, substituir versões históricas, fazer force-push, apagar arquivos, alterar hardware físico ou enviar código ao robô.
- Não execute motores, lançador ou intake reais sem autorização e procedimentos físicos de segurança.

## Arquitetura e compatibilidade
- Preserve a lógica e a estrutura existentes ao corrigir um defeito localizado. Evite reorganizações amplas, novas dependências e novas classes sem necessidade comprovada.
- O robô documentado usa tração diferencial de dois motores (`MD`/`ME`), não mecanum. `MI` e `MS` são mecanismos; `imu` é o sensor inercial.
- Respeite o pacote e o nome atuais (`org.firstinspires.ftc.teamcode.Autonomo`, classe `autonomo`) salvo solicitação explícita.
- O código atual usa `RUN_WITHOUT_ENCODER` com controle calculado no Java; não substitua automaticamente por `RUN_TO_POSITION`.
- Trate redução mecânica, diâmetro da roda, orientação do Control Hub, direção dos motores e polaridade dos encoders como hipóteses do código até confirmação física.
- Para recursos novos (por exemplo, câmera C270 e AprilTags), consulte a documentação da versão real do FTC SDK e mantenha o autônomo 3.1 intacto até aprovar a evolução.

## Critérios de engenharia
- Antes de mudar comandos de motores ou correção angular, explicite convenções de sinal, sistema de coordenadas e comportamento esperado.
- Verifique parada no STOP, timeout, valores não finitos, saturação, anti-windup, erros de leitura e desligamento de motores em caminhos de falha.
- Em movimento, separe problemas de software de atrito, engrenagens, montagem, alimentação, sentido elétrico e orientação do IMU.
- Não altere o comportamento mecânico para corrigir um problema de percepção sem evidência.

## Validação e comunicação
- Informe separadamente: revisão estática, compilação real, testes automatizados, simulação e testes físicos. Nunca marque um teste como aprovado sem executá-lo e apresentar o resultado.
- Este repositório ainda não contém o projeto FTC SDK/Gradle completo; não alegue `assembleDebug` bem-sucedido sem integrar primeiro um projeto reproduzível.
- Execute apenas testes disponíveis e adequados ao escopo; se não puder executar, indique exatamente o bloqueio.
- Para cada mudança, informe: arquivos afetados, problema resolvido, hipótese, riscos, evidências de teste e procedimento de reversão.
- Prefira explicações objetivas em português e entregue código Java diretamente na conversa quando o usuário pedir código. Evite comentários excessivos no código final.
