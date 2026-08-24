import { useEffect, useState } from "react";
import {
    obtenerResumenDashboard,
    obtenerDashboard
} from "../services/dashboardService";
import GraficoLineas from "../components/GraficoLineas";

// nombres cortos de día para el eje X semanal
const NOMBRES_DIA = ["dom", "lun", "mar", "mié", "jue", "vie", "sáb"];

// medallas para el top 3 del ranking
const MEDALLAS = ["🥇", "🥈", "🥉"];

// nombre del mes actual capitalizado + año (ej. "Agosto 2026")
function nombreMesActual() {

    const partes = new Intl.DateTimeFormat("es", {
        month: "long",
        year: "numeric"
    }).formatToParts(new Date());

    const mes =
        partes.find((parte) => parte.type === "month")?.value ?? "";

    const anio =
        partes.find((parte) => parte.type === "year")?.value ?? "";

    return `${mes.charAt(0).toUpperCase()}${mes.slice(1)} ${anio}`;
}

// =========================================================
// rellena la serie del backend con ceros para que
// el eje X sea continuo:
//   dia    -> horas 00:00 hasta la hora actual
//   semana -> lunes a domingo (semana completa)
//   mes    -> día 1 al último día del mes
// =========================================================
function construirSerieCompleta(periodo, filas) {

    if (!Array.isArray(filas)) {
        return [];
    }

    const mapa = new Map(
        filas.map((fila) => [String(fila.etiqueta), fila.total])
    );

    const ahora = new Date();
    const puntos = [];

    if (periodo === "dia") {

        for (let hora = 0; hora <= ahora.getHours(); hora++) {

            puntos.push({
                etiqueta: `${String(hora).padStart(2, "0")}:00`,
                // el backend devuelve "00".."23"
                total: mapa.get(String(hora).padStart(2, "0")) ?? 0
            });
        }

    } else if (periodo === "semana") {

        // lunes = 0; eje completo lun..dom
        const diasTranscurridos = (ahora.getDay() + 6) % 7;

        const lunes = new Date(
            ahora.getFullYear(),
            ahora.getMonth(),
            ahora.getDate() - diasTranscurridos
        );

        for (let i = 0; i < 7; i++) {

            const dia = new Date(lunes);
            dia.setDate(lunes.getDate() + i);

            const iso =
                `${dia.getFullYear()}-` +
                `${String(dia.getMonth() + 1).padStart(2, "0")}-` +
                `${String(dia.getDate()).padStart(2, "0")}`;

            puntos.push({
                etiqueta:
                    i <= diasTranscurridos
                        ? `${NOMBRES_DIA[dia.getDay()]} ${dia.getDate()}`
                        : NOMBRES_DIA[dia.getDay()],
                total: mapa.get(iso) ?? 0
            });
        }

    } else { // MES

        const ultimoDiaMes =
            new Date(ahora.getFullYear(), ahora.getMonth() + 1, 0)
                .getDate();

        for (let diaMes = 1; diaMes <= ultimoDiaMes; diaMes++) {

            puntos.push({
                etiqueta: String(diaMes),
                // el backend devuelve "01".."31"
                total: mapa.get(String(diaMes).padStart(2, "0")) ?? 0
            });
        }
    }

    return puntos;
}

