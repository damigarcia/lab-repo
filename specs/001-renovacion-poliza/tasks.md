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
- [ ] T002 [P] Consultar el MCP `catalogo-coberturas` (`consultar_regla_suscripcion` para AUTO, HOGAR y VIDA) y confirmar los valores de la spec: 30 días, 10 %, 12 meses, excluye RECHAZADO, edad máxima renovación VIDA 70
- [ ] T003 [P] Buscar usos de `contarUltimos12Meses`, `estaEnPeriodoDeGracia` y `EstadoPoliza` en todo el repo para dimensionar el impacto (principio II)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Modelo de datos y piezas compartidas que necesitan todas las historias

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T004 Agregar `RENOVADA` a `BE/poliza/EstadoPoliza.java`
- [ ] T005 Actualizar `backend/src/main/resources/schema.sql`: `RENOVADA` en `ck_polizas_estado`, columna `poliza_origen_id BIGINT NULL` y `fk_polizas_poliza_origen`
- [ ] T006 Agregar `polizaOrigenId` (`@Column(name = "poliza_origen_id")`), `marcarRenovada()` y `admiteSiniestroEn(LocalDate)` en `BE/poliza/Poliza.java`
- [ ] T007 Agregar `Long polizaOrigenId` al final de `BE/poliza/PolizaResponse.java` y mapearlo en `BE/poliza/PolizaMapper.java`; actualizar los constructores del record en `BT/poliza/PolizaControllerTest.java` (pasar `null`) sin cambiar ninguna aserción existente
- [ ] T008 En `BE/poliza/PolizaService.java`, reemplazar el número mágico `30` de `estaEnPeriodoDeGracia` por la constante `DIAS_MAXIMOS_RENOVACION` (sin cambio de comportamiento; los tests de gracia existentes deben seguir pasando)
- [ ] T009 [P] Agregar `countByPolizaIdAndFechaOcurrenciaAfterAndEstadoNot` en `BE/siniestro/SiniestroRepository.java` y usarlo en `SiniestroService.contarUltimos12Meses` excluyendo `RECHAZADO`
- [ ] T010 [P] Crear `BT/siniestro/SiniestroServiceTest.java` con tests de `contarUltimos12Meses` (cuenta no rechazados, excluye RECHAZADO, excluye exactamente 12 meses) usando `Clock.fixed`
- [ ] T011 Correr `cd backend && mvn -q test` (incluye `PolizasApplicationTests`, que valida esquema y entidad)

**Checkpoint**: Modelo listo; la app arranca y todos los tests previos pasan

---

## Phase 3: User Story 1 - Renovar una póliza elegible (Priority: P1) 🎯 MVP

**Goal**: `POST /api/polizas/{id}/renovacion` crea la póliza nueva con las reglas de la spec y marca la original como RENOVADA

**Independent Test**: Llamar al endpoint sobre los datos de demo (POL-HOG-0002, POL-AUT-0001, POL-VID-0003, POL-AUT-0004, POL-HOG-0005) y verificar respuestas y estados

### Tests for User Story 1 ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T012 [P] [US1] En `BT/poliza/PolizaServiceTest.java` agregar tests de `renovar`: VIGENTE sin siniestros (prima igual, fechas FR-003, `polizaOrigenId`, original RENOVADA); con siniestros (prima +10 %, redondeo HALF_UP); VENCIDA hace 30 días (renovable); VENCIDA hace 31 días (`PLAZO_RENOVACION_VENCIDO`); CANCELADA y RENOVADA (`POLIZA_NO_RENOVABLE`); VIDA con cliente de 71 años (`EDAD_MAXIMA_RENOVACION`); inexistente (`NotFoundException`)
- [ ] T013 [P] [US1] En `BT/poliza/PolizaControllerTest.java` agregar tests del endpoint: 201 + `Location`, 404 `NO_ENCONTRADO`, 422 con código de regla, 400 con id no numérico

### Implementation for User Story 1

