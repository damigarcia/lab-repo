# Implementation Plan: Renovación de póliza

**Branch**: `001-renovacion-poliza` | **Date**: 2026-10-19 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/001-renovacion-poliza/spec.md`

## Summary

Agregar la acción `POST /api/polizas/{id}/renovacion` en la feature `poliza`, implementada como un método nuevo
`PolizaService.renovar(id)` que reutiliza la búsqueda, la generación de número y el mapper existentes, y la
regla de período de gracia (30 días) ya presente en `estaEnPeriodoDeGracia`. La siniestralidad se obtiene de
`SiniestroService.contarUltimos12Meses` (ajustado para excluir `RECHAZADO`, según el catálogo corporativo). Se
agrega el estado `RENOVADA` y la columna `poliza_origen_id`. En el frontend se agrega `api.renovarPoliza` y el
botón "Renovar" en `PolizaDetallePage`. Sin dependencias nuevas.

## Technical Context

**Language/Version**: Java 17 (compatible con 21) · JavaScript (ES2022) con React 18

**Primary Dependencies**: Spring Boot 3.3.x (web, data-jpa, validation) · Vite 6, React Router · **sin dependencias nuevas**

**Storage**: H2 en memoria; esquema en `backend/src/main/resources/schema.sql`, datos en `data.sql`; Hibernate `ddl-auto=validate`

**Testing**: JUnit 5 + Mockito (services), `@WebMvcTest` + MockMvc (controllers), `PolizasApplicationTests` (esquema), Vitest (utils frontend)

**Target Platform**: Servidor Linux/Windows con JVM 17+; navegadores modernos

**Project Type**: Aplicación web (backend REST + SPA)

**Performance Goals**: Renovación < 300 ms p95 en entorno local (una lectura de póliza, un conteo de siniestros, dos escrituras)

**Constraints**:
- Todo el código backend nuevo vive en el paquete `com.aseguradorademo.polizas.poliza` (y un ajuste puntual en `siniestro`).
- Reutilizar `PolizaService` (no crear un `RenovacionService` aparte).
- El cambio de modelo se hace en `schema.sql` (no hay Flyway en este repo) y se refleja en la entidad y en `docs/DATABASE.md`.
- No cambiar contratos existentes: solo se agregan endpoint, valor de enum y campo `polizaOrigenId`.
- Constantes con nombre para 30 días, 10 %, 12 meses y 70 años; "hoy" vía `Clock`.
- Sin dependencias Maven ni npm nuevas.

**Scale/Scope**: 1 endpoint nuevo, ~6 archivos backend modificados, 4 archivos frontend, 2 docs

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principio | Cómo lo cumple el plan | Estado |
|-----------|------------------------|--------|
| I. Arquitectura por feature y capas | Endpoint en `PolizaController` (una línea de delegación); reglas en `PolizaService.renovar`; respuesta `PolizaResponse` vía `PolizaMapper`; errores con `ReglaNegocioException`. La siniestralidad se pide a `SiniestroService` (service, no repository). | PASA |
| II. Reutilizar antes de crear | Se reutilizan: `buscarPoliza`, `generarNumero`, `PolizaMapper`, la regla de 30 días de `estaEnPeriodoDeGracia` (se extrae la constante y se comparte), `SiniestroService.contarUltimos12Meses`, `ApiError`/`Mensaje`/`EstadoBadge`/`formatEstado` en el frontend. Nada nuevo duplicado. | PASA |
| III. Contratos compatibles | Solo adiciones: `POST /{id}/renovacion`, valor `RENOVADA`, campo `polizaOrigenId` en `PolizaResponse`. Tests existentes sin cambios. | PASA |
| IV. Tests obligatorios | Tests de service (cada regla), de controller (201/404/422), de contexto (esquema) y Vitest (`formatEstado`). Se agregan tests al ajuste de siniestros (feature hoy sin tests). | PASA |
| V. Reglas trazables | Reglas tomadas del MCP `catalogo-coberturas` (v2025.1); se agregan RN-POL-10 a RN-POL-14 en `docs/REGLAS_NEGOCIO.md` y se actualiza RN-SIN-01/06. Constantes con nombre. | PASA |
| VI. Seguridad y datos | Sin cuerpo de request (id validado por tipo); sin datos reales; sin SQL concatenado; mensajes de error sin detalles internos. | PASA |
| VII. Simplicidad | Sin dependencias, sin servicio nuevo, sin tabla nueva (solo una columna). | PASA |

## Project Structure

### Documentation (this feature)

```text
specs/001-renovacion-poliza/
├── spec.md      # Especificación (qué y por qué)
├── plan.md      # Este archivo
└── tasks.md     # Tareas por user story
```

### Source Code (repository root)

```text
backend/src/main/java/com/aseguradorademo/polizas/
├── poliza/
│   ├── EstadoPoliza.java          # + RENOVADA
│   ├── Poliza.java                # + polizaOrigenId, + marcarRenovada(), + admiteSiniestroEn(fecha)
│   ├── PolizaResponse.java        # + polizaOrigenId (campo agregado al final)
│   ├── PolizaMapper.java          # mapea polizaOrigenId
│   ├── PolizaService.java         # + renovar(id), + obtenerEntidad(id); constantes DIAS_MAXIMOS_RENOVACION, RECARGO_SINIESTRALIDAD, EDAD_MAXIMA_RENOVACION_VIDA
│   └── PolizaController.java      # + POST /{id}/renovacion
└── siniestro/
    ├── SiniestroRepository.java   # + countByPolizaIdAndFechaOcurrenciaAfterAndEstadoNot
    ├── SiniestroService.java      # contarUltimos12Meses excluye RECHAZADO; registrar() valida (acepta RENOVADA en vigencia)
    └── SiniestroController.java   # sin PolizaRepository ni reglas: delega (ver Complexity Tracking)

