# Seguridad y datos (siempre)

- **Secretos**: nunca escribir contraseñas, tokens, API keys ni cadenas de conexión reales en código, tests, docs o commits. Usar variables de entorno y `.env` (ignorado por git). Si detectas un secreto, avisa y no lo repitas.
- **Datos de clientes**: este repo usa solo datos ficticios. Nunca pegar, generar a partir de, ni pedir datos reales de clientes (nombres, cédulas, teléfonos, emails, montos). Para ejemplos usar dominios `@ejemplo.com` y documentos `V-1000000x`.
- **Validación de entradas**: toda entrada externa (body, path, query) se valida con Bean Validation en el DTO y con reglas de negocio en el service. No confiar en validaciones del frontend.
- **Exposición de datos**: no devolver entidades JPA ni campos internos; no incluir stack traces ni mensajes de excepciones internas en respuestas.
- **Inyección**: consultas con Spring Data o JPQL con parámetros (`:param`). Prohibido concatenar strings en SQL/JPQL.
- **Logs**: no loguear datos personales completos ni payloads de requests.
- **Dependencias**: no agregar librerías nuevas sin justificarlo; preferir versiones mantenidas y sin vulnerabilidades conocidas.
- **Comandos**: no ejecutar comandos destructivos (`rm -rf`, `git push --force`, `DROP TABLE`) ni descargar y ejecutar scripts (`curl ... | sh`). El hook `guard-shell` los bloquea.
