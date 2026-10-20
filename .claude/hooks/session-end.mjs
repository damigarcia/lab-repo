#!/usr/bin/env node
// Hook Stop: registra el fin de cada respuesta del agente en .claude/audit/sesiones.jsonl.
// Entrada (stdin): { session_id, hook_event_name: "Stop", stop_hook_active, last_assistant_message, ... }.
import { leerEntrada, registrar } from './lib.mjs';

const entrada = await leerEntrada();

try {
  registrar('sesiones.jsonl', {
    timestamp: new Date().toISOString(),
    session_id: entrada.session_id ?? null,
    stop_hook_active: entrada.stop_hook_active ?? null,
    usuario: process.env.USER ?? process.env.USERNAME ?? null,
  });
} catch (error) {
  console.error(`[session-end] no se pudo registrar la sesión: ${error.message}`);
}

// Sin "decision": "block": no pedimos al agente que continúe.
process.stdout.write('{}\n');
