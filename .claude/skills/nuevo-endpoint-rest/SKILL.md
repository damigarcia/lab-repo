---
name: nuevo-endpoint-rest
description: Agrega un endpoint REST nuevo al backend Spring Boot siguiendo las convenciones del equipo (DTO record, lógica en service, controller delgado, test MockMvc). Usar cuando se pida crear o exponer un endpoint, una acción sobre un recurso (p. ej. /cancelacion, /renovacion) o una consulta nueva en /api.
---

# Nuevo endpoint REST

Procedimiento para agregar un endpoint en `backend/` sin romper las convenciones de
`CONTRIBUTING.md` ni los contratos existentes. Seguir los pasos **en orden**.

## 0. Entender antes de escribir
1. Identificar la feature (`poliza`, `cliente`, `siniestro` o una nueva) y leer sus archivos existentes.
2. Si hay spec en `specs/NNN-*/spec.md`, leerla y anotar los FR-xxx que cubre el endpoint.
3. Revisar `docs/REGLAS_NEGOCIO.md` y consultar el MCP `catalogo-coberturas` si el endpoint aplica reglas de negocio.
4. Buscar lógica reutilizable (métodos de service existentes, enums, mappers). **Reusar antes de crear.**
5. Definir el contrato: método HTTP, ruta, request, response, códigos (200/201/400/404/422).

## 1. DTOs
- Request como `record` en el paquete de la feature: `Crear<Recurso>Request` o `<Accion><Recurso>Request`.
- Bean Validation en cada campo de entrada (`@NotNull`, `@NotBlank`, `@Size`, `@DecimalMin`, `@Digits`...).
- Response: reutilizar `<Recurso>Response` si existe. Si hace falta un campo nuevo, **agregarlo** (nunca renombrar ni quitar).
- Si cambia el mapeo, actualizar `<Recurso>Mapper` (método `static`).

## 2. Service
- Método público en `<Recurso>Service` con la lógica completa: búsqueda (`NotFoundException`), reglas
  (`ReglaNegocioException("CODIGO", "mensaje en español")`), persistencia.
- `@Transactional` si escribe. Usar `LocalDate.now(clock)`. Constantes con nombre, sin números mágicos.
- Test unitario en `src/test/java/.../<feature>/<Recurso>ServiceTest.java` con Mockito: camino feliz y cada regla que falla.

## 3. Controller
- Método en `<Recurso>Controller`: `@Valid @RequestBody`, una sola línea de delegación al service.
- Creación → `ResponseEntity.created(URI.create(...)).body(...)` (201 + `Location`). Acción → 200 con el response.
- Nada de `if` de negocio, repositorios ni `try/catch`.

## 4. Test de controller (MockMvc)
- Partir de la plantilla [assets/ControllerTestTemplate.java.txt](assets/ControllerTestTemplate.java.txt).
- Cubrir como mínimo: éxito (200/201), validación (400 con `codigo = VALIDACION`), no encontrado (404),
  regla de negocio (422 con el `codigo` esperado).

## 5. Modelo de datos (solo si aplica)
- Si cambia una entidad o enum: actualizar `schema.sql` (incluido el `CHECK` de enums), `data.sql` si hace falta,
  y `docs/DATABASE.md`. `PolizasApplicationTests` valida que el esquema y las entidades coincidan.

## 6. Verificar
```bash
cd backend && mvn -q test
```
- Si falla, corregir y volver a correr. No cerrar la tarea con tests en rojo.
- Recorrer [references/checklist-endpoint.md](references/checklist-endpoint.md) y reportar el resultado.
- Si el endpoint se consume desde el frontend, agregar la función en `frontend/src/api/client.js`.
