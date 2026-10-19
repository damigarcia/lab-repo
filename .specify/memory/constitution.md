<!--
Sync Impact Report
- Versión: (plantilla) → 1.0.0
- Principios definidos: I. Arquitectura por feature y capas · II. Reutilizar antes de crear ·
  III. Contratos de API compatibles · IV. Tests obligatorios · V. Reglas de negocio trazables ·
  VI. Seguridad y datos · VII. Simplicidad y dependencias mínimas
- Secciones agregadas: Restricciones técnicas · Flujo de desarrollo y quality gates · Gobierno
- Plantillas revisadas: .specify/templates/plan-template.md (Constitution Check usa los principios I–VII) (OK),
  spec-template.md (OK), tasks-template.md (OK) (los tests NO son opcionales en este proyecto, ver principio IV)
- Pendientes: ninguno
-->

# Constitución · Gestión de Pólizas (Aseguradora Demo)

Principios no negociables para cualquier cambio en este repositorio, hecho por personas o por agentes.
Derivados del código existente y de `CONTRIBUTING.md`, `docs/ARQUITECTURA.md`, `docs/DATABASE.md`
y `docs/REGLAS_NEGOCIO.md`. Las reglas operativas para el agente están en `.cursor/rules/`.

## Core Principles

### I. Arquitectura por feature y por capas

- El backend se organiza por feature bajo `com.aseguradorademo.polizas.<feature>` (`poliza`, `cliente`,
  `siniestro`) con `common/` para lo transversal. Una feature nueva crea su propio paquete; una regla nueva
  de una feature existente va **en su paquete**.
- Capas con responsabilidades estrictas: **Controller** (HTTP ↔ DTO, `@Valid`, códigos de estado) →
  **Service** (toda regla de negocio, `@Transactional`) → **Repository** (consultas). Los controllers no
  contienen reglas de negocio ni usan repositorios.
- Una feature consume a otra a través de su **service**, nunca de su repository.
- DTOs como `record` (`*Response`, `Crear*Request`, `<Accion>*Request`) y mappers estáticos. Las entidades
  JPA **nunca** se exponen en la API.
- Los errores se expresan con `NotFoundException` / `ReglaNegocioException` y solo `GlobalExceptionHandler`
  construye respuestas de error (`ErrorResponse`).

*Razón:* cambios localizados, revisión predecible y contratos desacoplados del esquema (ADR-001, ADR-002, ADR-003).

### II. Reutilizar antes de crear (Reuse Before Creation)

- Antes de escribir un método, clase, utilidad, endpoint o componente nuevo, el plan **debe** listar lo que
  ya existe y sirve: services (`PolizaService`, `SiniestroService`, `ClienteService`), enums, mappers,
  `api/client.js`, `utils/format.js`, componentes (`EstadoBadge`, `Mensaje`).
- Si algo existente casi sirve, se **extiende o refactoriza** (con tests) en lugar de duplicarse.
- Duplicar lógica de negocio o de formato es un defecto que bloquea la revisión.

*Razón:* la deuda que más cuesta en este código es la duplicación (reglas y formatos que divergen).

### III. Contratos de API compatibles

- Los endpoints y respuestas existentes son contratos con otros equipos: **no** se renombran, quitan ni
  cambian de tipo campos, rutas, parámetros ni códigos de estado.
- Se permite **agregar**: endpoints nuevos, campos nuevos opcionales en respuestas, valores nuevos de enum
  (documentados en `docs/DATABASE.md`).
- Convenciones obligatorias: rutas en plural bajo `/api`, acciones como sub-recurso sustantivado
  (`POST /api/polizas/{id}/cancelacion`), 201 + `Location` al crear, 400 validación, 404 inexistente,
  422 regla de negocio. Fechas ISO-8601, montos decimales.
- Un cambio incompatible requiere una enmienda explícita en el plan (sección Complexity Tracking) y
  aprobación del responsable técnico.

### IV. Tests obligatorios (NO NEGOCIABLE)

- Todo cambio de comportamiento en backend incluye: test unitario del service con Mockito (éxito y cada
  regla que falla) **y** test de controller con `@WebMvcTest` + MockMvc (2xx, 400, 404, 422 según aplique).
- Las reglas que dependen de la fecha se testean con `Clock.fixed`.
- Cambios de esquema se validan con `PolizasApplicationTests` (contexto + `schema.sql` + `data.sql`).
- Utilidades del frontend se testean con Vitest.
- Ningún cambio se da por terminado sin `mvn -q test`, `npm test` y `npm run build` en verde. Está prohibido
  borrar, desactivar o debilitar tests existentes para que pasen.
- En `tasks.md`, las tareas de test **no son opcionales**.

### V. Reglas de negocio documentadas y trazables

- Fuentes de verdad, en este orden: (1) catálogo corporativo (MCP `catalogo-coberturas`:
  `consultar_regla_suscripcion`, `listar_coberturas`), (2) `docs/REGLAS_NEGOCIO.md`, (3) el código.
  Si el catálogo y el documento difieren, manda el catálogo y se actualiza el documento en el mismo cambio.
