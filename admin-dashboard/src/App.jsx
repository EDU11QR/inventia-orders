import { useState } from "react";
import DashboardPage from "./pages/DashboardPage";
import PedidosPage from "./pages/PedidosPage";
import WhatsAppPage from "./pages/WhatsAppPage";

// iconos svg inline (sin librerías nuevas)
const iconoDashboard = (
  <svg
    className="h-5 w-5 shrink-0"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    aria-hidden="true"
  >
    <rect x="3" y="3" width="7" height="7" rx="1.5" />
    <rect x="14" y="3" width="7" height="7" rx="1.5" />
    <rect x="3" y="14" width="7" height="7" rx="1.5" />
    <rect x="14" y="14" width="7" height="7" rx="1.5" />
  </svg>
);

const iconoPedidos = (
  <svg
    className="h-5 w-5 shrink-0"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    aria-hidden="true"
  >
    <path d="M6 2h12a1 1 0 0 1 1 1v18l-3-2-2.5 2L11 19l-2.5 2L6 19l-2 2V3a1 1 0 0 1 1-1Z" />
    <path d="M9 8h6" />
    <path d="M9 12h4" />
  </svg>
);

const iconoWhatsApp = (
  <svg
    className="h-5 w-5 shrink-0"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    aria-hidden="true"
  >
    <path d="M21 11.5a8.5 8.5 0 0 1-12.4 7.5L3 21l2-5.4A8.5 8.5 0 1 1 21 11.5Z" />
    <path d="M9 10h.01M13 10h.01M17 10h.01" />
  </svg>
);

const iconosPorModulo = {
  dashboard: iconoDashboard,
  pedidos: iconoPedidos,
  whatsapp: iconoWhatsApp
};

function App() {

  const [moduloActivo, setModuloActivo] = useState("dashboard");

  // estado del sidebar; vive en App para
  // mantenerse mientras se navega entre páginas
  const [sidebarAbierto, setSidebarAbierto] = useState(true);

  const opciones = [
    { id: "dashboard", etiqueta: "Dashboard" },
    { id: "pedidos", etiqueta: "Pedidos" },
    { id: "whatsapp", etiqueta: "WhatsApp" }
  ];

  return (
    <div className="min-h-screen bg-slate-100 lg:flex">

      {/* sidebar */}
      <aside
        className={`
            border-b
            border-slate-200
            bg-white
            overflow-hidden
            transition-all
            ease-in-out
            duration-[250ms]
            lg:min-h-screen
            lg:border-b-0
            lg:border-r
            ${sidebarAbierto ? "lg:w-60" : "lg:w-[70px]"}
        `}
      >

        {/* logo + boton hamburguesa */}
        <div className="border-b border-slate-100 px-4 py-5">

          <div
            className={`
                flex items-center gap-2
                ${sidebarAbierto ? "justify-between" : "lg:justify-center"}
            `}
          >

            <div className={`whitespace-nowrap ${sidebarAbierto ? "" : "lg:hidden"}`}>

              <p className="text-lg font-bold tracking-tight text-blue-600">
                INVENTIA
              </p>

              <p className="mt-0.5 text-xs font-medium text-slate-400">
                ORDERS
              </p>

            </div>

            <button
              type="button"
              onClick={() => setSidebarAbierto((valor) => !valor)}
              aria-label={
                sidebarAbierto ? "Colapsar menú" : "Expandir menú"
              }
              aria-expanded={sidebarAbierto}
              title={
                sidebarAbierto ? "Colapsar menú" : "Expandir menú"
              }
              className="
                  inline-flex h-9 w-9 shrink-0 items-center justify-center
                  rounded-lg text-slate-500 transition-colors
                  hover:bg-slate-100 hover:text-slate-900
                  focus:outline-none focus:ring-2 focus:ring-blue-500/40
              "
            >

              <svg
                className="h-5 w-5"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                aria-hidden="true"
              >
                <path d="M4 6h16M4 12h16M4 18h16" />
              </svg>

            </button>

          </div>

        </div>

        {/* navegacion */}
        <nav
          className="flex gap-2 overflow-x-auto p-3 lg:flex-col lg:overflow-visible"
          aria-label="Módulos principales"
        >

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
                  ${moduloActivo === opcion.id
                      ? "bg-blue-600 text-white shadow-sm"
                      : "text-slate-600 hover:bg-slate-100 hover:text-slate-900"}
                  ${sidebarAbierto ? "" : "lg:justify-center lg:px-0"}
              `}
            >

              {iconosPorModulo[opcion.id]}

              <span className={sidebarAbierto ? "" : "lg:hidden"}>
                {opcion.etiqueta}
              </span>

            </button>
          ))}

        </nav>

      </aside>

      {/* contenido principal (se expande solo al colapsar) */}
      <main className="min-w-0 flex-1">

        {moduloActivo === "dashboard" ? (
          <DashboardPage />
        ) : moduloActivo === "pedidos" ? (
          <PedidosPage />
        ) : (
          <WhatsAppPage />
        )}

      </main>

    </div>
  );
}

export default App;
