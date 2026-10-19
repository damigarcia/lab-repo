# MCP `catalogo-coberturas`

Servidor MCP local (transporte **stdio**) que expone el "conocimiento corporativo" de
Aseguradora Demo: el catálogo de coberturas y las reglas de suscripción y renovación.
Los datos son ficticios y viven en [`data/catalogo.json`](data/catalogo.json).

## Herramientas

| Tool | Parámetros | Devuelve |
|------|------------|----------|
| `listar_coberturas` | `tipo`: `AUTO` \| `HOGAR` \| `VIDA` | Coberturas del tipo (obligatorias, límites, deducibles) |
| `consultar_regla_suscripcion` | `tipo`: `AUTO` \| `HOGAR` \| `VIDA` | Reglas del tipo + reglas generales de renovación y recargo por siniestralidad |

## Cómo correrlo

```bash
cd mcp/catalogo-coberturas
npm install
npm start          # queda esperando mensajes JSON-RPC por stdin
```

Normalmente no se ejecuta a mano: lo lanza el cliente MCP (Cursor) según la configuración
de `.cursor/mcp.json`. Requiere Node 18 o superior.

## Prueba rápida sin cliente

```bash
printf '%s\n' \
  '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-06-18","capabilities":{},"clientInfo":{"name":"prueba","version":"0"}}}' \
  '{"jsonrpc":"2.0","method":"notifications/initialized"}' \
  '{"jsonrpc":"2.0","id":2,"method":"tools/call","params":{"name":"consultar_regla_suscripcion","arguments":{"tipo":"VIDA"}}}' \
  | node index.js
```

También se puede inspeccionar con `npx @modelcontextprotocol/inspector node index.js`.

## Nota

Con transporte stdio, **stdout está reservado para el protocolo**. Los logs van a stderr.
