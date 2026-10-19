// Utilidades de formato compartidas. Usar siempre estas funciones en los componentes.

const LOCALE = 'es-VE';

const formatoMoneda = new Intl.NumberFormat(LOCALE, {
  style: 'currency',
  currency: 'USD',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

/**
 * Formatea un monto en USD. Acepta número o string (la API envía BigDecimal como número).
 * Devuelve '—' si el valor es nulo o inválido.
 */
export function formatMoneda(valor) {
  if (valor === null || valor === undefined || valor === '') return '—';
  const numero = Number(valor);
  if (Number.isNaN(numero)) return '—';
  return formatoMoneda.format(numero);
}

/**
 * Formatea una fecha ISO (yyyy-MM-dd) como dd/MM/yyyy sin conversión de zona horaria.
 * Devuelve '—' si el valor es nulo o no tiene el formato esperado.
 */
export function formatFecha(isoDate) {
  if (!isoDate) return '—';
  const match = /^(\d{4})-(\d{2})-(\d{2})/.exec(isoDate);
  if (!match) return '—';
  const [, anio, mes, dia] = match;
  return `${dia}/${mes}/${anio}`;
}

const ETIQUETAS_ESTADO = {
  VIGENTE: 'Vigente',
  VENCIDA: 'Vencida',
  CANCELADA: 'Cancelada',
  REPORTADO: 'Reportado',
  EN_REVISION: 'En revisión',
  APROBADO: 'Aprobado',
  RECHAZADO: 'Rechazado',
};

/** Etiqueta legible para un estado de póliza o siniestro. */
export function formatEstado(estado) {
  return ETIQUETAS_ESTADO[estado] ?? estado ?? '—';
}
