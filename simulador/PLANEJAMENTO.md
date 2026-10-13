# Idealização e próximos marcos

## Problema e público

Equipes que precisam enxergar trajetos, sinais dos motores, encoders e controle de rumo antes de levar o software ao robô. A ferramenta nasce com duas vistas e Java colado, sem exigir Unity, motor 3D ou uma instalação Android completa.

Não afirmar inexistência de simuladores FTC: já há iniciativas de simulação e visualização; este produto precisa justificar seu valor por facilidade, idioma, integração com código real e documentação das limitações. Pesquisa comparativa de concorrentes é uma tarefa futura, não executada nesta entrega.

## Arquitetura 0.1

Interface HTML/Canvas e Gamepad API → servidor Python local → processo JVM com camada FTC parcial → motores/sensores do modelo Python → estado e telemetria da interface. A simulação fica no Python; o navegador não calcula uma segunda pose concorrente. A geometria e balística de visualização usam fórmulas puras testadas.

| Marco | Entrega | Critério de conclusão |
| --- | --- | --- |
| 0.1 — protótipo atual | Duas vistas, Java LinearOpMode, encoder/IMU, gamepads, testes | Exemplos executam; STOP encerra JVM; equações passam testes analíticos |
| 0.2 — compatibilidade | Importar vários `.java`, ampliar APIs pelos códigos usados por equipes | Matriz de códigos reais com compilações e resultados publicados |
| 0.3 — cenários e falhas | Obstáculos, colisões, velocidade desigual, falha de encoder/IMU, latência e logs | Cenários reproduzíveis e comparação com soluções analíticas |
| 0.4 — mecanismos | Launcher ligado a MS/MI por modelo configurável de RPM e alimentação | Calibração experimental; distinguir comando de peça realmente lançada |
| 0.5 — distribuição | Instalador/launcher simples, presets de equipes, documentação e demonstração | Teste de uso com pelo menos outra equipe, Windows e gamepads reais |
| Visão — depois | AprilTags sintéticas e latência configurável | Convenções/extrínsecos explícitos e falhas sem detecção testadas |

## Gargalos que precisam ser resolvidos

1. **Compatibilidade Java:** colar código inteiro só funciona se todas as APIs/classes usadas estiverem disponíveis. Ampliar pelas necessidades reais, com erros explícitos.
2. **Fidelidade:** simular bem um modelo ideal não prova que o robô real o segue. Medir bitola efetiva, ticks/m, velocidade e polaridade antes de oferecer previsões precisas.
3. **Distribuição:** exigir Python e JDK ajuda a construir rápido, mas é uma barreira para outras equipes. O produto final precisa de instalação guiada ou empacotamento.
4. **Execução pública:** Java enviado por visitantes precisa de isolamento real, limites de CPU/memória/tempo, filesystem descartável e rede bloqueada. Não hospedar o runner local como serviço público.
5. **Confiabilidade:** futuro modo determinístico deve separar tempo de simulação do agendamento da JVM, permitindo repetir uma execução com o mesmo gamepad/log.

## Próximo ensaio com Pablo

- Abrir no Windows; validar JDK/Python, Chrome/Edge e resolução de tela.
- Rodar os dois exemplos; testar controle USB conectado, desconectado e perda de foco.
- Colar o autônomo sem câmera atual; anotar APIs ausentes e a hipótese de sinais selecionada.
- Comparar o ponto do lançador com o Desmos em 0°, 90° e 180° e mover seus offsets.
- Comparar uma distância/virada curta com medições supervisionadas do robô. Não alterar ganhos do robô apenas porque a simulação parece melhor.

## Registro e compartilhamento

O projeto fica inicialmente na pasta `simulador/`, em branch e PR próprios do repositório existente. Código do robô e históricos não são reescritos. Um repositório público independente, licença e identidade definitiva exigem decisões posteriores; nesta entrega não se altera visibilidade ou licença do repositório da equipe.
