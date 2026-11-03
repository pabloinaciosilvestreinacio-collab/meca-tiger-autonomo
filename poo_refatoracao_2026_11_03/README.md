# Mequinha — POO, etapa 01 (03/11/2026)

## O que existe nesta etapa

| Classe | Responsabilidade | Estabilidade |
|---|---|---|
| `ParametrosMequinha` | Medidas do robô e posições de servo usadas no Meca Sim | Valor de referência, precisa de calibração real |
| `PoseMequinha` | Pose imutável `(x, y, rumo)` e normalização de ângulos | Testada localmente |
| `HardwareMequinha` | Configuração ME/MD/MI/MS/SA/imu e parada de segurança | Compilada com stubs FTC e verificações unitárias |
| `OdometriaDiferencial` | Estimativa de deslocamento dos encoders e yaw externo da IMU | Testada localmente em cenários artificiais |
| `DiagnosticoPOOMequinhaV1` | OpMode não competitivo; somente telemetria de odometria relativa | Compilação com stubs; não testado em Meca Sim/FTC real |

**Sem câmera, AprilTags, TIP por imagem, rotas, lançamentos ou intake automático.** Este conjunto intencionalmente não substitui qualquer OpMode competitivo.

## Local no TeamCode

Copiar os 4 módulos da pasta `src/org/firstinspires/ftc/teamcode/meca/` para:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/meca/`

Copiar `DiagnosticoPOOMequinhaV1.java` para:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/`

Não copiar `testes` para o TeamCode; são stubs locais que imitam apenas as APIs necessárias para compilar e **não** substituem o FTC SDK.

## Por que ainda não integrar aos L1/L2 oficiais?

- Os autônomos L1-01/02/03 foram aprovados **no Meca Sim**, mas não devem ser alterados silenciosamente.
- L1-05 v17 e L2-01/02 precisam de testes integrais no Meca Sim e validação física.
- Refatorar shooter + controle de direção + navegação + tempo simultaneamente tornaria impossível isolar regressões.
- A pose L2 (1.574, 3.420, 180°) nos códigos atuais é uma hipótese de spawn e `yawOffset=esperado-yawBruto` **não valida** fisicamente a orientação: apenas alinha o referencial; não usar isso como prova de largada correta.

## Verificações feitas

Executado no contêiner: `javac --release 17` com stubs mínimos locais e `java TestesPOO`. Saída: `OK: 14 verificacoes POO...`.

Cenários: ticks por metro, deslocamento de 560 ticks a 0° e a 90°, cruzamento de yaw 179°/−179°, zero deslocamento numa rotação pura, direção invertida do MD e condições seguras dos quatro motores/servo.

**Isso não é:** build Gradle FTC SDK, câmera, Meca Sim completo, nem validação física.

## Próximas etapas POO (sem alterar as fontes atuais)

1. `ControleShooter`: extrair RPM/encoder/3 amostras/cadência e intertravamento MI/MS; parametrizar por **perfil de simulador** ou medição física real.
2. `NavegacaoDiferencial`: extrair controle de rumo, trajetória Bézier, proteção de parede e watchdog de encoders.
3. `ColetaGarden` e `ColetaFlower`: separar rotas específicas dos mecanismos comuns de intake.
4. `EstrategiaAutonoma`: classes L1/L2 passam a orquestrar componentes; as versões oficiais L1/L2 originais ficam congeladas para teste A/B.
5. Visão/TIP somente depois da etapa de teste de câmera autorizada.

**Testes de regressão obrigatórios:** mesma pose inicial, mesmo perfil Meca Sim, mesmo tempo máximo, mesma rota, quatro/ oito/ doze POLLEN efetivamente ejetados (não apenas pulsos), pose final e desligamento seguro.

## Auditoria completa dos oito arquivos e manual para manutenção (03/11/2026)

A lista operacional contém **7 estratégias numeradas, representadas por 8 fontes de referência**:
`L1-01`, `L1-02`, `L1-03`, `L1-04 v1`, `L1-04 v2`, `L1-05 v17`, `L2-01 v1`, `L2-02 v2`. O L1-04 foi anteriormente omitido de um resumo, mas **não está excluído**: v1 e v2 continuam **experimentais**, com erro de coordenada Flower corrigido somente na v2. O L2-03 foi removido, e o 5.3 agressivo é legado separado desta contagem.

Documentos produzidos:
- [**Catálogo auditado**](CATALOGO_AUDITADO_8_AUTONOMOS_2026-11-03.md): fontes exatas, 296 constantes e seus valores, todos os métodos, motores, sensores, coordenadas e riscos por versão.
- [**Manual prático de ajustes**](GUIA_PRATICO_AJUSTES_POO_MEQUINHA_2026-11-03.md): servo aberto/fechado/tempo, cadência, RPM, intake, rotas, checklist de teste e plano futuro de câmera/HIVE.
- [**AjustesAutonomoMequinha.java**](src/org/firstinspires/ftc/teamcode/meca/AjustesAutonomoMequinha.java): arquivo único e legível com parâmetros compartilhados **e perfis históricos explicitamente diferentes** (L1-03 vs L1-05, etc.).

**Importante:** o arquivo de ajustes é um **primeiro módulo, não ligado aos OpModes antigos**. Alterá-lo ainda **não** muda o comportamento das oito fontes. Quando forem convertidas, verificar a equivalência linha a linha e testar A/B com cópias, sem editar o original. Não ativar câmera nos autônomos do Meca Sim atualmente: `CameraHive.java` é somente plano futuro e só deve ser introduzida no SDK real depois de validação.

**Testes locais de código-fonte:** compilação Java 17 com interfaces simuladas FTC, 14 verificações POO existentes + 5 verificações de parâmetros novos = **19 verificações aprovadas**. Nenhum build Gradle/FTC SDK real nem execução de rotas no Meca Sim são implicados por esses resultados.
