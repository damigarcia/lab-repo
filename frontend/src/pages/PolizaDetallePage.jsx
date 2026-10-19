import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api } from '../api/client.js';
import EstadoBadge from '../components/EstadoBadge.jsx';
import Mensaje from '../components/Mensaje.jsx';

// Formato de fechas y montos para el detalle
function formatearFecha(fecha) {
  if (!fecha) return '-';
  const d = new Date(fecha);
  return d.toLocaleDateString('es-VE');
}

function formatearMonto(monto) {
  if (monto == null) return '-';
  return '$ ' + Number(monto).toFixed(2);
}

export default function PolizaDetallePage() {
  const { id } = useParams();
  const [poliza, setPoliza] = useState(null);
  const [siniestros, setSiniestros] = useState([]);
  const [error, setError] = useState(null);
  const [mensaje, setMensaje] = useState(null);

  const cargar = () => {
    setError(null);
    Promise.all([api.obtenerPoliza(id), api.listarSiniestros(id)])
      .then(([p, s]) => {
        setPoliza(p);
        setSiniestros(s);
      })
      .catch((e) => setError(e.message));
  };

  useEffect(cargar, [id]);

  const cancelar = async () => {
    const motivo = window.prompt('Motivo de la cancelación:');
    if (!motivo) return;
    try {
      await api.cancelarPoliza(id, motivo);
      setMensaje('La póliza fue cancelada.');
      cargar();
    } catch (e) {
      setError(e.message);
    }
  };

  if (error) return <Mensaje tipo="error">{error}</Mensaje>;
  if (!poliza) return <p>Cargando…</p>;

  return (
    <section>
      <p>
        <Link to="/polizas">← Volver al listado</Link>
      </p>
      <h2>
        Póliza {poliza.numero} <EstadoBadge estado={poliza.estado} />
      </h2>

      {mensaje && <Mensaje tipo="exito">{mensaje}</Mensaje>}
      {poliza.enPeriodoDeGracia && (
        <Mensaje tipo="aviso">La póliza está vencida pero aún dentro del período de gracia.</Mensaje>
      )}

      <dl className="detalle">
        <dt>Cliente</dt>
        <dd>{poliza.clienteNombre}</dd>
        <dt>Tipo</dt>
        <dd>{poliza.tipo}</dd>
        <dt>Suma asegurada</dt>
        <dd>{formatearMonto(poliza.sumaAsegurada)}</dd>
        <dt>Prima anual</dt>
        <dd>{formatearMonto(poliza.primaAnual)}</dd>
        <dt>Vigencia</dt>
        <dd>
          {formatearFecha(poliza.fechaInicio)} al {formatearFecha(poliza.fechaFin)}
        </dd>
        {poliza.motivoCancelacion && (
          <>
            <dt>Motivo de cancelación</dt>
            <dd>{poliza.motivoCancelacion}</dd>
          </>
        )}
      </dl>

      <div className="acciones">
        {poliza.estado === 'VIGENTE' && (
          <button type="button" className="boton boton-peligro" onClick={cancelar}>
            Cancelar póliza
          </button>
        )}
      </div>

      <h3>Siniestros</h3>
      {siniestros.length === 0 ? (
        <p>Esta póliza no tiene siniestros registrados.</p>
      ) : (
        <table className="tabla">
          <thead>
            <tr>
              <th>Fecha ocurrencia</th>
              <th>Descripción</th>
              <th className="num">Monto reclamado</th>
              <th>Estado</th>
            </tr>
          </thead>
          <tbody>
            {siniestros.map((s) => (
              <tr key={s.id}>
                <td>{formatearFecha(s.fechaOcurrencia)}</td>
                <td>{s.descripcion}</td>
                <td className="num">{formatearMonto(s.montoReclamado)}</td>
                <td>
                  <EstadoBadge estado={s.estado} />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
