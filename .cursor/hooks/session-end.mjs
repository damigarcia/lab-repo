#!/usr/bin/env node
// Hook stop: registra el fin de cada ejecución del agente en .cursor/audit/sesiones.jsonl.
// Entrada (stdin): { conversation_id, generation_id, status, loop_count, ... }.
import { leerEntrada, registrar } from './lib.mjs';

const entrada = await leerEntrada();

try {
  registrar('sesiones.jsonl', {
    timestamp: new Date().toISOString(),
    conversation_id: entrada.conversation_id ?? null,
    generation_id: entrada.generation_id ?? null,
    status: entrada.status ?? null,
    loop_count: entrada.loop_count ?? null,
    usuario: process.env.USER ?? process.env.USERNAME ?? null,
  });
} catch (error) {
  console.error(`[session-end] no se pudo registrar la sesión: ${error.message}`);
}

// Sin followup_message: no pedimos al agente que continúe.
process.stdout.write('{}\n');