function DashboardPage() {

    // resumen del dashboard (tarjetas de métricas)
    const [resumen, setResumen] = useState(null);

    // =========================================================
    // ESTADO ÚNICO DEL PERIODO DE ANÁLISIS
    // =========================================================
    // compartido por el gráfico, el ranking de empleados
    // y futuras métricas del dashboard:
    //   "dia" | "semana" | "mes"
    // =========================================================
    const [periodo, setPeriodo] = useState("dia");

    // respuesta consolidada del backend para el periodo
    // ({ metricas, grafico, topEmpleados })
    const [datosPeriodo, setDatosPeriodo] = useState(null);

    // estado de carga inicial
    const [cargando, setCargando] = useState(true);

    // mensaje de error de la API
    const [error, setError] = useState(null);

    // error exclusivo del bloque sincronizado
    // (gráfico y ranking viajan en la misma llamada)
    const [errorPeriodo, setErrorPeriodo] = useState(false);

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

        async function cargarDashboardPeriodo(periodoSeleccionado) {

            try {

                const data = await obtenerDashboard(periodoSeleccionado);

                if (!activo) {
                    return;
                }

                setDatosPeriodo(data);

                setErrorPeriodo(false);

            } catch (err) {

                console.log(err);

                if (activo) {

                    setErrorPeriodo(true);
                }
            }
        }

        // carga inicial y auto refresh cada 30 segundos
        Promise.all([
            cargarResumen(),
            cargarDashboardPeriodo(periodo)
        ]);

        const interval = setInterval(() => {

            Promise.all([
                cargarResumen(),
                cargarDashboardPeriodo(periodo)
            ]);

        }, 30000);

        // limpiar intervalo al salir
        return () => {

            activo = false;

            clearInterval(interval);
        };

    }, [actualizacion, periodo]);

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

            {/* analitica: grafico 70% + ranking 30% (sincronizados al periodo) */}
            <div className="mb-8 grid grid-cols-1 gap-4 lg:grid-cols-10 lg:gap-5">

                {/* panel izquierdo: pedidos por hora */}
                <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6 lg:col-span-7">

                    <div className="mb-4 flex items-center justify-between gap-3">

                        <h2 className="text-sm font-medium text-slate-500">
                            Pedidos por Hora
                        </h2>

                        <select
                            value={periodo}
                            onChange={(evento) =>
                                setPeriodo(evento.target.value)
                            }
                            aria-label="Periodo de análisis"
                            className="
                                rounded-lg border border-slate-300 bg-white
                                px-2.5 py-1.5 text-xs font-medium text-slate-600
                                transition-colors focus:border-blue-500
                                focus:outline-none focus:ring-1 focus:ring-blue-500
                            "
                        >
                            <option value="dia">Hoy</option>
                            <option value="semana">Semana</option>
                            <option value="mes">{nombreMesActual()}</option>
                        </select>

                    </div>

                    {
                        errorPeriodo && (

                            <p className="py-16 text-center text-sm text-red-600">
                                No se pudo cargar el gráfico
                            </p>
                        )
                    }

                    {
                        !errorPeriodo && !datosPeriodo && (

                            <p className="py-16 text-center text-sm text-slate-400">
                                Cargando gráfico...
                            </p>
                        )
                    }

                    {
                        !errorPeriodo &&
                        datosPeriodo && (
                            <GraficoLineas
                                datos={construirSerieCompleta(
                                    periodo,
                                    datosPeriodo.grafico ?? []
                                )}
                                pasoEtiqueta={periodo === "dia" ? 2 : undefined}
                                clave={periodo}
                            />
                        )
                    }

                </div>

                {/* panel derecho: ranking sincronizado al mismo periodo */}
                <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6 lg:col-span-3">

                    <h2 className="mb-4 text-sm font-medium text-slate-500">
                        Pedidos por Empleado
                    </h2>

                    {
                        errorPeriodo && (
                            <p className="py-6 text-center text-sm text-red-600">
                                No se pudo cargar el ranking
                            </p>
                        )
                    }

                    {
                        !errorPeriodo && !datosPeriodo && (
                            <p className="py-6 text-center text-sm text-slate-400">
                                Cargando vendedores...
                            </p>
                        )
                    }

                    {
                        !errorPeriodo &&
                        datosPeriodo &&
                        datosPeriodo.topEmpleados?.length === 0 && (
                            <p className="py-6 text-center text-sm text-slate-400">
                                No hay datos disponibles
                            </p>
                        )
                    }

                    {
                        !errorPeriodo &&
                        datosPeriodo &&
                        datosPeriodo.topEmpleados?.length > 0 && (

                            <ol>

                                {datosPeriodo.topEmpleados.map((vendedor, indice) => (

                                    <li
                                        key={vendedor.vendedorId}
                                        className="flex items-center gap-2.5 py-2.5"
                                    >

                                        <span className="w-7 shrink-0 text-center text-sm">
                                            {MEDALLAS[indice] ?? `${indice + 1}.`}
                                        </span>

                                        <span className="truncate text-sm font-medium text-slate-900">
                                            {vendedor.vendedor}
                                        </span>

                                        <span className="min-w-4 flex-1 border-b border-dotted border-slate-300" />

                                        <span className="shrink-0 text-sm font-semibold tabular-nums text-blue-600">
                                            {vendedor.total}
                                        </span>

                                    </li>
                                ))}
                            </ol>
                        )
                    }

                </div>

            </div>

        </div>
    );
}

export default DashboardPage;
