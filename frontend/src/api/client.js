// Cliente HTTP centralizado. Todas las llamadas a la API deben pasar por aquí.
// En desarrollo, Vite redirige /api a http://localhost:8080 (ver vite.config.js).

const BASE_URL = '/api';

export class ApiError extends Error {
  constructor(status, codigo, mensaje, detalles = []) {
    super(mensaje);
    this.status = status;
    this.codigo = codigo;
    this.detalles = detalles;
  }
}

async function request(path, options = {}) {
  const response = await fetch(`${BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json', ...options.headers },
    ...options,
  });

  if (!response.ok) {
    let body = {};
    try {
      body = await response.json();
    } catch {
      // respuesta sin cuerpo JSON
    }
    throw new ApiError(
      response.status,
      body.codigo ?? 'ERROR_HTTP',
      body.mensaje ?? `Error ${response.status}`,
      body.detalles ?? [],
    );
  }

  if (response.status === 204) return null;
  return response.json();
}

function toQuery(params) {
  const limpio = Object.fromEntries(
    Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== ''),
  );
  const qs = new URLSearchParams(limpio).toString();
  return qs ? `?${qs}` : '';
}

export const api = {
  listarPolizas: (filtros = {}) => request(`/polizas${toQuery(filtros)}`),
  obtenerPoliza: (id) => request(`/polizas/${id}`),
  crearPoliza: (datos) => request('/polizas', { method: 'POST', body: JSON.stringify(datos) }),
  cancelarPoliza: (id, motivo) =>
    request(`/polizas/${id}/cancelacion`, { method: 'POST', body: JSON.stringify({ motivo }) }),

  listarClientes: () => request('/clientes'),
  obtenerCliente: (id) => request(`/clientes/${id}`),

  listarSiniestros: (polizaId) => request(`/siniestros${toQuery({ polizaId })}`),
  registrarSiniestro: (datos) => request('/siniestros', { method: 'POST', body: JSON.stringify(datos) }),
};
