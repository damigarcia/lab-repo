import { useEffect, useState } from 'react';
import { api } from '../api/client.js';
import Mensaje from '../components/Mensaje.jsx';
import { formatFecha } from '../utils/format.js';

export default function ClientesPage() {
  const [clientes, setClientes] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    api
      .listarClientes()
      .then(setClientes)
      .catch((e) => setError(e.message))
      .finally(() => setCargando(false));
  }, []);

  if (cargando) return <p>Cargando…</p>;
  if (error) return <Mensaje tipo="error">No se pudieron cargar los clientes: {error}</Mensaje>;

  return (
    <section>
      <h2>Clientes</h2>
      <table className="tabla">
        <thead>
          <tr>
            <th>Nombre</th>
            <th>Documento</th>
            <th>Email</th>
            <th>Teléfono</th>
            <th>Fecha de nacimiento</th>
          </tr>
        </thead>
        <tbody>
          {clientes.map((c) => (
            <tr key={c.id}>
              <td>{c.nombreCompleto}</td>
              <td>{c.documentoIdentidad}</td>
              <td>{c.email}</td>
              <td>{c.telefono ?? '—'}</td>
              <td>{formatFecha(c.fechaNacimiento)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}