- Cada regla implementada tiene un identificador (`RN-XXX-nn`) en `docs/REGLAS_NEGOCIO.md` y se referencia
  desde la spec (FR-xxx) y los tests.
- Sin números mágicos: plazos, tasas y porcentajes son constantes con nombre o atributos de enum.
- "Hoy" se obtiene siempre del `Clock` inyectado.
- El modelo de datos sigue `docs/DATABASE.md` (snake_case, enums como VARCHAR + CHECK, montos DECIMAL).
  Todo cambio de modelo actualiza en el mismo cambio: entidad, `schema.sql`, `data.sql` (si aplica) y
  `docs/DATABASE.md`.

### VI. Seguridad y protección de datos

- Prohibido incluir secretos (claves, tokens, cadenas de conexión reales) en código, tests, docs o commits.
- Prohibido usar datos reales de clientes: solo datos ficticios (`@ejemplo.com`, documentos `V-1000000x`).
- Toda entrada externa se valida con Bean Validation en el DTO y con reglas en el service; el frontend no
  es una barrera de seguridad.
- Consultas solo con Spring Data / JPQL parametrizado. Sin concatenación de strings.
- Las respuestas de error no exponen stack traces ni detalles internos; los logs no contienen datos personales.
- Ver `.cursor/rules/seguridad.mdc` (siempre activa).

### VII. Simplicidad y dependencias mínimas

- No se agregan dependencias (Maven o npm) sin justificación en el plan y revisión de vulnerabilidades.
- Se elige la solución más simple que cumpla la spec; nada de capas, abstracciones o configuraciones
  "por si acaso" (YAGNI).
- El frontend no usa librerías de UI; JavaScript, no TypeScript.

## Restricciones técnicas

| Ámbito | Restricción |
|--------|-------------|
| Backend | Java 17 (compatible con 21), Spring Boot 3.3.x, Maven, Spring Data JPA, Bean Validation |
| Base de datos | H2 en memoria; esquema versionado en `backend/src/main/resources/schema.sql`; Hibernate `ddl-auto=validate` |
| Frontend | React 18 + Vite, JavaScript, React Router, CSS simple; HTTP solo vía `src/api/client.js`; formato solo vía `src/utils/format.js` |
| Tests | JUnit 5, Mockito, MockMvc, Vitest |
| Idioma | Código de dominio, mensajes, docs y commits en español neutro; identificadores sin tildes |
| Conocimiento corporativo | MCP local `mcp/catalogo-coberturas` (consultado en diseño; la app no depende de él en runtime) |

## Flujo de desarrollo y quality gates

1. **Especificar** (`/speckit-specify`, `/speckit-clarify`): la spec describe el *qué* y el *por qué*, con
   user stories priorizadas, escenarios Given/When/Then, FR-xxx y criterios de éxito medibles. Las dudas se
   resuelven antes del plan; no se planifica con `[NEEDS CLARIFICATION]` abiertos.
2. **Planificar** (`/speckit-plan`): el plan pasa el *Constitution Check* (principios I–VII), lista qué se
   reutiliza (principio II) y explicita cambios de contrato y de esquema.
3. **Tareas** (`/speckit-tasks`): organizadas por user story, con tests incluidos y rutas exactas.
4. **Implementar** (`/speckit-implement`): siguiendo `.cursor/rules/` y el skill `nuevo-endpoint-rest`
   cuando aplique.
5. **Verificar**: subagente `test-runner` (suites en verde) y subagente `code-reviewer` (rules, OWASP básico
   y cobertura de la spec). Hallazgos críticos o altos bloquean el merge.
6. **Trazabilidad**: los hooks de `.cursor/hooks.json` bloquean comandos destructivos y registran las
   ediciones del agente en `.cursor/audit/`. Los commits siguen el formato de `CONTRIBUTING.md`
   (`feat: ...`, `fix: ...`) y referencian la spec (`specs/NNN-*`).

## Governance

- Esta constitución prevalece sobre cualquier otra práctica del repositorio. `CONTRIBUTING.md`, los docs y
  `.cursor/rules/` la detallan operativamente; si alguno la contradice, se corrige ese archivo o se enmienda
  la constitución, nunca se ignora en silencio.
- **Enmiendas**: se proponen por PR que modifique este archivo, con el *Sync Impact Report* actualizado,
  justificación y plan de migración si afecta código existente. Requieren aprobación del responsable técnico
  del equipo.
- **Versionado** (SemVer): MAJOR si se elimina o redefine un principio de forma incompatible; MINOR si se
  agrega un principio o sección; PATCH para aclaraciones de redacción.
- **Cumplimiento**: cada plan incluye el Constitution Check; cada revisión de código (humana o del
  subagente `code-reviewer`) verifica los principios. Las excepciones se registran en la sección
  *Complexity Tracking* del plan con su justificación.

**Version**: 1.0.0 | **Ratified**: 2026-10-19 | **Last Amended**: 2026-10-19
