#!/usr/bin/env node
// Hook PostToolUse (matcher "Edit|Write|MultiEdit"): deja traza de cada edición hecha por el agente (trazabilidad del desarrollo asistido).
// Entrada (stdin): { session_id, tool_name, tool_input: { file_path, old_string, new_string } | { file_path, content } | { file_path, edits: [...] }, ... }.
// Agrega una línea JSONL a .claude/audit/edits.jsonl. No modifica nada más.
import { leerEntrada, registrar, rutaRelativa } from './lib.mjs';

const entrada = await leerEntrada();
const input = entrada.tool_input ?? {};

// Edit trae old_string/new_string, MultiEdit trae edits[] y Write trae el contenido completo.
const edits = Array.isArray(input.edits)
  ? input.edits
  : input.content !== undefined
    ? [{ old_string: '', new_string: input.content }]
    : [{ old_string: input.old_string, new_string: input.new_string }];

const contarLineas = (texto) => (texto ? String(texto).split('\n').length : 0);

try {
  registrar('edits.jsonl', {
    timestamp: new Date().toISOString(),
    session_id: entrada.session_id ?? null,
    tool_name: entrada.tool_name ?? null,
    file_path: rutaRelativa(input.file_path ?? null),
    cantidad_edits: edits.length,
    lineas_eliminadas: edits.reduce((n, e) => n + contarLineas(e.old_string), 0),
    lineas_agregadas: edits.reduce((n, e) => n + contarLineas(e.new_string), 0),
    usuario: process.env.USER ?? process.env.USERNAME ?? null,
  });
} catch (error) {
  console.error(`[audit-edit] no se pudo registrar la edición: ${error.message}`);
}

process.stdout.write('{}\n');
