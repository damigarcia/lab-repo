// Utilidades compartidas por los hooks de Cursor. Solo Node estándar (multiplataforma).
import { appendFileSync, mkdirSync } from 'node:fs';
import { dirname, isAbsolute, join, relative } from 'node:path';
import { fileURLToPath } from 'node:url';

// Raíz del repo: .cursor/hooks/ -> ../..
export const REPO_ROOT = join(dirname(fileURLToPath(import.meta.url)), '..', '..');
export const AUDIT_DIR = join(REPO_ROOT, '.cursor', 'audit');

/** Lee todo stdin y lo parsea como JSON. Devuelve {} si está vacío o es inválido. */
export async function leerEntrada() {
  const chunks = [];
  for await (const chunk of process.stdin) chunks.push(chunk);
  const texto = Buffer.concat(chunks).toString('utf8').trim();
  if (!texto) return {};
  try {
    return JSON.parse(texto);
  } catch {
    return {};
  }
}

/** Escribe la respuesta del hook en stdout como JSON. */
export function responder(objeto) {
  process.stdout.write(JSON.stringify(objeto) + '\n');
}

/** Agrega una línea JSONL al archivo de auditoría indicado. */
export function registrar(archivo, evento) {
  mkdirSync(AUDIT_DIR, { recursive: true });
  appendFileSync(join(AUDIT_DIR, archivo), JSON.stringify(evento) + '\n', 'utf8');
}

/** Ruta relativa al repo con separador '/', para que el log sea igual en Windows y Unix. */
export function rutaRelativa(ruta) {
  if (!ruta) return ruta;
  const rel = isAbsolute(ruta) ? relative(REPO_ROOT, ruta) : ruta;
  return rel.split('\\').join('/');
}
