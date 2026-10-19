import { formatEstado } from '../utils/format.js';

export default function EstadoBadge({ estado }) {
  return <span className={`badge badge-${String(estado).toLowerCase()}`}>{formatEstado(estado)}</span>;
}
