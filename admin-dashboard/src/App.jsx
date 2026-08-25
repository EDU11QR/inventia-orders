import { useState } from "react";
import { useAuth } from "./context/AuthContext";
import LoginPage from "./pages/LoginPage";
import DashboardPage from "./pages/DashboardPage";
import PedidosPage from "./pages/PedidosPage";
import WhatsAppPage from "./pages/WhatsAppPage";
import ConfiguracionPage from "./pages/ConfiguracionPage";

const iconoDashboard = (
  <svg className="h-5 w-5 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
    <rect x="3" y="3" width="7" height="7" rx="1.5" />
    <rect x="14" y="3" width="7" height="7" rx="1.5" />
    <rect x="3" y="14" width="7" height="7" rx="1.5" />
    <rect x="14" y="14" width="7" height="7" rx="1.5" />
  </svg>
);

const iconoPedidos = (
  <svg className="h-5 w-5 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
    <path d="M6 2h12a1 1 0 0 1 1 1v18l-3-2-2.5 2L11 19l-2.5 2L6 19l-2 2V3a1 1 0 0 1 1-1Z" />
    <path d="M9 8h6" />
    <path d="M9 12h4" />
  </svg>
);

const iconoConfiguracion = (
  <svg className="h-5 w-5 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
    <circle cx="12" cy="12" r="3" />
    <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 1 1-4 0v-.09a1.65 1.65 0 0 0-1-1.51 1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 1 1 0-4h.09a1.65 1.65 0 0 0 1.51-1 1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33h.01a1.65 1.65 0 0 0 1-1.51V3a2 2 0 1 1 4 0v.09a1.65 1.65 0 0 0 1 1.51h.01a1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82v.01a1.65 1.65 0 0 0 1.51 1H21a2 2 0 1 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1Z" />
  </svg>
);

const iconoUsuarios = (
  <svg className="h-5 w-5 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
    <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
    <circle cx="9" cy="7" r="4" />
    <path d="M22 21v-2a4 4 0 0 0-3-3.87" />
    <path d="M16 3.13a4 4 0 0 1 0 7.75" />
  </svg>
);

const iconosPorModulo = {
  dashboard: iconoDashboard,
  pedidos: iconoPedidos,
  configuracion: iconoConfiguracion,
  usuarios: iconoUsuarios,
};

// menú por rol
const MODULOS_POR_ROL = {
  ADMIN: [
    { id: "dashboard", etiqueta: "Dashboard" },
    { id: "pedidos", etiqueta: "Pedidos" },
    { id: "configuracion", etiqueta: "Configuración" },
    { id: "usuarios", etiqueta: "Usuarios" },
  ],
  VENTAS: [
    { id: "dashboard", etiqueta: "Dashboard" },
    { id: "pedidos", etiqueta: "Pedidos" },
  ],
  ALMACEN: [
    { id: "pedidos", etiqueta: "Pedidos" },
  ],
};

function App() {

  const { usuario, cargando, logout } = useAuth();

  const [moduloActivo, setModuloActivo] = useState("dashboard");
  const [sidebarAbierto, setSidebarAbierto] = useState(true);

  if (cargando) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-50">
        <div className="flex flex-col items-center gap-3">
          <span className="h-10 w-10 animate-spin rounded-full border-4 border-slate-200 border-t-blue-600" />
          <p className="text-sm font-medium text-slate-500">Cargando...</p>
        </div>
      </div>
    );
  }

  if (!usuario) {
    return <LoginPage />;
  }

  const opciones = MODULOS_POR_ROL[usuario.rol] || [];

  // si el módulo activo no está permitido, redirigir al primero
  const moduloValido = opciones.find((o) => o.id === moduloActivo);
  const moduloActual = moduloValido ? moduloActivo : opciones[0]?.id;

  return (
    <div className="min-h-screen bg-slate-100 lg:flex">

      {/* sidebar */}
      <aside
        className={`
            border-b border-slate-200 bg-white overflow-hidden
            transition-all ease-in-out duration-[250ms]
            lg:min-h-screen lg:border-b-0 lg:border-r
            ${sidebarAbierto ? "lg:w-60" : "lg:w-[70px]"}
        `}
      >
        {/* logo + hamburguesa */}
        <div className="border-b border-slate-100 px-4 py-5">
          <div className={`flex items-center gap-2 ${sidebarAbierto ? "justify-between" : "lg:justify-center"}`}>
            <div className={`whitespace-nowrap ${sidebarAbierto ? "" : "lg:hidden"}`}>
              <p className="text-lg font-bold tracking-tight text-blue-600">INVENTIA</p>
              <p className="mt-0.5 text-xs font-medium text-slate-400">ORDERS</p>
            </div>
            <button
              type="button"
              onClick={() => setSidebarAbierto((v) => !v)}
              className="inline-flex h-9 w-9 shrink-0 items-center justify-center rounded-lg text-slate-500 transition-colors hover:bg-slate-100 hover:text-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-500/40"
            >
              <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" aria-hidden="true">
                <path d="M4 6h16M4 12h16M4 18h16" />
              </svg>
            </button>
          </div>
        </div>

        {/* navegación */}
        <nav className="flex gap-2 overflow-x-auto p-3 lg:flex-col lg:overflow-visible" aria-label="Módulos principales">
          {opciones.map((opcion) => (
            <button
              key={opcion.id}
              type="button"
              onClick={() => setModuloActivo(opcion.id)}
              title={opcion.etiqueta}
              className={`
                  flex items-center gap-3 whitespace-nowrap
                  rounded-lg px-4 py-2.5 text-left text-sm font-medium
                  transition-colors
                  ${moduloActual === opcion.id
                      ? "bg-blue-600 text-white shadow-sm"
                      : "text-slate-600 hover:bg-slate-100 hover:text-slate-900"}
                  ${sidebarAbierto ? "" : "lg:justify-center lg:px-0"}
              `}
            >
              {iconosPorModulo[opcion.id]}
              <span className={sidebarAbierto ? "" : "lg:hidden"}>{opcion.etiqueta}</span>
            </button>
          ))}
        </nav>

        {/* usuario + logout */}
        <div className={`border-t border-slate-100 p-3 ${sidebarAbierto ? "" : "lg:px-2"}`}>
          <div className={`flex items-center gap-2 ${sidebarAbierto ? "justify-between" : "lg:justify-center"}`}>
            <div className={`min-w-0 ${sidebarAbierto ? "" : "lg:hidden"}`}>
              <p className="truncate text-xs font-medium text-slate-900">{usuario.nombre}</p>
              <p className="truncate text-xs text-slate-400">{usuario.rol}</p>
            </div>
            <button
              type="button"
              onClick={logout}
              title="Cerrar sesión"
              className="inline-flex h-8 w-8 shrink-0 items-center justify-center rounded-lg text-slate-400 transition-colors hover:bg-red-50 hover:text-red-600 focus:outline-none focus:ring-2 focus:ring-red-500/40"
            >
              <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
                <polyline points="16 17 21 12 16 7" />
                <line x1="21" y1="12" x2="9" y2="12" />
              </svg>
            </button>
          </div>
        </div>
      </aside>

      {/* contenido principal */}
      <main className="min-w-0 flex-1">
        {moduloActual === "dashboard" && <DashboardPage />}
        {moduloActual === "pedidos" && <PedidosPage />}
        {moduloActual === "configuracion" && <ConfiguracionPage />}
        {moduloActual === "usuarios" && (
          <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
            <p className="text-sm text-slate-400">Módulo de Usuarios (próximamente)</p>
          </div>
        )}
      </main>

    </div>
  );
}

export default App;
