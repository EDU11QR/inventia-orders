import { useState } from "react";
import PedidosPage from "./pages/PedidosPage";
import WhatsAppPage from "./pages/WhatsAppPage";

function App() {

  const [moduloActivo, setModuloActivo] = useState("pedidos");

  const opciones = [
    { id: "pedidos", etiqueta: "Pedidos" },
    { id: "whatsapp", etiqueta: "WhatsApp" }
  ];

  return (
    <div className="min-h-screen bg-slate-100 lg:flex">
      <aside className="border-b border-slate-200 bg-white lg:min-h-screen lg:w-60 lg:border-b-0 lg:border-r">
        <div className="border-b border-slate-100 px-5 py-5">
          <p className="text-lg font-bold tracking-tight text-blue-600">INVENTIA</p>
          <p className="mt-0.5 text-xs font-medium text-slate-400">ORDERS</p>
        </div>
        <nav className="flex gap-2 overflow-x-auto p-3 lg:flex-col" aria-label="Módulos principales">
          {opciones.map((opcion) => (
            <button
              key={opcion.id}
              type="button"
              onClick={() => setModuloActivo(opcion.id)}
              className={`rounded-lg px-4 py-2.5 text-left text-sm font-medium transition-colors ${
                moduloActivo === opcion.id
                  ? "bg-blue-600 text-white shadow-sm"
                  : "text-slate-600 hover:bg-slate-100 hover:text-slate-900"
              }`}
            >
              {opcion.etiqueta}
            </button>
          ))}
        </nav>
      </aside>

      <main className="min-w-0 flex-1">
        {moduloActivo === "pedidos" ? <PedidosPage /> : <WhatsAppPage />}
      </main>
    </div>
  );
}

export default App;
