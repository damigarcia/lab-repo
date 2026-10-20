---

description: "Tareas para implementar la renovación de pólizas"
---

# Tasks: Renovación de póliza

**Input**: Design documents from `/specs/001-renovacion-poliza/`

**Prerequisites**: plan.md (required), spec.md (required for user stories)

**Tests**: OBLIGATORIOS en este proyecto (constitución, principio IV). Cada user story incluye sus tests y se escriben antes de la implementación.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- Backend: `backend/src/main/java/com/aseguradorademo/polizas/` (abreviado `BE/`) y `backend/src/test/java/com/aseguradorademo/polizas/` (abreviado `BT/`)
- Frontend: `frontend/src/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirmar punto de partida y reglas corporativas

- [ ] T001 Verificar que la línea base está en verde: `cd backend && mvn -q test` y `cd frontend && npm test && npm run build`
- [ ] T002 [P] Consultar el MCP `catalogo-coberturas` (`consultar_regla_suscripcion`) y confirmar los valores de la spec: 30 días, vigencia desde la fecha de fin, 10 %, 12 meses, excluye RECHAZADO
- [ ] T003 [P] Buscar usos de `contarUltimos12Meses`, `estaEnPeriodoDeGracia`, `calcularPrima` y `new PolizaResponse(` en todo el repo para dimensionar el impacto (principio II)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Modelo de datos y piezas compartidas que necesitan todas las historias

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T004 Actualizar `backend/src/main/resources/schema.sql`: columnas `poliza_origen_id BIGINT NULL` y `renovada_por_id BIGINT NULL`, `uk_polizas_poliza_origen` y las dos FKs. **No** tocar `ck_polizas_estado`
- [ ] T005 Agregar `polizaOrigenId`, `renovadaPorId`, `marcarRenovadaPor(Long)` y `estaRenovada()` en `BE/poliza/Poliza.java`, y un constructor o fábrica para la póliza renovada con `polizaOrigenId`
- [ ] T006 Agregar `Long polizaOrigenId` y `Long renovadaPorId` al final de `BE/poliza/PolizaResponse.java` y mapearlos en `BE/poliza/PolizaMapper.java`; en `BT/poliza/PolizaControllerTest.java` pasar `null` en el helper que construye el record, sin cambiar ninguna aserción existente
- [ ] T007 En `BE/poliza/PolizaService.java`, reemplazar el número mágico `30` de `estaEnPeriodoDeGracia` por la constante `DIAS_GRACIA` (sin cambio de comportamiento)
- [ ] T008 Crear los records `BE/poliza/RenovacionPreviewResponse.java` y `BE/poliza/ConfirmarRenovacionRequest.java` (`@NotNull @Positive BigDecimal primaAnual`)
- [ ] T009 Correr `cd backend && mvn -q test` (incluye `PolizasApplicationTests`, que valida esquema y entidad)

**Checkpoint**: Modelo listo; la app arranca y todos los tests previos pasan sin cambios de aserciones

---

## Phase 3: User Story 1 - Renovar una póliza elegible (Priority: P1) 🎯 MVP

**Goal**: Vista previa y confirmación desde el detalle; la nueva queda vigente y la original marcada como renovada sin cambiar de estado

**Independent Test**: Renovar POL-HOG-0002 (vigente, sin siniestros) desde el detalle y verificar la póliza nueva, el vínculo en los dos sentidos y que el listado previo no cambió

### Tests for User Story 1 ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T010 [P] [US1] En `BT/poliza/PolizaServiceTest.java` agregar tests: vista previa de VIGENTE (prima = `calcularPrima`, inicio = fin original, fin = +1 año) sin escribir nada; `renovar` crea la nueva vigente con `polizaOrigenId`, marca la original con `renovadaPorId` y **conserva su estado**; VENCIDA con fin hoy y hace 30 días (renovable, inicio en el pasado); inexistente (`NotFoundException`)
- [ ] T011 [P] [US1] En `BT/poliza/PolizaControllerTest.java` agregar tests: `GET /{id}/renovacion` 200; `POST /{id}/renovacion` 201 + `Location`; 404 `NO_ENCONTRADO`; 400 con body sin prima y con id no numérico
- [ ] T012 [P] [US1] Crear `frontend/src/utils/renovacion.test.js`: `puedeRenovar` es true para VIGENTE y para VENCIDA en gracia, false si tiene `renovadaPorId`, si es CANCELADA o si está VENCIDA fuera de gracia

### Implementation for User Story 1

- [ ] T013 [US1] Implementar en `BE/poliza/PolizaService.java` el método privado `calcularRenovacion(Poliza)`, `previsualizarRenovacion(Long id)` y `renovar(Long id, ConfirmarRenovacionRequest)` (`@Transactional`) según el plan, reutilizando `buscarPoliza`, `calcularPrima`, `estaEnPeriodoDeGracia`, `generarNumero` y `PolizaMapper`
- [ ] T014 [US1] Agregar `@GetMapping("/{id}/renovacion")` y `@PostMapping("/{id}/renovacion")` en `BE/poliza/PolizaController.java` (solo delegan; el POST devuelve `ResponseEntity.created(...)`)
- [ ] T015 [P] [US1] Agregar `previsualizarRenovacion(id)` y `renovarPoliza(id, primaAnual)` en `frontend/src/api/client.js`
- [ ] T016 [P] [US1] Crear `frontend/src/utils/renovacion.js` con `puedeRenovar(poliza)`
- [ ] T017 [US1] En `frontend/src/pages/PolizaDetallePage.jsx`: botón "Renovar" con `puedeRenovar`, panel de vista previa (cliente, tipo, suma, prima, vigencia) con Confirmar / Volver, navegación a la nueva al confirmar, vínculos "Renovada por" / "Renovación de"; montos y fechas con `formatMoneda` / `formatFecha`
- [ ] T018 [US1] Verificar SC-005: con la app recién levantada, `GET /api/polizas` y `GET /api/polizas/{id}` devuelven los mismos campos y valores que en `main`, más los dos campos nuevos en `null`
- [ ] T019 [US1] Correr `cd backend && mvn -q test` y `cd frontend && npm test && npm run build`; probar el flujo en el navegador con POL-HOG-0002 y POL-VID-0006
- [ ] T020 [P] [US1] Documentar RN-POL-10 (elegibilidad), RN-POL-11 (vigencia desde la fecha de fin) y RN-POL-12 (marca de renovación y vínculo) en `docs/REGLAS_NEGOCIO.md`; columnas nuevas en `docs/DATABASE.md`; endpoints nuevos en `README.md`

**Checkpoint**: La renovación funciona de punta a punta sin recargo; lo existente responde igual

---

## Phase 4: User Story 2 - Ajuste de prima por siniestralidad (Priority: P2)

**Goal**: La prima de la vista previa incluye el recargo del 10 % cuando corresponde y la confirmación guarda exactamente esa prima

**Independent Test**: Comparar la vista previa de POL-AUT-0001 (con siniestros recientes) y de POL-HOG-0002 (sin siniestros)

### Tests for User Story 2 ⚠️

- [ ] T021 [P] [US2] Crear `BT/siniestro/SiniestroServiceTest.java` con tests de `contarUltimos12Meses` usando `Clock.fixed`: cuenta no rechazados, excluye RECHAZADO, **incluye** el día exacto de 12 meses atrás, excluye el día anterior
- [ ] T022 [P] [US2] En `BT/poliza/PolizaServiceTest.java` agregar tests: con un siniestro que cuenta → prima base × 1,10 HALF_UP y `recargoAplicado = true`; con varios → un solo 10 %; con prima mínima → 110,00; confirmación con la prima mostrada guarda exactamente esa prima

### Implementation for User Story 2

- [ ] T023 [US2] Agregar `countByPolizaIdAndFechaOcurrenciaGreaterThanEqualAndEstadoNot` en `BE/siniestro/SiniestroRepository.java` y usarlo en `SiniestroService.contarUltimos12Meses`
- [ ] T024 [US2] Inyectar `SiniestroService` en `PolizaService`, agregar la constante `RECARGO_SINIESTRALIDAD` (0.10, catálogo v2025.1) y aplicarla en `calcularRenovacion`
- [ ] T025 [US2] En `PolizaDetallePage.jsx`, mostrar el aviso "Incluye recargo del 10 % por siniestralidad" cuando `recargoAplicado`
- [ ] T026 [P] [US2] Actualizar RN-SIN-06 (ventana inclusiva, excluye RECHAZADO) y agregar RN-POL-13 (recargo de renovación) en `docs/REGLAS_NEGOCIO.md`
- [ ] T027 [US2] Correr `cd backend && mvn -q test`

**Checkpoint**: US1 y US2 funcionan de forma independiente

---

## Phase 5: User Story 3 - Rechazar renovaciones no permitidas (Priority: P2)

**Goal**: Ninguna renovación no permitida crea una póliza y el operador recibe el motivo en español

**Independent Test**: Intentar renovar POL-HOG-0005 (cancelada), POL-AUT-0004 (vencida hace 90 días) y una póliza ya renovada

### Tests for User Story 3 ⚠️

- [ ] T028 [P] [US3] En `BT/poliza/PolizaServiceTest.java` agregar tests: CANCELADA → `POLIZA_NO_RENOVABLE`; VENCIDA hace 31 días → `PLAZO_RENOVACION_VENCIDO`; ya renovada → `POLIZA_YA_RENOVADA`, también si además está vencida fuera de plazo; prima confirmada distinta de la recalculada → `PRIMA_DESACTUALIZADA`; `DataIntegrityViolationException` al guardar → `POLIZA_YA_RENOVADA`. En todos, verificar que no se llamó a `save` (o que se revirtió)
- [ ] T029 [P] [US3] En `BT/poliza/PolizaControllerTest.java` agregar tests de 422 con cada código

### Implementation for User Story 3

- [ ] T030 [US3] Completar en `calcularRenovacion` el orden de validaciones del plan (ya renovada primero) y en `renovar` la comparación de prima y la captura de `DataIntegrityViolationException`
- [ ] T031 [US3] En `PolizaDetallePage.jsx`, ante `PRIMA_DESACTUALIZADA` volver a pedir la vista previa y mostrar el mensaje; ante otros 422, mostrar el mensaje de la API sin salir del detalle
- [ ] T032 [US3] Correr `cd backend && mvn -q test`

**Checkpoint**: Todas las user stories funcionan de forma independiente

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Verificación final y revisión

- [ ] T033 Correr backend y frontend completos en verde (`mvn -q test`, `npm test`, `npm run build`)
- [ ] T034 Ejecutar el subagente `code-reviewer` contra esta spec; resolver hallazgos de severidad Alta
- [ ] T035 [P] Verificar SC-005 y FR-012: `git diff main -- backend/src/test` solo agrega tests o pasa `null` a los campos nuevos; ninguna aserción existente cambió; `ck_polizas_estado` y `EstadoPoliza` sin cambios
- [ ] T036 Recorrer `.claude/skills/nuevo-endpoint-rest/references/checklist-endpoint.md` y dejar el resultado en la descripción del PR

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: sin dependencias
- **Foundational (Phase 2)**: depende de Setup; BLOQUEA todas las historias
- **US1 (Phase 3)**: depende de Foundational
- **US2 (Phase 4)**: depende de Foundational; usa `calcularRenovacion` de US1
- **US3 (Phase 5)**: depende de Foundational; completa las validaciones de `calcularRenovacion` y `renovar` de US1
- **Polish (Phase 6)**: depende de las historias que se entreguen

### Within Each User Story

- Tests primero (deben fallar), luego implementación
- Modelo → service → controller → frontend
- Docs en la misma historia que introduce la regla

### Parallel Opportunities

- T002 y T003 en paralelo
- T010, T011 y T012 en paralelo; T015 y T016 en paralelo con T013–T014; T020 en paralelo con la implementación
- T021 y T022 en paralelo; T026 en paralelo con T023–T025
- T028 y T029 en paralelo

---

## Parallel Example: User Story 1

```bash
# Tests de US1 en paralelo:
Task: "Tests de vista previa y renovar en backend/src/test/java/com/aseguradorademo/polizas/poliza/PolizaServiceTest.java"
Task: "Tests de los endpoints en backend/src/test/java/com/aseguradorademo/polizas/poliza/PolizaControllerTest.java"
Task: "Tests de puedeRenovar en frontend/src/utils/renovacion.test.js"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Phase 1 + Phase 2
2. Phase 3 (US1)
3. **STOP and VALIDATE**: probar los 7 escenarios de US1 en el navegador
4. Demo

### Incremental Delivery

1. Foundational → US1 (vista previa + confirmación) → demo
2. US2 (recargo) → demo
3. US3 (rechazos y prima desactualizada) → demo
4. Polish → PR

---

## Notes

- [P] tasks = archivos distintos, sin dependencias
- La etiqueta [Story] permite trazar cada tarea a su user story y a los FR de la spec
- Commit por tarea o grupo lógico con mensajes `feat:` / `test:` / `docs:` / `refactor:` que referencien `specs/001-renovacion-poliza`
- No implementar comportamiento que la spec no pide (edad máxima VIDA, refactor de `SiniestroController`)
