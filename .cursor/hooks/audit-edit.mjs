#!/usr/bin/env node
// Hook afterFileEdit: deja traza de cada edición hecha por el agente (trazabilidad del desarrollo asistido).
// Entrada (stdin): { conversation_id, generation_id, file_path, edits: [{ old_string, new_string }], ... }.
// Agrega una línea JSONL a .cursor/audit/edits.jsonl. No modifica nada más.
import { leerEntrada, registrar, rutaRelativa } from './lib.mjs';

const entrada = await leerEntrada();
const edits = Array.isArray(entrada.edits) ? entrada.edits : [];

const contarLineas = (texto) => (texto ? String(texto).split('\n').length : 0);

try {
  registrar('edits.jsonl', {
    timestamp: new Date().toISOString(),
    conversation_id: entrada.conversation_id ?? null,
    generation_id: entrada.generation_id ?? null,
    file_path: rutaRelativa(entrada.file_path ?? null),
    cantidad_edits: edits.length,
    lineas_eliminadas: edits.reduce((n, e) => n + contarLineas(e.old_string), 0),
    lineas_agregadas: edits.reduce((n, e) => n + contarLineas(e.new_string), 0),
    usuario: process.env.USER ?? process.env.USERNAME ?? null,
  });
} catch (error) {
  console.error(`[audit-edit] no se pudo registrar la edición: ${error.message}`);
}

process.stdout.write('{}\n');
