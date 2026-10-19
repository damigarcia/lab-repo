# Aseguradora Demo · Gestión de pólizas

Aplicación interna (ficticia) para consultar y gestionar **pólizas**, **clientes** y **siniestros**
de *Aseguradora Demo*. Es el repositorio de laboratorio del workshop de **Desarrollo Agéntico**
(Cursor + GitHub Spec Kit): una app "brownfield" pequeña, con convenciones reales y alguna
deuda técnica, sobre la cual se trabaja con agentes.

> Todos los datos son ficticios. No cargar datos reales de clientes en este repositorio.

## Stack

| Capa | Tecnología |
|------|------------|
| Backend | Java 17, Spring Boot 3.3, Spring Data JPA, Bean Validation, H2 en memoria |
| Frontend | React 18 + Vite (JavaScript), React Router, CSS simple |
| Tests | JUnit 5, Mockito, MockMvc · Vitest |
| Conocimiento corporativo | Servidor MCP local `mcp/catalogo-coberturas` (Node) |

## Requisitos

- Java 17 o superior (compatible con 21) y Maven 3.9+
- Node 20 o superior y npm 10+

## Cómo correr

### Backend (puerto 8080)

```bash
cd backend
mvn spring-boot:run
```

- API: http://localhost:8080/api/polizas
- Consola H2: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:polizasdb`, usuario `sa`, sin clave)
- La base se crea al iniciar desde `schema.sql` y se carga con `data.sql` (se pierde al detener).

### Frontend (puerto 5173)

```bash
cd frontend
npm install
npm run dev
```

Abrir http://localhost:5173. Vite redirige `/api` al backend en `http://localhost:8080`.

### Servidor MCP de coberturas

```bash
cd mcp/catalogo-coberturas
npm install
npm start
```

Ver [mcp/catalogo-coberturas/README.md](mcp/catalogo-coberturas/README.md).

## Tests

```bash
cd backend && mvn test          # unitarios + MockMvc + carga de contexto
cd frontend && npm test         # Vitest
cd frontend && npm run build    # build de producción
```

## Endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/polizas?estado=&tipo=` | Lista pólizas (filtros opcionales) |
| GET | `/api/polizas/{id}` | Detalle de una póliza |
| POST | `/api/polizas` | Emite una póliza nueva |
| POST | `/api/polizas/{id}/cancelacion` | Cancela una póliza vigente |
| GET | `/api/clientes` | Lista clientes |
| GET | `/api/clientes/{id}` | Detalle de un cliente |
| GET | `/api/siniestros?polizaId=` | Siniestros de una póliza |
| POST | `/api/siniestros` | Registra un siniestro |

Los errores siempre se devuelven con el formato `ErrorResponse` (ver [docs/ARQUITECTURA.md](docs/ARQUITECTURA.md)).

## Estructura

```
.
├── backend/                     API Spring Boot
│   └── src/main/java/com/aseguradorademo/polizas/
│       ├── poliza/              feature pólizas (controller, service, repository, entidad, DTOs, mapper)
│       ├── cliente/             feature clientes
│       ├── siniestro/           feature siniestros
│       └── common/              manejo global de errores, configuración web
├── frontend/                    SPA React + Vite
│   └── src/
│       ├── api/client.js        cliente HTTP centralizado
│       ├── utils/format.js      formato de fechas, montos y estados
│       ├── components/          componentes reutilizables
│       └── pages/               pantallas
├── mcp/catalogo-coberturas/     servidor MCP con coberturas y reglas de suscripción
└── docs/
    ├── ARQUITECTURA.md          capas, flujo de una request, decisiones (ADR)
    ├── DATABASE.md              tablas, columnas, enums y reglas de datos
    └── REGLAS_NEGOCIO.md        estados de póliza, prima, siniestros
```

## Documentación

- [CONTRIBUTING.md](CONTRIBUTING.md): convenciones del equipo (capas, nombres, tests, commits).
- [docs/ARQUITECTURA.md](docs/ARQUITECTURA.md)
- [docs/DATABASE.md](docs/DATABASE.md)
- [docs/REGLAS_NEGOCIO.md](docs/REGLAS_NEGOCIO.md)
