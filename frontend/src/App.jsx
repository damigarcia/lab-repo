import { NavLink, Navigate, Route, Routes } from 'react-router-dom';
import PolizasPage from './pages/PolizasPage.jsx';
import PolizaDetallePage from './pages/PolizaDetallePage.jsx';
import ClientesPage from './pages/ClientesPage.jsx';

export default function App() {
  return (
    <div className="app">
      <header className="app-header">
        <h1>Aseguradora Demo</h1>
        <nav>
          <NavLink to="/polizas">Pólizas</NavLink>
          <NavLink to="/clientes">Clientes</NavLink>
        </nav>
      </header>
      <main className="app-main">
        <Routes>
          <Route path="/" element={<Navigate to="/polizas" replace />} />
          <Route path="/polizas" element={<PolizasPage />} />
          <Route path="/polizas/:id" element={<PolizaDetallePage />} />
          <Route path="/clientes" element={<ClientesPage />} />
          <Route path="*" element={<p>Página no encontrada.</p>} />
        </Routes>
      </main>
    </div>
  );
}
