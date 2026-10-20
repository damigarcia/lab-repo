# Implementation Plan: Renovación de póliza

**Branch**: `001-renovacion-poliza` | **Date**: 2026-10-20 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/001-renovacion-poliza/spec.md`

**Decisión del equipo (prompt del plan)**: la regla de recargo por siniestralidad se consulta en el MCP
`catalogo-coberturas` al diseñar y queda como configuración del backend; el backend no llama al MCP en runtime.
Todo lo demás (stack, convenciones, componentes a reutilizar) sale de la constitution, `AGENTS.md`, las rules y `docs/`.

## Summary

La renovación se implementa dentro de la feature `poliza` como dos operaciones sobre el sub-recurso
`/api/polizas/{id}/renovacion`: una **vista previa** (`GET`, no escribe nada) y una **confirmación** (`POST`, recibe la
prima que vio el operador y falla si ya no coincide). La lógica vive en `PolizaService` y reutiliza `calcularPrima`,
`estaEnPeriodoDeGracia`, `generarNumero` y `PolizaMapper`. La siniestralidad se pide a `SiniestroService`.

La póliza original **no cambia de estado** (FR-011): se agrega una marca de renovación (`renovada_por_id`) y la nueva
guarda su origen (`poliza_origen_id`, con `UNIQUE` para garantizar una sola renovación). No se agregan valores al enum
`EstadoPoliza`, así que listados, filtros y registro de siniestros no cambian (FR-012). Sin dependencias nuevas.

## Technical Context

**Language/Version**: Java 17 (compatible con 21) · JavaScript (ES2022) con React 18

**Primary Dependencies**: Spring Boot 3.3.x (web, data-jpa, validation) · Vite 6, React Router · **sin dependencias nuevas**

**Storage**: H2 en memoria; esquema en `backend/src/main/resources/schema.sql`, datos en `data.sql`; Hibernate `ddl-auto=validate`

**Testing**: JUnit 5 + Mockito (services), `@WebMvcTest` + MockMvc (controllers), `PolizasApplicationTests` (esquema), Vitest (utils frontend)

**Target Platform**: Servidor Linux/Windows con JVM 17+; navegadores modernos

**Project Type**: Aplicación web (backend REST + SPA)

**Performance Goals**: Vista previa y confirmación < 300 ms p95 en entorno local

**Constraints**:
- Código backend nuevo en `com.aseguradorademo.polizas.poliza`; en `siniestro` solo se ajusta el conteo de siniestralidad.
- Reutilizar `PolizaService` (no crear un `RenovacionService`).
- Cambios de modelo en `schema.sql` (no hay Flyway), reflejados en la entidad y en `docs/DATABASE.md`.
- Solo cambios aditivos de contrato: endpoints nuevos y dos campos nuevos al final de `PolizaResponse`.
- Constantes con nombre para 10 % y 12 meses (valores del catálogo v2025.1); "hoy" vía `Clock`.

**Scale/Scope**: 2 endpoints nuevos, ~7 archivos backend, 3 archivos frontend + 1 util nuevo, 3 docs

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principio | Cómo lo cumple el plan | Estado |
|-----------|------------------------|--------|
| I. Arquitectura por feature y capas | Endpoints en `PolizaController` (solo delegan); reglas en `PolizaService`; DTOs `record` nuevos (`RenovacionPreviewResponse`, `ConfirmarRenovacionRequest`); errores con `ReglaNegocioException`. La siniestralidad se pide a `SiniestroService` (service, no repository). | PASA |
| II. Reutilizar antes de crear | Se reutilizan `calcularPrima` (FR-008), `estaEnPeriodoDeGracia` (la ventana de 30 días de FR-002 es el período de gracia RN-POL-06), `generarNumero`, `buscarPoliza`, `PolizaMapper`, `SiniestroService.contarUltimos12Meses`, y en el frontend `api/client.js`, `utils/format.js`, `Mensaje` y `EstadoBadge`. | PASA |
| III. Contratos compatibles | No se agrega valor al enum de estado ni se cambia ningún campo existente. Solo se agregan `GET`/`POST /{id}/renovacion` y los campos `polizaOrigenId` y `renovadaPorId` (nulos en las pólizas existentes) al final de `PolizaResponse`. | PASA |
| IV. Tests obligatorios | Tests de service por regla, de controller (200/201/404/422/400), de contexto (esquema) y Vitest del util de elegibilidad. | PASA |
| V. Reglas trazables | Valores del MCP `catalogo-coberturas` v2025.1 como constantes; RN-POL-10 a RN-POL-13 en `docs/REGLAS_NEGOCIO.md` y ajuste de RN-SIN-06. | PASA |
| VI. Seguridad y datos | El único input es la prima confirmada (Bean Validation); sin datos reales; errores vía `GlobalExceptionHandler`. | PASA |
| VII. Simplicidad | Sin dependencias, sin servicio ni tabla nuevos; dos columnas. | PASA |

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
│   ├── Poliza.java                       # + polizaOrigenId, + renovadaPorId, + marcarRenovadaPor(nueva), + estaRenovada()
│   ├── PolizaResponse.java               # + polizaOrigenId, + renovadaPorId (al final del record)
│   ├── PolizaMapper.java                 # mapea los dos campos nuevos
│   ├── RenovacionPreviewResponse.java    # nuevo record (vista previa)
│   ├── ConfirmarRenovacionRequest.java   # nuevo record (prima confirmada)
│   ├── PolizaService.java                # + previsualizarRenovacion(id), + renovar(id, request); constantes RECARGO_SINIESTRALIDAD, DIAS_GRACIA
│   └── PolizaController.java             # + GET y POST /{id}/renovacion
└── siniestro/
    ├── SiniestroRepository.java          # + countByPolizaIdAndFechaOcurrenciaGreaterThanEqualAndEstadoNot
    └── SiniestroService.java             # contarUltimos12Meses: ventana inclusiva y excluye RECHAZADO

backend/src/main/resources/
└── schema.sql                            # columnas poliza_origen_id (UNIQUE) y renovada_por_id + FKs

backend/src/test/java/com/aseguradorademo/polizas/
├── poliza/PolizaServiceTest.java         # + tests de vista previa y renovación
├── poliza/PolizaControllerTest.java      # + tests de los endpoints; el helper del record pasa null a los campos nuevos
└── siniestro/SiniestroServiceTest.java   # nuevo: ventana y exclusión de RECHAZADO

frontend/src/
├── api/client.js                         # + previsualizarRenovacion(id), + renovarPoliza(id, primaAnual)
├── utils/renovacion.js                   # nuevo: puedeRenovar(poliza)
├── utils/renovacion.test.js              # nuevo
└── pages/PolizaDetallePage.jsx           # + botón Renovar, panel de vista previa, vínculos origen/renovada

docs/
├── REGLAS_NEGOCIO.md                     # RN-POL-10..13, ajuste RN-SIN-06
└── DATABASE.md                           # columnas poliza_origen_id y renovada_por_id
```

