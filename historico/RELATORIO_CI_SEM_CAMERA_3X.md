# Relatório de integração real — Autônomos FTC SEM câmera 3.1 a 3.5

**Data do build:** 12 de outubro de 2026 (logs em UTC).
**Repositório:** `pabloinaciosilvestreinacio-collab/meca-tiger-autonomo`.
**Pull Request:** [#3](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/pull/3), draft; sem merge na main.
**GitHub Actions run:** [38319766443](https://github.com/pabloinaciosilvestreinacio-collab/meca-tiger-autonomo/actions/runs/38319766443).
**Workflow:** `.github/workflows/ftc-sem-camera.yml`.

## Evidências observadas diretamente no GitHub Actions
| Job | Estado | Evidência |
| --- | --- | --- |
| `Validacao sintetica sem camera` | **SUCCESS** | `node tests/autonomo_sem_camera_3x.mjs`: **21 assertions**, **5.000 cenários**; `node tests/auditoria_autonomo_3x.mjs`: **58 verificações**; grep de dependências de câmera sem achados |
| `FTC SDK compile :TeamCode:assembleDebug` | **SUCCESS** | `> Task :TeamCode:compileDebugJavaWithJavac`, `> Task :TeamCode:assembleDebug`, **`BUILD SUCCESSFUL in 2m 16s`** |

O runner integrou os códigos `autonomo.java` (3.1), `Autonomo32.java`, `Autonomo33.java`, `Autonomo34.java` e `Autonomo35.java` ao `TeamCode` do repositório oficial `FIRST-Tech-Challenge/FtcRobotController` obtido pelo checkout público durante o job, com Java Temurin 17 e Android SDK do GitHub runner.

### Proveniência do código realmente compilado
Commit de código da branch incluído no teste: `a537fdb5c9316ae7712274d1594557fdfc4efe7f` (checkout de merge do PR `d71ea04...`).

| Fonte Java | SHA do blob efetivamente compilado |
| --- | --- |
| `autonomo.java` | `cccc44f9ddfd8b90b06fb826d35e21b19bdc4fcb` |
| `Autonomo32.java` | `ac3d183421553b9adeba620e6299cdb1504baeb2` |
| `Autonomo33.java` | `7103e0b86180def8967814960995637ea136e3c8` |
| `Autonomo34.java` | `62228bf535c382505328a386d70cda85a67b8b2d` |
| `Autonomo35.java` | `37cc2ac051f014487d45702958e46f0bc4e3455b` |

Em verificação independente após o build, os cinco blobs Java acima correspondiam **exatamente** aos presentes na branch de desenvolvimento. Commits posteriores afetaram **somente documentação**.

## Sequência da depuração da infraestrutura CI
1. Primeiro erro: `android-actions/setup-android@v3` tentou instalar pacote `tools` descontinuado, antes de compilar Java.
2. Segundo erro: `sdkmanager` existia em `$ANDROID_HOME/cmdline-tools/16.0/bin`, mas estava fora do `PATH`.
3. Workflow corrigido para localizar `sdkmanager` no Android SDK preinstalado, adicionar ao `GITHUB_PATH`, instalar componentes Android necessários e copiar as classes para o projeto FTC.
4. **Terceira execução aprovada, com compilação Java e Gradle reais**, conforme o run #38319766443.

## O que isso valida
- As cinco classes **compilaram juntas** no projeto oficial FTC SDK público obtido no dia.
- Imports, anotações OpMode, sintaxe Java e uso das APIs disponíveis resolveram sem erro de compilação.
- A linha 3.x não usa webcam/AprilTag/VisionPortal.
- Os scripts sintéticos e o linter de arquivos/backups passaram.

## O que NÃO valida
- A compilação foi feita no **SDK oficial clonado no CI**, não no computador da escola nem no Control Hub da Meca Tiger. O clone usou a versão pública corrente naquele instante; o workflow ainda não está travado a um SHA de upstream.
- Teste físico com motores, IMU, encoders, rodas, bateria, intake ou lançador: **NÃO EXECUTADO**.
- `REDUCAO=20`, diâmetro efetivo da roda, montagem `LEFT/UP`, sinais de yaw/encoder, ganhos PID, limiar de 220 ticks/35°, potência de atrito e 3 lançamentos: **DEPENDENTES DE MEDIÇÃO**.
- Os 5.000 cenários não são a taxa de sucesso em competição e não verificam a sequência real de alimentação de peças.
- A trajetória fixa não corrige localização absoluta ou obstáculo sem sensores adicionais.

## Próxima fase física
Primeiro `DiagnosticoSinais` para yaw e encoder sem motores energizados; depois OpMode `Autonomo 3.5 - Bancada`: B + A para giro curto, X + A para 350 mm, D-pad CIMA + A para lançador em ambiente isolado, Y + A para estratégia completa. Medir e registrar condições/resultado com `GUIA_AUTONOMO_SEM_CAMERA.md`.

## Estado de release
**3.5 = candidata para ensaio físico supervisionado**, com build real aprovado no GitHub Actions. **Não é versão de competição homologada.** PR #3 permanece draft e `main` não foi alterada por esse trabalho.