backend/src/main/resources/
├── schema.sql                     # CHECK estado + RENOVADA; columna poliza_origen_id + FK
└── data.sql                       # sin cambios obligatorios

backend/src/test/java/com/aseguradorademo/polizas/
├── poliza/PolizaServiceTest.java      # + tests de renovar
├── poliza/PolizaControllerTest.java   # + tests del endpoint
├── siniestro/SiniestroServiceTest.java    # nuevo
└── siniestro/SiniestroControllerTest.java # nuevo

frontend/src/
├── api/client.js                  # + renovarPoliza(id)
├── utils/format.js                # + etiqueta RENOVADA
├── utils/format.test.js           # + test de RENOVADA
├── pages/PolizaDetallePage.jsx    # + botón Renovar
├── pages/PolizasPage.jsx          # + RENOVADA en filtro
└── styles.css                     # + .badge-renovada

docs/
├── REGLAS_NEGOCIO.md              # RN-POL-10..14, ajuste RN-SIN-01 y RN-SIN-06
└── DATABASE.md                    # estado RENOVADA, columna poliza_origen_id
```

**Structure Decision**: Aplicación web existente (`backend/` + `frontend/`). La feature se implementa dentro de
la feature `poliza` del backend, sin paquetes nuevos.

## Diseño

### Contrato

```
POST /api/polizas/{id}/renovacion
(sin cuerpo)

201 Created
Location: /api/polizas/{nuevoId}
Body: PolizaResponse (de la póliza nueva) — incluye el nuevo campo "polizaOrigenId": {id}