**Structure Decision**: Aplicación web existente (`backend/` + `frontend/`). La feature vive en `poliza`, sin paquetes nuevos.

## Diseño

### Por qué una marca y no un estado nuevo

La spec pide que la original **conserve su estado** (FR-011) y que lo existente no cambie de significado (FR-012).
Un estado `RENOVADA` haría que una póliza vigente deje de aparecer en `?estado=VIGENTE`, rechazaría siniestros sobre
una póliza que todavía cubre (RN-SIN-01) y obligaría a tocar el enum, el `CHECK`, el filtro y las etiquetas del
frontend. Con una marca, nada de eso cambia.

### Contrato

```
GET /api/polizas/{id}/renovacion            vista previa, no escribe

200 OK
Body: RenovacionPreviewResponse
  { polizaId, clienteId, clienteNombre, tipo, sumaAsegurada,
    primaAnual, recargoAplicado, fechaInicio, fechaFin }

POST /api/polizas/{id}/renovacion           confirmación
Body: ConfirmarRenovacionRequest { primaAnual }   (@NotNull @Positive)

201 Created
Location: /api/polizas/{nuevoId}
Body: PolizaResponse de la póliza nueva

Errores (ambas operaciones, formato ErrorResponse):
400 VALIDACION / SOLICITUD_INVALIDA   body inválido o id no numérico
404 NO_ENCONTRADO                     póliza inexistente
422 POLIZA_YA_RENOVADA                ya tiene renovada_por_id (prevalece sobre los demás)
422 POLIZA_NO_RENOVABLE               CANCELADA
422 PLAZO_RENOVACION_VENCIDO          VENCIDA hace más de 30 días
422 PRIMA_DESACTUALIZADA              solo POST: la prima recalculada no es la confirmada (FR-010)
```

`PolizaResponse` agrega `Long polizaOrigenId` y `Long renovadaPorId` **al final** del record. En las pólizas
existentes valen `null`: ningún campo actual cambia (FR-012, SC-005).

### Modelo de datos

```sql
-- schema.sql (tabla polizas)
poliza_origen_id  BIGINT NULL,
renovada_por_id   BIGINT NULL,
CONSTRAINT uk_polizas_poliza_origen  UNIQUE (poliza_origen_id),
CONSTRAINT fk_polizas_poliza_origen  FOREIGN KEY (poliza_origen_id) REFERENCES polizas (id),
CONSTRAINT fk_polizas_renovada_por   FOREIGN KEY (renovada_por_id)  REFERENCES polizas (id)
```

Entidad: dos `@Column Long` simples (sin `@OneToOne`, para no agregar cargas perezosas).
`ck_polizas_estado` **no cambia**.

