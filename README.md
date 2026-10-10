# Grafana — pruebas automatizadas (JUnit 5 + Playwright Java)

Testing Automatizado con IA · Unidad 1 (JUnit 5 + Playwright), Unidad 2 (BDD con Cucumber) y Unidad 3 (TDD + BDD en CI/CD con JaCoCo y SonarCloud).

## Requisitos
- Docker Desktop (o Rancher Desktop) corriendo. **No necesitas Java ni Maven**: la imagen `mcr.microsoft.com/playwright/java` ya trae JDK, Maven y Chromium.

## Ejecutar
```bash
./run-tests.sh
```
1. Levanta Grafana OSS 11.3.0 en http://localhost:3000 (usuario `admin`, clave `UdemTesting2026`).
2. Espera a que `/api/health` responda.
3. Ejecuta `mvn test` dentro del contenedor.

La primera ejecución descarga imágenes y dependencias (~3–5 min). Las siguientes tardan segundos.

## Evidencia generada
| Ruta | Contenido |
|---|---|
| `target/surefire-reports/` | Resultados JUnit (XML y TXT) |
| `target/evidence/videos/` | Video `.webm` de cada caso de UI |
| `target/evidence/screenshots/` | Captura final de cada caso |
| `target/evidence/traces/` | Traza Playwright: arrástrala a https://trace.playwright.dev |

## Casos
| ID | Tipo | Caso |
|---|---|---|
| TC-01 | UI | Login válido → inicio + sesión válida en `/api/user` |
| TC-02.1/.2 | UI | Login inválido (usuario inexistente / clave errada) → "Login failed" |
| TC-03 | UI | Formulario vacío → validaciones obligatorias |
| TC-04 | API | `GET /api/health` → 200, database ok |
| TC-05 | API | `GET /api/user` sin credenciales → 401 |
| TC-06 | API | `GET /api/user` con Basic Auth → 200 |

## Grabar un caso con codegen (opcional, requiere Node)
```bash
docker compose up -d grafana
npx playwright@1.49.0 codegen --target java-junit http://localhost:3000/login
```

## Apagar
```bash
docker compose down
```

## Integración continua
`.github/workflows/pruebas.yml` ejecuta la misma suite en GitHub Actions en cada push y publica la evidencia (reportes JUnit, videos, capturas y trazas) como artefacto de la ejecución.

## Evidencia incluida en el repositorio
| Carpeta | Contenido |
|---|---|
| `evidencia-corrida-1/` | Reportes de la primera corrida (6/7, TC-06 falló por un defecto del script) |
| `evidencia-corrida-final/` | Reportes y evidencia de la corrida final (7/7) |
| `evidencia-video/` | Videos en cámara lenta (`SLOW_MO=800`) usados en el video de la entrega |

## Unidad 2: BDD con Cucumber

Historia **HU-SRE-01**, acceso de solo lectura para ingenieros de guardia:

> Como líder del equipo SRE, quiero dar a los ingenieros de guardia acceso de solo lectura a los tableros de Grafana, para que consulten el estado de los servicios durante un incidente sin riesgo de modificar los tableros por error.

| Archivo | Contenido |
|---|---|
| `src/test/resources/features/acceso_solo_lectura.feature` | Historia, criterios de aceptación y escenarios en Gherkin (español) |
| `src/test/java/.../bdd/AccesoSoloLecturaSteps.java` | Definiciones de pasos (Playwright para UI y API) |
| `src/test/java/.../bdd/GrafanaAdminApi.java` | Preparación de datos: tablero y cuentas por API |
| `src/test/java/.../bdd/RunCucumberTest.java` | Runner de Cucumber sobre JUnit Platform |

Cada escenario lleva la etiqueta del criterio que verifica (`@CA1` a `@CA4`).

```bash
./run-bdd.sh                 # solo los escenarios BDD
SLOW_MO=800 ./run-bdd.sh     # en cámara lenta, para grabar
./run-tests.sh               # todo: Unidad 1 + Unidad 2
```

Reporte HTML de Cucumber: `target/cucumber/reporte-bdd.html` (incluye la captura final de cada escenario de UI).

## Unidad 3: auditor de roles con TDD + BDD en CI/CD

Historia **HU-SRE-02**: como líder del equipo SRE, quiero un reporte automático de las cuentas cuyo rol supera lo que permite la política de guardia, para corregir privilegios de más antes de que se conviertan en un riesgo o en un hallazgo de auditoría.

| Capa | Archivo |
|---|---|
| Código de producción | `src/main/java/.../auditoria/` (`Rol`, `PoliticaAccesos`, `AuditorRoles`, `ClienteGrafanaApi`, `ReporteAuditoria`, `AuditorCli`) |
| TDD (unitarias, `@Tag("unit")`) | `src/test/java/.../auditoria/*Test.java` |
| BDD (Cucumber) | `src/test/resources/features/auditoria_roles.feature` + `bdd/AuditoriaRolesSteps.java` |
| Política de ejemplo | `politica-guardia.properties` |
| Pipeline | `.github/workflows/pruebas.yml` (TDD → BDD/E2E → SonarCloud con quality gate) |

```bash
mvn -B verify -Dgroups=unit     # solo TDD, con JaCoCo y umbral de cobertura (80 % líneas, 70 % ramas)
./run-tests.sh                   # suite completa contra Grafana en Docker
```

El historial de commits muestra los ciclos rojo → verde → refactor de TDD dentro del ciclo externo de BDD.

### Uso del auditor en un pipeline

`AuditorCli` termina con código `0` si la auditoría queda aprobada, `2` si encuentra cuentas con privilegios de más y `1` si no pudo consultar Grafana. Así puede correr como un job programado que falla cuando aparece una desviación de permisos.
