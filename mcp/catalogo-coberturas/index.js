#!/usr/bin/env node
// Servidor MCP "catalogo-coberturas" (transporte stdio).
// Expone el conocimiento corporativo de Aseguradora Demo: coberturas por tipo de póliza
// y reglas de suscripción/renovación. Los datos viven en data/catalogo.json.
//
// IMPORTANTE: con transporte stdio, stdout está reservado para JSON-RPC.
// Cualquier log debe ir a stderr (console.error).

import { readFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { McpServer } from '@modelcontextprotocol/sdk/server/mcp.js';
import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js';
import { z } from 'zod';

const __dirname = dirname(fileURLToPath(import.meta.url));
const catalogo = JSON.parse(readFileSync(join(__dirname, 'data', 'catalogo.json'), 'utf8'));

const TIPOS = ['AUTO', 'HOGAR', 'VIDA'];
const tipoSchema = z.enum(TIPOS).describe('Tipo de póliza: AUTO, HOGAR o VIDA');

const server = new McpServer({
  name: 'catalogo-coberturas',
  version: '0.1.0',
});

function respuestaJson(datos) {
  return { content: [{ type: 'text', text: JSON.stringify(datos, null, 2) }] };
}

server.registerTool(
  'listar_coberturas',
  {
    title: 'Listar coberturas',
    description:
      'Devuelve el catálogo oficial de coberturas de Aseguradora Demo para un tipo de póliza (AUTO, HOGAR, VIDA), ' +
      'indicando cuáles son obligatorias, límites y deducibles.',
    inputSchema: { tipo: tipoSchema },
  },
  async ({ tipo }) =>
    respuestaJson({
      tipo,
      moneda: catalogo.moneda,
      versionCatalogo: catalogo.version,
      coberturas: catalogo.coberturas[tipo],
    }),
);

server.registerTool(
  'consultar_regla_suscripcion',
  {
    title: 'Consultar reglas de suscripción',
    description:
      'Devuelve las reglas de suscripción vigentes para un tipo de póliza (sumas aseguradas, edades, observaciones) ' +
      'junto con las reglas generales de renovación y recargo por siniestralidad. ' +
      'Usar esta herramienta antes de implementar lógica de negocio sobre emisión o renovación de pólizas.',
    inputSchema: { tipo: tipoSchema },
  },
  async ({ tipo }) =>
    respuestaJson({
      tipo,
      versionCatalogo: catalogo.version,
      reglasDelTipo: catalogo.reglasSuscripcion[tipo],
      renovacion: catalogo.reglasGenerales.renovacion,
      recargoSiniestralidad: catalogo.reglasGenerales.recargoSiniestralidad,
    }),
);

const transport = new StdioServerTransport();
await server.connect(transport);
console.error('[catalogo-coberturas] servidor MCP iniciado (stdio)');