### Lógica en `PolizaService`

Un método privado `calcularRenovacion(Poliza)` arma la vista previa y lo usan las dos operaciones:

1. `buscarPoliza(id)` → 404 si no existe.
2. Elegibilidad (FR-001 a FR-003), en este orden:
   - `estaRenovada()` → `POLIZA_YA_RENOVADA`;
   - `CANCELADA` → `POLIZA_NO_RENOVABLE`;
   - `VENCIDA` y no `estaEnPeriodoDeGracia(p)` → `PLAZO_RENOVACION_VENCIDO`;
   - `VIGENTE` es renovable sin mirar fechas.
3. Vigencia (FR-007): `inicio = fechaFin original`, `fin = inicio.plusYears(1)`. No se aplica RN-POL-03
   (fecha de inicio no anterior a hoy), que es solo para emisión.
4. Prima (FR-008, FR-009): `base = calcularPrima(tipo, sumaAsegurada)` (incluye mínimo y redondeo);
   si `siniestroService.contarUltimos12Meses(id) > 0` → `base × (1 + RECARGO_SINIESTRALIDAD)`, `setScale(2, HALF_UP)`.

`previsualizarRenovacion(id)` devuelve ese cálculo como `RenovacionPreviewResponse`.

`renovar(id, request)` (`@Transactional`):

1. Recalcula con `calcularRenovacion`. Si la elegibilidad falla, se lanza el mismo error.
2. Si `primaAnual` recalculada `compareTo` la confirmada ≠ 0 → `PRIMA_DESACTUALIZADA` (FR-010).
3. Crea la nueva con `generarNumero(tipo)`, `polizaOrigenId = id`; `saveAndFlush`.
4. `original.marcarRenovadaPor(nueva.getId())`. Persiste por dirty checking en la misma transacción.
5. Devuelve `PolizaMapper.toResponse(nueva, false)`.

**Doble confirmación concurrente** (edge case): la restricción `UNIQUE (poliza_origen_id)` impide la segunda
inserción. `renovar` captura `DataIntegrityViolationException` del `saveAndFlush` y lanza
`ReglaNegocioException("POLIZA_YA_RENOVADA", ...)`, así el manejador global devuelve 422 y no 500.

### Siniestralidad

`SiniestroService.contarUltimos12Meses` (hoy sin otros usos, verificado con búsqueda) pasa a
`countByPolizaIdAndFechaOcurrenciaGreaterThanEqualAndEstadoNot(polizaId, hoy.minusMonths(12), RECHAZADO)`:
ventana inclusiva del día límite y sin rechazados, como piden la spec y el catálogo. Se actualiza RN-SIN-06.
`registrar` y `SiniestroController` no se tocan: la deuda plantada en el controller queda fuera de alcance.

### Frontend

- `api.previsualizarRenovacion(id)` → `request(\`/polizas/${id}/renovacion\`)`.
- `api.renovarPoliza(id, primaAnual)` → `POST` con `{ primaAnual }`.
- `utils/renovacion.js`: `puedeRenovar(poliza)` = no tiene `renovadaPorId` y (`estado === 'VIGENTE'` o `enPeriodoDeGracia`).
- `PolizaDetallePage`:
  - botón **Renovar** si `puedeRenovar`; al pulsarlo carga la vista previa y muestra un panel con cliente, tipo,
    suma, prima (con aviso "Incluye recargo del 10 % por siniestralidad" si `recargoAplicado`) y vigencia, con
    **Confirmar** y **Volver**;
  - al confirmar: `navigate(\`/polizas/${nueva.id}\`)` con mensaje de éxito; ante error, `Mensaje` con el texto de la
    API (si es `PRIMA_DESACTUALIZADA`, se vuelve a pedir la vista previa);
  - si `renovadaPorId`: "Renovada por" + `Link` a la nueva; si `polizaOrigenId`: "Renovación de" + `Link`;
  - montos y fechas nuevos con `formatMoneda` / `formatFecha` de `utils/format.js`, no con las funciones locales.

## Riesgos

| Riesgo | Mitigación |
|--------|------------|
| Columnas nuevas sin reflejar en la entidad → falla `ddl-auto=validate` | `PolizasApplicationTests` lo detecta en la Fase 2 |
| Cambiar `contarUltimos12Meses` altera otros usos | Búsqueda de referencias antes del cambio (hoy sin usos) + `SiniestroServiceTest` |
| Una póliza vigente ya renovada todavía se puede cancelar | Lo permite la regla actual (spec: "cancelar sigue las reglas de hoy"); se deja anotado para negocio |
| `generarNumero` usa `count() + 1` (no es seguro con concurrencia) | Deuda previa, fuera de alcance; el `UNIQUE` de `numero` evita duplicados |

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

Sin violaciones.