400 SOLICITUD_INVALIDA        id no numérico
404 NO_ENCONTRADO             póliza inexistente
422 POLIZA_NO_RENOVABLE       estado CANCELADA o RENOVADA
422 PLAZO_RENOVACION_VENCIDO  VENCIDA hace más de 30 días
422 EDAD_MAXIMA_RENOVACION    VIDA con cliente > 70 años a la nueva fecha de inicio
```

`PolizaResponse` agrega `Long polizaOrigenId` **al final** del record (los consumidores JSON no se ven
afectados; los tests que construyen el record se actualizan con `null`).

### Modelo de datos

```sql
-- schema.sql (tabla polizas)
poliza_origen_id  BIGINT NULL,
CONSTRAINT fk_polizas_poliza_origen FOREIGN KEY (poliza_origen_id) REFERENCES polizas (id),
CONSTRAINT ck_polizas_estado CHECK (estado IN ('VIGENTE', 'VENCIDA', 'CANCELADA', 'RENOVADA'))
```

Entidad: `@Column(name = "poliza_origen_id") private Long polizaOrigenId;` (id simple, sin relación
`@ManyToOne`, para no agregar carga perezosa innecesaria).

### Lógica de `PolizaService.renovar(Long id)` (`@Transactional`)

1. `buscarPoliza(id)` → 404 si no existe.
2. Estado: `CANCELADA`/`RENOVADA` → `POLIZA_NO_RENOVABLE`; `VENCIDA` y `hoy > fechaFin + DIAS_MAXIMOS_RENOVACION`
   → `PLAZO_RENOVACION_VENCIDO`. Reutilizar la misma constante que `estaEnPeriodoDeGracia`
   (renombrar el `30` actual a `DIAS_MAXIMOS_RENOVACION`, que es también el período de gracia — RN-POL-06).
3. Nueva vigencia: `inicio = fechaFin + 1 día`, `fin = fechaFin + 1 año`.
4. VIDA: edad del cliente a `inicio` (`Period.between(fechaNacimiento, inicio).getYears()`) `> 70` →
   `EDAD_MAXIMA_RENOVACION`.
5. Prima: `primaAnual`; si `siniestroService.contarUltimos12Meses(id) > 0` → `× (1 + RECARGO_SINIESTRALIDAD)`,
   `setScale(2, HALF_UP)`.
6. Crear la póliza nueva (número con `generarNumero(tipo)`, `polizaOrigenId = id`), `poliza.marcarRenovada()`,
   `save` de la nueva. La original se persiste por dirty checking en la misma transacción.
7. Devolver `PolizaMapper.toResponse(nueva, false)`.

Concurrencia (edge case de doble renovación): la segunda transacción ve la original en `RENOVADA` y recibe
`POLIZA_NO_RENOVABLE`. Para el entorno H2 de laboratorio no se agrega bloqueo optimista (`@Version`); se deja
anotado como mejora (ver Complexity Tracking).

### Siniestros (US3)

- `SiniestroService.contarUltimos12Meses` pasa a usar
  `countByPolizaIdAndFechaOcurrenciaAfterAndEstadoNot(polizaId, desde, EstadoSiniestro.RECHAZADO)`
  (hoy no tiene otros usos, verificado con búsqueda).
- Las validaciones de registro que hoy están en `SiniestroController` se mueven a `SiniestroService.registrar(Poliza, request)`
  (firma existente) y se amplía la regla de estado: se acepta `VIGENTE` o `RENOVADA`, siempre con la fecha de
  ocurrencia dentro de la vigencia. La regla de cobertura se expresa como método de dominio
  `Poliza.admiteSiniestroEn(LocalDate)`.
- El controller deja de usar `PolizaRepository`: obtiene la entidad con un método nuevo de uso interno
  `PolizaService.obtenerEntidad(id)` (no se expone por HTTP) y delega en `SiniestroService.registrar`.
- **Dependencias entre beans** (sin ciclos): `PolizaService → SiniestroService` (siniestralidad) y
  `SiniestroController → PolizaService, SiniestroService`. `SiniestroService` **no** depende de `PolizaService`;
  Spring Boot rechaza referencias circulares al arrancar.

### Frontend (US2)

- `api.renovarPoliza(id)` → `request(\`/polizas/${id}/renovacion\`, { method: 'POST' })`.
- `PolizaDetallePage`: `const puedeRenovar = poliza.estado === 'VIGENTE' || poliza.enPeriodoDeGracia;`
  botón "Renovar" (`.boton`), `window.confirm`, en éxito `navigate(\`/polizas/${nueva.id}\`, { state: { mensaje } })`
  y mostrar el mensaje con `Mensaje`; en error `setError(e.message)` sin salir de la pantalla.
- Formato con `formatMoneda` de `utils/format.js` en el mensaje de éxito (no usar las funciones locales).
- `ETIQUETAS_ESTADO.RENOVADA = 'Renovada'`; `.badge-renovada` con color neutro/azul; `RENOVADA` en `ESTADOS`
  de `PolizasPage`.

## Riesgos

| Riesgo | Mitigación |
|--------|------------|
| Agregar `RENOVADA` al enum sin tocar el `CHECK` rompe inserts | `PolizasApplicationTests` + test de integración del flujo lo detecta; checklist del skill `nuevo-endpoint-rest` |
| Cambiar `contarUltimos12Meses` altera otros usos | Búsqueda de referencias antes del cambio (hoy sin usos); test nuevo en `SiniestroServiceTest` |
| Fechas mostradas con un día de diferencia | Usar `formatFecha` de `utils/format.js` en la UI nueva |

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| Se modifica `SiniestroController`/`SiniestroService` (fuera del paquete `poliza`) | FR-013 exige aceptar siniestros sobre pólizas RENOVADA; la regla vive hoy en el controller (deuda previa que viola el principio I) | Agregar `RENOVADA` al `if` del controller perpetuaría lógica de negocio en el controller |
| Sin bloqueo optimista para doble renovación | Entorno H2 de laboratorio, operación manual de baja concurrencia | `@Version` requiere columna nueva y manejo de `OptimisticLockException`; se propone como mejora posterior |
