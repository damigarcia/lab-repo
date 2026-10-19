# Notas para el facilitador

> Documento interno del workshop. Describe la deuda técnica **plantada a propósito** en la rama `main`
> para que los participantes (y sus agentes) la descubran. No compartir antes de los ejercicios.

## Imperfecciones deliberadas

| # | Dónde | Qué | Regla que viola | Qué debería detectar el agente |
|---|-------|-----|-----------------|--------------------------------|
| 1 | `backend/.../siniestro/SiniestroController.java` (`registrar`) | El controller inyecta `PolizaRepository` y valida ahí: póliza `VIGENTE`, fecha de ocurrencia dentro de la vigencia y `monto <= suma asegurada`. | CONTRIBUTING §1 (controllers delgados, lógica en service; una feature no usa el repositorio de otra) | Mover las validaciones a `SiniestroService` (o a un método de `PolizaService`) y dejar el controller solo delegando. |
| 2 | `backend/src/test/...` | La feature `siniestro` **no tiene tests** (ni de service ni de controller). | CONTRIBUTING §5 (tests obligatorios) | Al tocar siniestros, el subagente `code-reviewer` debería marcarlo; buen ejercicio para el skill de test. |
| 3 | `backend/.../poliza/PolizaService.java` (`estaEnPeriodoDeGracia`) | Número mágico `30` (días de gracia) hardcodeado. | CONTRIBUTING §1 (constantes con nombre) | Extraer a constante (`DIAS_GRACIA`) o leerlo del catálogo. Relevante en el Lab 2: la renovación usa el mismo plazo de 30 días → "Reuse Before Creation". |
| 4 | `frontend/src/pages/PolizasPage.jsx` y `PolizaDetallePage.jsx` | Funciones `formatearFecha` / `formatearMonto` duplicadas localmente en vez de usar `src/utils/format.js` (que existe y está testeado; `ClientesPage` sí lo usa). | CONTRIBUTING §4 | Reemplazar por `formatFecha` / `formatMoneda`. **Bonus:** la versión duplicada tiene un bug real: `new Date('2025-11-08')` se interpreta en UTC y en Venezuela (UTC-4) muestra **el día anterior**. `utils/format.js` no tiene ese problema. Además el formato de moneda es distinto (`$ 800.00` vs `USD 800,00`). |
| 5 | `backend/.../cliente/ClienteController.java` (`GET /api/clientes/{id}`) | Devuelve la entidad JPA `Cliente` en vez de `ClienteResponse`. Se filtra `fechaRegistro`, un dato interno. | CONTRIBUTING §1, ADR-002 | Devolver `ClienteResponse` vía `ClienteMapper`. Ojo: cambiar el payload es un cambio de contrato (quitar un campo) → buena discusión sobre compatibilidad. El test `ClienteControllerTest.obtener_existente_devuelve200` sigue pasando con el cambio. |

## Otras "trampas" útiles para el Lab 2 (renovación)

- **Discrepancia de reglas:** `SiniestroService.contarUltimos12Meses` cuenta siniestros en **cualquier** estado,
  pero el catálogo corporativo (MCP, `recargoSiniestralidad`) dice que **no cuentan los `RECHAZADO`**.
  Un buen `/speckit.clarify` o un agente que consulta el MCP debería detectarlo.
- **Estado nuevo `RENOVADA`:** exige tocar el enum, el `CHECK` de `schema.sql` y docs/DATABASE.md (lo dice el doc).
  Si el agente solo cambia el enum, `PolizasApplicationTests` falla al insertar (buena señal de que el test de contexto sirve).
- **Datos de prueba ya preparados** en `data.sql` (fechas relativas a hoy):
  - `POL-AUT-0001` VIGENTE con siniestros recientes → renovable con recargo 10 %.
  - `POL-HOG-0002` VIGENTE sin siniestros → renovable sin recargo.
  - `POL-VID-0003` VENCIDA hace 10 días, siniestro de hace ~370 días → renovable sin recargo (borde de la ventana).
  - `POL-AUT-0004` VENCIDA hace 90 días → **no** renovable; su siniestro es `RECHAZADO`.
  - `POL-HOG-0005` CANCELADA → **no** renovable.
  - `POL-VID-0006` VIGENTE, vence en 20 días → renovable.
- **Edad máxima VIDA:** el catálogo define `edadMaximaRenovacion: 70`. Ningún cliente de demo la supera; es un
  edge case que la spec debería mencionar.
- **`numero` de póliza:** `generarNumero` usa `count() + 1`, lo que no es seguro ante concurrencia. No es parte de las
  imperfecciones "oficiales" pero puede salir en la revisión.

## Ramas

- `main`: punto de partida de los participantes.
- `solucion`: harness de referencia (Lab 1) + Spec Kit inicializado con constitution, spec, plan y tasks de la
  renovación (Lab 2). **No** contiene la implementación de la renovación.
