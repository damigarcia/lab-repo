import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client.js';
import EstadoBadge from '../components/EstadoBadge.jsx';
import Mensaje from '../components/Mensaje.jsx';

const ESTADOS = ['VIGENTE', 'VENCIDA', 'CANCELADA'];
const TIPOS = ['AUTO', 'HOGAR', 'VIDA'];

function formatearFecha(fecha) {
  if (!fecha) return '-';
  return new Date(fecha).toLocaleDateString('es-VE');
}

function formatearMonto(monto) {
  return '$ ' + Number(monto).toFixed(2);
}

export default function PolizasPage() {
  const [filtros, setFiltros] = useState({ estado: '', tipo: '' });
  const [polizas, setPolizas] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let activo = true;
    setCargando(true);
    setError(null);
    api
      .listarPolizas(filtros)
      .then((datos) => activo && setPolizas(datos))
      .catch((e) => activo && setError(e.message))
      .finally(() => activo && setCargando(false));
    return () => {
      activo = false;
    };
  }, [filtros]);

  const cambiarFiltro = (campo) => (evento) => setFiltros((f) => ({ ...f, [campo]: evento.target.value }));

  return (
    <section>
      <h2>Pólizas</h2>

      <div className="filtros">
        <label>
          Estado
          <select value={filtros.estado} onChange={cambiarFiltro('estado')}>
            <option value="">Todos</option>
            {ESTADOS.map((e) => (
              <option key={e} value={e}>
                {e}
              </option>
            ))}
          </select>
        </label>
        <label>
          Tipo
          <select value={filtros.tipo} onChange={cambiarFiltro('tipo')}>
            <option value="">Todos</option>
            {TIPOS.map((t) => (
              <option key={t} value={t}>
                {t}
              </option>
            ))}
          </select>
        </label>
      </div>

      {error && <Mensaje tipo="error">No se pudieron cargar las pólizas: {error}</Mensaje>}
      {cargando && <p>Cargando…</p>}

      {!cargando && !error && (
        <table className="tabla">
          <thead>
            <tr>
              <th>Número</th>
              <th>Cliente</th>
              <th>Tipo</th>
              <th>Estado</th>
              <th className="num">Prima anual</th>
              <th>Inicio</th>
              <th>Fin</th>
            </tr>
          </thead>
          <tbody>
            {polizas.length === 0 && (
              <tr>
                <td colSpan={7}>No hay pólizas para los filtros seleccionados.</td>
              </tr>
            )}
            {polizas.map((p) => (
              <tr key={p.id}>
                <td>
                  <Link to={`/polizas/${p.id}`}>{p.numero}</Link>
                </td>
                <td>{p.clienteNombre}</td>
                <td>{p.tipo}</td>
                <td>
                  <EstadoBadge estado={p.estado} />
                </td>
                <td className="num">{formatearMonto(p.primaAnual)}</td>
                <td>{formatearFecha(p.fechaInicio)}</td>
                <td>{formatearFecha(p.fechaFin)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