- [ ] T014 [US1] Inyectar `SiniestroService` en `BE/poliza/PolizaService.java` y agregar constantes `RECARGO_SINIESTRALIDAD` (0.10) y `EDAD_MAXIMA_RENOVACION_VIDA` (70)
- [ ] T015 [US1] Implementar `PolizaService.renovar(Long id)` (`@Transactional`) según el paso a paso del plan, reutilizando `buscarPoliza`, `generarNumero` y `PolizaMapper`
- [ ] T016 [US1] Agregar `@PostMapping("/{id}/renovacion")` en `BE/poliza/PolizaController.java` devolviendo `ResponseEntity.created(...)` (una línea de delegación)
- [ ] T017 [US1] Correr `cd backend && mvn -q test` y probar manualmente con `curl -X POST localhost:8080/api/polizas/1/renovacion`
- [ ] T018 [P] [US1] Documentar RN-POL-10 a RN-POL-14 (elegibilidad, vigencia, prima, estado RENOVADA, edad VIDA) en `docs/REGLAS_NEGOCIO.md`, actualizar RN-SIN-06 (excluye RECHAZADO) y el diagrama de estados
- [ ] T019 [P] [US1] Actualizar `docs/DATABASE.md` (enum `EstadoPoliza`, columna `poliza_origen_id`) y la tabla de endpoints de `README.md`

**Checkpoint**: La renovación funciona vía API y es testeable de forma independiente

---

## Phase 4: User Story 2 - Botón "Renovar" en el detalle (Priority: P2)

**Goal**: El operador renueva desde la interfaz

**Independent Test**: Renovar POL-HOG-0002 desde el detalle y verificar la navegación a la póliza nueva y la etiqueta "Renovada" en el listado

### Tests for User Story 2 ⚠️

- [ ] T020 [P] [US2] En `frontend/src/utils/format.test.js` agregar test: `formatEstado('RENOVADA')` devuelve `'Renovada'`

### Implementation for User Story 2

- [ ] T021 [P] [US2] Agregar `RENOVADA: 'Renovada'` en `ETIQUETAS_ESTADO` de `frontend/src/utils/format.js`
- [ ] T022 [P] [US2] Agregar `renovarPoliza: (id) => request(\`/polizas/${id}/renovacion\`, { method: 'POST' })` en `frontend/src/api/client.js`
- [ ] T023 [P] [US2] Agregar `.badge-renovada` en `frontend/src/styles.css`
- [ ] T024 [US2] En `frontend/src/pages/PolizaDetallePage.jsx`: botón "Renovar" visible si `estado === 'VIGENTE' || enPeriodoDeGracia`, confirmación, mensaje de éxito con `formatMoneda` y navegación a `/polizas/{nuevoId}`, error con `Mensaje`
- [ ] T025 [US2] En `frontend/src/pages/PolizasPage.jsx` agregar `RENOVADA` a `ESTADOS`
- [ ] T026 [US2] Correr `cd frontend && npm test && npm run build` y probar el flujo en el navegador

**Checkpoint**: US1 y US2 funcionan de forma independiente

---

## Phase 5: User Story 3 - Continuidad de cobertura (Priority: P3)

**Goal**: Se pueden registrar siniestros sobre pólizas RENOVADA dentro de su vigencia; la lógica sale del controller

**Independent Test**: Renovar POL-AUT-0001 y registrar un siniestro de hoy sobre la póliza original (201)

### Tests for User Story 3 ⚠️

- [ ] T027 [P] [US3] En `BT/siniestro/SiniestroServiceTest.java` agregar tests de `registrar`: VIGENTE ok, RENOVADA en vigencia ok, RENOVADA fuera de vigencia (`FUERA_DE_VIGENCIA`), VENCIDA/CANCELADA (`POLIZA_NO_VIGENTE`), monto mayor a la suma (`MONTO_EXCEDE_SUMA`)
- [ ] T028 [P] [US3] Crear `BT/siniestro/SiniestroControllerTest.java` (`@WebMvcTest`): GET por póliza 200, POST 201, 400 validación, 404, 422

