#!/usr/bin/env node
// Hook PreToolUse (matcher "Bash"): bloquea comandos destructivos o peligrosos antes de que el agente los ejecute.
// Entrada (stdin): { session_id, cwd, hook_event_name, tool_name: "Bash", tool_input: { command, description }, ... }.
// Salida (stdout): {} para dejar pasar, o
//   { "hookSpecificOutput": { "hookEventName": "PreToolUse", "permissionDecision": "deny", "permissionDecisionReason": "..." } }.
// El motivo de un "deny" se le muestra al agente para que proponga otra alternativa.
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
const comando = String(entrada.tool_input?.command ?? '');
const regla = REGLAS.find((r) => r.patron.test(comando));

if (regla) {
  try {
    registrar('shell-denegados.jsonl', {
      timestamp: new Date().toISOString(),
      session_id: entrada.session_id ?? null,
      regla: regla.id,
      command: comando,
    });
  } catch {
    // la auditoría nunca debe impedir responder
  }
  responder({
    hookSpecificOutput: {
      hookEventName: 'PreToolUse',
      permissionDecision: 'deny',
      permissionDecisionReason:
        `Comando bloqueado por política del equipo: ${regla.motivo} (regla "${regla.id}" del hook guard-shell). ` +
        'No intentes ejecutarlo de otra forma. Propón una alternativa segura o pide al usuario que lo ejecute manualmente si es imprescindible.',
    },
  });
} else {
  // Sin decisión: sigue el flujo normal de permisos de Claude Code.
  responder({});
}
