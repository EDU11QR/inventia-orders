import { useEffect, useState } from "react";
import { obtenerResumenDashboard, getTopVendedores } from "../services/dashboardService";

function DashboardPage({ onIrAPedidos }) {

    // resumen del dashboard
    const [resumen, setResumen] = useState(null);

    // ranking de vendedores
    const [vendedores, setVendedores] = useState(null);

    // estado de carga inicial
    const [cargando, setCargando] = useState(true);

    // mensaje de error de la API
    const [error, setError] = useState(null);

    // error exclusivo del ranking de vendedores
    const [errorVendedores, setErrorVendedores] = useState(false);

    // contador para forzar recarga (botón reintentar)
    const [actualizacion, setActualizacion] = useState(0);

    useEffect(() => {

        let activo = true;

        async function cargarResumen() {

            try {

                const data = await obtenerResumenDashboard();

                if (!activo) {
                    return;
                }

                setResumen(data);

                setError(null);

            } catch (err) {

                console.log(err);

                if (activo) {

                    setError(
                        "No se pudo conectar con el servidor. Verifique que la API esté en ejecución."
                    );
                }

            } finally {

                if (activo) {

                    setCargando(false);
                }
            }
        }

        async function cargarVendedores() {

            try {

                const data = await getTopVendedores();

                if (!activo) {
                    return;
                }

                setVendedores(data);

                setErrorVendedores(false);

            } catch (err) {

                console.log(err);

                if (activo) {

                    setErrorVendedores(true);
                }
            }
        }

        // carga inicial y auto refresh cada 30 segundos
        Promise.all([cargarResumen(), cargarVendedores()]);

        const interval = setInterval(() => {

            Promise.all([cargarResumen(), cargarVendedores()]);

        }, 30000);

        // limpiar intervalo al salir
        return () => {

            activo = false;

            clearInterval(interval);
        };

    }, [actualizacion]);

    if (cargando) {

        return (

            <div className="flex min-h-screen items-center justify-center bg-slate-50">

                <div className="flex flex-col items-center gap-3">

                    <span className="h-10 w-10 animate-spin rounded-full border-4 border-slate-200 border-t-blue-600" />

                    <p className="text-sm font-medium text-slate-500">
                        Cargando dashboard...
                    </p>

                </div>

            </div>
        );
    }

    return (

        <div className="min-h-screen bg-slate-50 px-4 py-6 sm:px-6 lg:px-8">

            {/* titulo */}
            <div className="mb-8">

                <div className="flex flex-wrap items-baseline gap-x-3">

                    <span className="text-3xl font-bold text-blue-600 sm:text-4xl">
                        INVENTIA
                    </span>

                    <span className="text-3xl font-bold text-slate-300 sm:text-4xl">
                        |
                    </span>

                    <h1 className="text-3xl font-bold text-slate-900 sm:text-4xl">
                        Dashboard
                    </h1>

                </div>

                <p className="mt-1 text-sm text-slate-500">
                    Resumen general de pedidos recibidos por WhatsApp
                </p>

            </div>

            {/* error de la API */}
            {
                error && (

                    <div
                        className="
                            mb-6
                            flex
                            flex-col
                            gap-3
                            rounded-xl
                            border
                            border-red-200
                            bg-red-50
                            p-5
                            sm:flex-row
                            sm:items-center
                            sm:justify-between
                        "
                    >

                        <p className="text-sm font-medium text-red-700">
                            {error}
                        </p>

                        <button
                            onClick={() =>
                                setActualizacion(
                                    (valor) => valor + 1
                                )
                            }
                            className="
                                inline-flex
                                items-center
                                justify-center
                                rounded-lg
                                bg-red-600
                                px-4
                                py-2
                                text-sm
                                font-semibold
                                text-white
                                shadow-sm
                                transition-colors
                                hover:bg-red-700
                                focus:outline-none
                                focus:ring-2
                                focus:ring-red-500/30
                            "
                        >
                            Reintentar
                        </button>

                    </div>
                )
            }

            {/* metricas temporales */}
            <div className="mb-6 grid grid-cols-1 gap-4 sm:grid-cols-3 sm:gap-5">

                {/* pedidos de hoy */}
                <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6">

                    <div className="mb-2 flex items-center gap-2">

                        <span className="h-2.5 w-2.5 rounded-full bg-blue-600" />

                        <h2 className="text-sm font-medium text-slate-500">
                            Pedidos de Hoy
                        </h2>

                    </div>

                    <p className="text-3xl font-bold text-blue-600">
                        {resumen?.pedidosHoy ?? 0}
                    </p>

                </div>

                {/* pedidos de la semana */}
                <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6">

                    <div className="mb-2 flex items-center gap-2">

                        <span className="h-2.5 w-2.5 rounded-full bg-sky-500" />

                        <h2 className="text-sm font-medium text-slate-500">
                            Pedidos de la Semana
                        </h2>

                    </div>

                    <p className="text-3xl font-bold text-sky-600">
                        {resumen?.pedidosSemana ?? 0}
                    </p>

                </div>

                {/* pedidos del mes */}
                <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6">

                    <div className="mb-2 flex items-center gap-2">

                        <span className="h-2.5 w-2.5 rounded-full bg-indigo-600" />

                        <h2 className="text-sm font-medium text-slate-500">
                            Pedidos del Mes
                        </h2>

                    </div>

                    <p className="text-3xl font-bold text-indigo-600">
                        {resumen?.pedidosMes ?? 0}
                    </p>

                </div>

            </div>

            {/* conteos por estado */}
            <div className="mb-8 grid grid-cols-1 gap-4 sm:grid-cols-3 sm:gap-5">

                {/* pendientes */}
                <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6">

                    <div className="mb-2 flex items-center gap-2">

                        <span className="h-2.5 w-2.5 rounded-full bg-amber-500" />

                        <h2 className="text-sm font-medium text-slate-500">
                            Pedidos Pendientes
                        </h2>

                    </div>

                    <p className="text-3xl font-bold text-amber-500">
                        {resumen?.pendientes ?? 0}
                    </p>

                </div>

                {/* impresos */}
                <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6">

                    <div className="mb-2 flex items-center gap-2">

                        <span className="h-2.5 w-2.5 rounded-full bg-green-600" />

                        <h2 className="text-sm font-medium text-slate-500">
                            Pedidos Impresos
                        </h2>

                    </div>

                    <p className="text-3xl font-bold text-green-600">
                        {resumen?.impresos ?? 0}
                    </p>

                </div>

                {/* cancelados */}
                <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6">

                    <div className="mb-2 flex items-center gap-2">

                        <span className="h-2.5 w-2.5 rounded-full bg-red-600" />

                        <h2 className="text-sm font-medium text-slate-500">
                            Pedidos Cancelados
                        </h2>

                    </div>

                    <p className="text-3xl font-bold text-red-600">
                        {resumen?.cancelados ?? 0}
                    </p>

                </div>

            </div>

            {/* top vendedores */}
            <div className="mb-8 rounded-xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6">

                <h2 className="mb-4 text-sm font-medium text-slate-500">
                    🏆 Top Vendedores
                </h2>

                {
                    errorVendedores && (

                        <p className="py-6 text-center text-sm text-red-600">
                            No se pudo cargar el ranking
                        </p>
                    )
                }

                {
                    !errorVendedores && !Array.isArray(vendedores) && (

                        <p className="py-6 text-center text-sm text-slate-400">
                            Cargando vendedores...
                        </p>
                    )
                }

                {
                    !errorVendedores &&
                    Array.isArray(vendedores) &&
                    vendedores.length === 0 && (

                        <p className="py-6 text-center text-sm text-slate-400">
                            No hay datos disponibles
                        </p>
                    )
                }

                {
                    !errorVendedores &&
                    Array.isArray(vendedores) &&
                    vendedores.length > 0 && (

                        <ol className="divide-y divide-slate-100">

                            {vendedores.slice(0, 5).map((vendedor, indice) => (

                                <li
                                    key={vendedor.vendedor}
                                    className="flex items-center justify-between py-3 first:pt-0 last:pb-0"
                                >

                                    <span className="flex items-center gap-3">

                                        <span className="w-6 text-right text-sm font-bold tabular-nums text-slate-400">
                                            {indice + 1}.
                                        </span>

                                        <span className="text-sm font-medium text-slate-900">
                                            {vendedor.vendedor}
                                        </span>

                                    </span>

                                    <span className="text-sm font-semibold tabular-nums text-blue-600">
                                        {vendedor.total} pedidos
                                    </span>

                                </li>
                            ))}
                        </ol>
                    )
                }

            </div>

            {/* ultimos pedidos */}
            <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">

                <div className="flex flex-col gap-3 border-b border-slate-200 px-5 py-4 sm:flex-row sm:items-center sm:justify-between">

                    <h2 className="text-base font-semibold text-slate-900">
                        Últimos pedidos
                    </h2>

                    <button
                        onClick={onIrAPedidos}
                        className="
                            inline-flex
                            items-center
                            justify-center
                            rounded-lg
                            bg-blue-600
                            px-4
                            py-2
                            text-sm
                            font-semibold
                            text-white
                            shadow-sm
                            transition-colors
                            hover:bg-blue-700
                            focus:outline-none
                            focus:ring-2
                            focus:ring-blue-500
                            focus:ring-offset-2
                        "
                    >
                        Ver todos los pedidos
                    </button>

                </div>

                <div className="overflow-x-auto">

                    <table className="w-full min-w-[720px]">

                        <thead className="bg-slate-100">

                            <tr>

                                <th className="whitespace-nowrap px-5 py-3.5 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                                    ID
                                </th>

                                <th className="whitespace-nowrap px-5 py-3.5 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                                    Cliente
                                </th>

                                <th className="whitespace-nowrap px-5 py-3.5 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                                    Producto
                                </th>

                                <th className="whitespace-nowrap px-5 py-3.5 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                                    Estado
                                </th>

                                <th className="whitespace-nowrap px-5 py-3.5 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                                    Fecha
                                </th>

                            </tr>

                        </thead>

                        <tbody>

                            {
                                resumen?.ultimosPedidos?.length > 0 ? (

                                    resumen.ultimosPedidos.map((pedido, index) => (

                                        <tr
                                            key={pedido.id}
                                            className={`
                                                border-b
                                                border-slate-100
                                                transition-colors
                                                hover:bg-slate-50
                                                ${index % 2 === 0
                                                    ? "bg-white"
                                                    : "bg-slate-50/50"}
                                            `}
                                        >

                                            <td className="whitespace-nowrap px-5 py-4 font-semibold text-slate-500">
                                                #{pedido.id}
                                            </td>

                                            <td className="whitespace-nowrap px-5 py-4 text-sm font-medium text-slate-900">
                                                {pedido.cliente}
                                            </td>

                                            <td className="max-w-[300px] break-words px-5 py-4 text-sm leading-snug text-slate-600">
                                                {pedido.producto}
                                            </td>

                                            <td className="whitespace-nowrap px-5 py-4">

                                                <span
                                                    className={`
                                                        inline-flex
                                                        items-center
                                                        rounded-full
                                                        px-2.5
                                                        py-1
                                                        text-xs
                                                        font-semibold

                                                        ${pedido.estado === "PENDIENTE"
                                                            ? "bg-amber-100 text-amber-700"
                                                            : pedido.estado === "IMPRESO"
                                                                ? "bg-green-100 text-green-700"
                                                                : pedido.estado === "CANCELADO"
                                                                    ? "bg-red-100 text-red-700"
                                                                    : "bg-slate-100 text-slate-600"
                                                        }
                                                    `}
                                                >
                                                    <span
                                                        className={`
                                                            mr-1.5
                                                            h-1.5
                                                            w-1.5
                                                            rounded-full

                                                            ${pedido.estado === "PENDIENTE"
                                                                ? "bg-amber-500"
                                                                : pedido.estado === "IMPRESO"
                                                                    ? "bg-green-600"
                                                                    : pedido.estado === "CANCELADO"
                                                                        ? "bg-red-600"
                                                                        : "bg-slate-400"
                                                        }
                                                    `}
                                                    />
                                                    {pedido.estado}
                                                </span>

                                            </td>

                                            <td className="whitespace-nowrap px-5 py-4 text-sm tabular-nums text-slate-500">
                                                {
                                                    new Date(
                                                        pedido.fechaRegistro
                                                    ).toLocaleString()
                                                }
                                            </td>

                                        </tr>
                                    ))
                                ) : (
                                    <tr>

                                        <td
                                            colSpan="5"
                                            className="px-5 py-10 text-center text-sm text-slate-400"
                                        >
                                            No hay pedidos recientes
                                        </td>

                                    </tr>
                                )
                            }

                        </tbody>

                    </table>

                </div>

            </div>

        </div>
    );
}

export default DashboardPage;
