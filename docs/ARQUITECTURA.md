# Arquitectura

## Vista general

```
┌──────────────┐   /api (proxy Vite)   ┌──────────────────────────────┐     ┌──────────┐
│  React SPA   │ ────────────────────▶ │  Spring Boot (polizas-api)   │ ──▶ │ H2 (mem) │
│  :5173       │ ◀──────── JSON ────── │  :8080                       │     └──────────┘
└──────────────┘                       └──────────────────────────────┘
```

Un único backend monolítico organizado **por feature** (`poliza`, `cliente`, `siniestro`) y un
paquete `common` para lo transversal. El frontend es una SPA que solo habla con `/api`.

## Capas del backend

```
Controller  →  Service  →  Repository  →  BD
    │             │
    │             └── lanza NotFoundException / ReglaNegocioException
    └── recibe Crear*Request (@Valid) y devuelve *Response
              ▲
              └── *Mapper (estático) convierte entidad → DTO
```

## Flujo de una request

Ejemplo `POST /api/polizas/{id}/cancelacion`:

1. `PolizaController.cancelar` recibe `CancelarPolizaRequest`; `@Valid` valida que `motivo` no esté vacío.
   Si falla, `GlobalExceptionHandler` responde **400** con `codigo = VALIDACION`.
2. Delega en `PolizaService.cancelar(id, request)` (transaccional).
3. El service busca la póliza (`NotFoundException` → **404**), verifica la regla
   "solo VIGENTE" (`ReglaNegocioException` → **422**) y cambia el estado en la entidad.
4. Al cerrar la transacción, JPA persiste el cambio (dirty checking).
5. El service devuelve `PolizaResponse` vía `PolizaMapper`; el controller responde **200**.

## Formato de errores

Todas las respuestas de error usan `common/ErrorResponse`:

```json
{
  "timestamp": "2025-06-15T10:00:00-04:00",
  "status": 422,
  "codigo": "ESTADO_INVALIDO",
  "mensaje": "Solo se pueden cancelar pólizas en estado VIGENTE (estado actual: VENCIDA)",
  "detalles": []
}
```

| Excepción | HTTP | `codigo` |
|-----------|------|----------|
| `MethodArgumentNotValidException` | 400 | `VALIDACION` (con `detalles` por campo) |
| JSON inválido / parámetro mal tipado | 400 | `SOLICITUD_INVALIDA` |
| `NotFoundException` | 404 | `NO_ENCONTRADO` |
| `ReglaNegocioException` | 422 | el código de la regla (p. ej. `POLIZA_NO_VIGENTE`) |
| Cualquier otra | 500 | `ERROR_INTERNO` (se loguea, no se expone el detalle) |

## Frontend

```
src/
  main.jsx           monta <App/> dentro de BrowserRouter
  App.jsx            layout + rutas (/polizas, /polizas/:id, /clientes)
  api/client.js      fetch centralizado; lanza ApiError con codigo/mensaje del backend
  utils/format.js    formatFecha, formatMoneda, formatEstado
  components/        EstadoBadge, Mensaje
  pages/             PolizasPage, PolizaDetallePage, ClientesPage
```

## Decisiones (ADR cortos)

### ADR-001 · Organización por feature
**Contexto:** el equipo mantiene varios dominios pequeños. **Decisión:** un paquete por feature con todas sus capas
adentro. **Consecuencia:** un cambio de negocio toca un solo paquete; las dependencias entre features se
hacen a través del *service* de la otra feature, no de su repositorio.

### ADR-002 · DTOs como `record` y mappers estáticos
**Contexto:** exponer entidades JPA filtra columnas internas y acopla el contrato al esquema.
**Decisión:** entrada `Crear*Request` / salida `*Response` como `record`; conversión en `*Mapper` con métodos
estáticos, sin librerías de mapeo. **Consecuencia:** más código explícito, contratos estables.

### ADR-003 · Errores centralizados
**Decisión:** las capas lanzan excepciones de dominio; solo `GlobalExceptionHandler` arma respuestas de error.
**Consecuencia:** formato homogéneo y controllers sin `try/catch`.

### ADR-004 · H2 en memoria con `schema.sql` explícito
**Contexto:** entorno de laboratorio sin infraestructura. **Decisión:** H2 en memoria; el esquema se versiona en
`schema.sql` y Hibernate solo **valida** (`ddl-auto=validate`). **Consecuencia:** todo cambio de modelo exige
actualizar `schema.sql` (y `data.sql` si aplica) y docs/DATABASE.md. En producción se usaría una herramienta de
migraciones (Flyway), fuera del alcance de este repo.

### ADR-005 · Reloj inyectable
**Decisión:** "hoy" se obtiene de un bean `java.time.Clock`. **Consecuencia:** reglas con fechas (gracia,
vigencias, siniestralidad) testeables con `Clock.fixed`.

### ADR-006 · Proxy de Vite en desarrollo
**Decisión:** el frontend llama a rutas relativas `/api`; en desarrollo Vite hace proxy a `:8080`. CORS
para `http://localhost:5173` queda habilitado como respaldo. **Consecuencia:** el frontend no conoce URLs absolutas.
