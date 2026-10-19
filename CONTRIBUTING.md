# Guía de contribución

Estas son las convenciones que el equipo aplica en revisión de código. Si un cambio no las
cumple, el PR vuelve.

## 1. Organización del backend

El código se organiza **por feature**, no por capa técnica:

```
com.aseguradorademo.polizas.<feature>/
    <Entidad>.java                 entidad JPA
    <Entidad>Repository.java       Spring Data JPA
    <Entidad>Service.java          lógica de negocio y transacciones
    <Entidad>Controller.java       endpoints REST
    <Entidad>Mapper.java           conversión entidad -> DTO (métodos estáticos)
    <Entidad>Response.java         DTO de salida (record)
    Crear<Entidad>Request.java     DTO de entrada (record con Bean Validation)
    <Enum>.java                    enums del dominio
```

Lo transversal va en `common/` (errores, configuración).

### Responsabilidades por capa

| Capa | Hace | No hace |
|------|------|---------|
| Controller | Mapear HTTP ↔ DTO, `@Valid`, códigos de estado, `Location` en 201 | Reglas de negocio, acceso a repositorios, `try/catch` para armar errores |
| Service | Reglas de negocio, orquestación, `@Transactional`, lanzar excepciones de dominio | Conocer HTTP (`ResponseEntity`, `HttpStatus`) |
| Repository | Consultas | Lógica |
| Mapper | Entidad → DTO | Acceder a repositorios |

- Los controllers **nunca** devuelven entidades JPA: siempre un `XResponse`.
- Los services de lectura llevan `@Transactional(readOnly = true)` a nivel de clase; los métodos que escriben, `@Transactional`.
- Los errores se expresan con excepciones (`NotFoundException`, `ReglaNegocioException`) y los traduce `GlobalExceptionHandler`.
- "Hoy" se obtiene del bean `Clock` inyectado (`LocalDate.now(clock)`), nunca `LocalDate.now()` directo.
- Las constantes de negocio (tasas, plazos, límites) van con nombre (`private static final` o en el enum correspondiente), no como números sueltos.
- Inyección por constructor. Sin `@Autowired` en campos.

## 2. Nombres

- Código, endpoints, DTOs y mensajes **en español** (dominio del negocio): `Poliza`, `primaAnual`, `/api/polizas`.
- Sin tildes ni ñ en identificadores (`Poliza`, no `Póliza`). Los mensajes al usuario sí llevan tildes.
- Endpoints en plural y kebab-case si hace falta. Acciones que cambian estado como sub-recurso sustantivado:
  `POST /api/polizas/{id}/cancelacion`.
- Columnas de base de datos en `snake_case`; propiedades Java en `camelCase` (ver [docs/DATABASE.md](docs/DATABASE.md)).
- Tests: `metodo_escenario_resultadoEsperado` (por ejemplo `cancelar_polizaVencida_lanzaReglaNegocio`).

## 3. API

- JSON con fechas ISO-8601 (`yyyy-MM-dd`) y montos como número decimal.
- No se rompen contratos existentes: se pueden **agregar** campos a una respuesta, pero no renombrar ni quitar.
- Códigos: 200 lectura/acción, 201 creación (con `Location`), 400 validación, 404 inexistente, 422 regla de negocio.

## 4. Frontend

- Todas las llamadas HTTP pasan por `src/api/client.js` (no usar `fetch` directo en componentes).
- Formato de fechas, montos y estados con `src/utils/format.js`. No crear funciones de formato locales.
- Componentes funcionales con hooks. Un componente por archivo, nombre en PascalCase y extensión `.jsx`.
- Pantallas en `src/pages/`, piezas reutilizables en `src/components/`.
- Sin librerías de UI: CSS en `src/styles.css` con clases simples.

## 5. Tests

- **Todo cambio en backend lleva tests.** Como mínimo:
  - test unitario del service con Mockito (`@ExtendWith(MockitoExtension.class)`), incluyendo los casos de error;
  - test del controller con `@WebMvcTest` + `MockMvc` (camino feliz, 400 por validación, 404/422 según corresponda).
- Fijar la fecha con `Clock.fixed(...)` en tests que dependan de "hoy".
- Utilidades del frontend con Vitest (`*.test.js` junto al archivo).
- Antes de abrir un PR: `mvn test` y `npm test && npm run build` en verde.

## 6. Commits y PRs

- Mensajes en español, en imperativo y con prefijo de tipo:
  `feat: agrega renovación de pólizas`, `fix: corrige cálculo de prima mínima`, `test: ...`, `docs: ...`, `refactor: ...`.
- Un cambio lógico por commit. PRs chicos y con descripción de qué y por qué.
- Ramas: `feature/<descripcion-corta>`, `fix/<descripcion-corta>`.

## 7. Seguridad y datos

- Nunca commitear secretos, tokens ni credenciales (la base H2 de desarrollo no tiene clave a propósito).
- Nunca usar datos reales de clientes, ni siquiera en tests o capturas. Usar los datos ficticios de `data.sql`.
- Validar toda entrada externa con Bean Validation en los DTOs de request.