### Implementation for User Story 3

- [ ] T029 [US3] Agregar `PolizaService.obtenerEntidad(Long id)` (uso interno, no expuesto por HTTP) en `BE/poliza/PolizaService.java`
- [ ] T030 [US3] Mover las validaciones de `BE/siniestro/SiniestroController.java` a `SiniestroService.registrar(Poliza, request)` usando `Poliza.admiteSiniestroEn`; mantener los mismos códigos de error
- [ ] T031 [US3] Dejar `SiniestroController` sin `PolizaRepository` ni reglas: obtiene la póliza con `PolizaService.obtenerEntidad` y delega
- [ ] T032 [US3] Actualizar RN-SIN-01 en `docs/REGLAS_NEGOCIO.md` (VIGENTE o RENOVADA dentro de vigencia)
- [ ] T033 [US3] Correr `cd backend && mvn -q test`

**Checkpoint**: Todas las user stories funcionan de forma independiente

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Verificación final y revisión

- [ ] T034 Ejecutar el subagente `test-runner` (backend + frontend en verde)
- [ ] T035 Ejecutar el subagente `code-reviewer` contra esta spec; resolver hallazgos CRÍTICO y ALTO
- [ ] T036 [P] Verificar SC-004: `git diff main -- backend/src/test` solo agrega tests o pasa `null` al nuevo campo; ninguna aserción existente cambió
- [ ] T037 Recorrer `.cursor/skills/nuevo-endpoint-rest/references/checklist-endpoint.md` y dejar el resultado en la descripción del PR

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: sin dependencias
- **Foundational (Phase 2)**: depende de Setup; BLOQUEA todas las historias
- **US1 (Phase 3)**: depende de Foundational
- **US2 (Phase 4)**: depende de Foundational para el contrato; para probar de punta a punta necesita US1
- **US3 (Phase 5)**: depende de Foundational (`RENOVADA`, `admiteSiniestroEn`); para su prueba independiente necesita US1
- **Polish (Phase 6)**: depende de las historias que se entreguen

### Within Each User Story

- Tests primero (deben fallar), luego implementación
- Modelo → service → controller → frontend
- Docs en la misma historia que introduce la regla

### Parallel Opportunities

- T002 y T003 en paralelo
- T009 y T010 en paralelo con T004–T008 (archivos distintos)
- T012 y T013 en paralelo; T018 y T019 en paralelo con T014–T016
- T020–T023 en paralelo (archivos distintos)
- T027 y T028 en paralelo

---

## Parallel Example: User Story 1

```bash
# Tests de US1 en paralelo:
Task: "Tests de renovar en backend/src/test/java/com/aseguradorademo/polizas/poliza/PolizaServiceTest.java"
Task: "Tests del endpoint en backend/src/test/java/com/aseguradorademo/polizas/poliza/PolizaControllerTest.java"

# Docs de US1 en paralelo con la implementación:
Task: "RN-POL-10..14 en docs/REGLAS_NEGOCIO.md"
Task: "EstadoPoliza y poliza_origen_id en docs/DATABASE.md"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Phase 1 + Phase 2
2. Phase 3 (US1)
3. **STOP and VALIDATE**: probar los 7 escenarios de US1 con curl
4. Demo de la API

### Incremental Delivery

1. Foundational → US1 (API) → demo
2. US2 (UI) → demo con operadores
3. US3 (continuidad de cobertura + refactor de siniestros) → demo
4. Polish → PR

---

## Notes

- [P] tasks = archivos distintos, sin dependencias
- La etiqueta [Story] permite trazar cada tarea a su user story y a los FR de la spec
- Commit por tarea o grupo lógico con mensajes `feat:` / `test:` / `docs:` / `refactor:` que referencien `specs/001-renovacion-poliza`
- No implementar comportamiento que la spec no pide
