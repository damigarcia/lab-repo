# Checklist de endpoint nuevo

Marcar cada punto antes de dar el endpoint por terminado.

## Contrato
- [ ] Ruta en plural bajo `/api/...`; acciones como sub-recurso sustantivado (`/{id}/renovacion`).
- [ ] Método HTTP correcto (GET lectura, POST creación/acción).
- [ ] 201 + `Location` al crear; 200 en lecturas y acciones.
- [ ] Errores: 400 validación, 404 inexistente, 422 regla de negocio, todos con `ErrorResponse`.
- [ ] No se renombró ni quitó ningún campo de respuestas existentes.
- [ ] Fechas ISO (`yyyy-MM-dd`), montos `BigDecimal`.

## Capas
- [ ] Request y Response son `record`; el request tiene Bean Validation en cada campo.
- [ ] El controller no tiene lógica de negocio ni usa repositorios.
- [ ] La lógica está en el service, con `@Transactional` si escribe.
- [ ] No se expone ninguna entidad JPA.
- [ ] Se reutilizó lógica existente en lugar de duplicarla.
- [ ] Sin números mágicos; "hoy" vía `Clock`.
- [ ] Códigos de `ReglaNegocioException` en MAYÚSCULAS y mensajes en español.

## Datos
- [ ] Si cambió el modelo: `schema.sql`, `data.sql` y `docs/DATABASE.md` actualizados.
- [ ] Enum nuevo/modificado: enum Java + `CHECK` + tabla de enums del doc.

## Tests
- [ ] Test de service con Mockito: éxito + cada regla que falla.
- [ ] Test de controller con `@WebMvcTest`: 2xx, 400, 404, 422.
- [ ] `cd backend && mvn -q test` en verde.

## Seguridad
- [ ] Sin secretos ni datos reales en código o tests.
- [ ] Sin consultas por concatenación de strings.
- [ ] Mensajes de error sin detalles internos.

## Documentación
- [ ] Endpoint agregado a la tabla de `README.md`.
- [ ] Reglas nuevas documentadas en `docs/REGLAS_NEGOCIO.md` (con id `RN-...`).
