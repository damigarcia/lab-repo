#!/usr/bin/env node
// Hook beforeShellExecution: bloquea comandos destructivos o peligrosos antes de que el agente los ejecute.
// Entrada (stdin): JSON con al menos { "command": "..." }.
// Salida (stdout): { "permission": "allow" } o { "permission": "deny", "user_message": "...", "agent_message": "..." }.
import { leerEntrada, registrar, responder } from './lib.mjs';

const REGLAS = [
  {
    id: 'rm-recursivo-forzado',
    patron: /\brm\s+(?:-[a-z]*r[a-z]*f[a-z]*|-[a-z]*f[a-z]*r[a-z]*|(?:-[a-z]*r[a-z]*\s+-[a-z]*f[a-z]*)|(?:-[a-z]*f[a-z]*\s+-[a-z]*r[a-z]*)|--recursive\s+--force|--force\s+--recursive)\b/i,
    motivo: 'borrado recursivo forzado (rm -rf)',
  },
  {
    id: 'remove-item-recurse-force',
    patron: /\bRemove-Item\b.*-Recurse\b.*-Force\b|\bRemove-Item\b.*-Force\b.*-Recurse\b|\brmdir\s+\/s\b|\brd\s+\/s\b|\bdel\s+\/[sfq]/i,
    motivo: 'borrado recursivo forzado (Windows)',
  },
  {
    id: 'git-push-force',
    patron: /\bgit\s+push\b[^|;&]*(?:\s--force(?:-with-lease)?\b|\s-f\b|\s\+\S)/i,
    motivo: 'git push forzado (reescribe historia remota)',
  },
  {
    id: 'drop-table',
    patron: /\bDROP\s+(?:TABLE|DATABASE|SCHEMA)\b/i,
    motivo: 'DROP TABLE / DATABASE / SCHEMA',
  },
  {
    id: 'truncate-table',
    patron: /\bTRUNCATE\s+TABLE\b/i,
    motivo: 'TRUNCATE TABLE',
  },
  {
    id: 'pipe-a-shell',
    patron: /\b(?:curl|wget|iwr|Invoke-WebRequest|irm|Invoke-RestMethod)\b[^|]*\|\s*(?:sudo\s+)?(?:sh|bash|zsh|iex|Invoke-Expression|powershell|pwsh|python3?|node)\b/i,
    motivo: 'descargar y ejecutar un script remoto (curl ... | sh)',
  },
];

const entrada = await leerEntrada();
const comando = String(entrada.command ?? '');
const regla = REGLAS.find((r) => r.patron.test(comando));

if (regla) {
  try {
    registrar('shell-denegados.jsonl', {
      timestamp: new Date().toISOString(),
      conversation_id: entrada.conversation_id ?? null,
      regla: regla.id,
      command: comando,
    });
  } catch {
    // la auditoría nunca debe impedir responder
  }
  responder({
    permission: 'deny',
    user_message: `Comando bloqueado por política del equipo: ${regla.motivo}.`,
    agent_message:
      `El hook guard-shell bloqueó el comando porque coincide con la regla "${regla.id}" (${regla.motivo}). ` +
      'No intentes ejecutarlo de otra forma. Propón una alternativa segura o pide al usuario que lo ejecute manualmente si es imprescindible.',
  });
} else {
  responder({ permission: 'allow' });
}
