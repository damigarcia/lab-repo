# AGENTS.md

App interna de pólizas de **Aseguradora Demo** (datos ficticios). Backend Java/Spring Boot + frontend React/Vite.
Todo el código, mensajes y docs van en **español**.

## Comandos

| Qué | Comando |
|-----|---------|
| Backend: correr (puerto 8080) | `cd backend && mvn spring-boot:run` |
| Backend: tests | `cd backend && mvn -q test` |
| Frontend: instalar | `cd frontend && npm install` |
| Frontend: correr (puerto 5173, proxy `/api` → 8080) | `cd frontend && npm run dev` |
| Frontend: tests + build | `cd frontend && npm test && npm run build` |
| MCP de coberturas | `cd mcp/catalogo-coberturas && npm install && npm start` |

Un cambio está terminado solo si `mvn -q test`, `npm test` y `npm run build` pasan.

## Estructura

- `backend/src/main/java/com/aseguradorademo/polizas/<feature>/` — por feature: `poliza`, `cliente`, `siniestro`; transversal en `common/`.
- `backend/src/main/resources/schema.sql` y `data.sql` — esquema y datos de demo (Hibernate solo valida).
- `frontend/src/` — `api/client.js` (HTTP), `utils/format.js` (formato), `components/`, `pages/`.
- `mcp/catalogo-coberturas/` — servidor MCP con el catálogo y reglas corporativas (configurado en `.cursor/mcp.json`).
- `specs/NNN-nombre/` — especificaciones Spec Kit por feature (`spec.md`, `plan.md`, `tasks.md`).

## Dónde está el conocimiento

- Convenciones del equipo: `CONTRIBUTING.md` (léelo antes de escribir código).
- Arquitectura y ADRs: `docs/ARQUITECTURA.md`.
- Tablas, columnas y enums: `docs/DATABASE.md`.
- Reglas de negocio: `docs/REGLAS_NEGOCIO.md`. Reglas corporativas de suscripción/renovación: tools del MCP
  `catalogo-coberturas` (`consultar_regla_suscripcion`, `listar_coberturas`). Si difieren, manda el MCP.
- Principios no negociables: `.specify/memory/constitution.md` (prevalece sobre todo lo demás).
- Reglas para el agente: `.cursor/rules/`. Skills: `.cursor/skills/` (incluye `speckit-*`). Subagentes: `.cursor/agents/`.

## Límites

- No usar datos reales de clientes ni agregar secretos.
- No cambiar contratos de API existentes (solo agregar campos/endpoints).
- No agregar dependencias sin justificarlo en el plan.
