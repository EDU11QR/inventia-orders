import { useEffect, useState } from "react";
import { obtenerPedidos, obtenerVendedores, exportarPedidosExcel, actualizarEstadoPedido, cancelarPedido, editarPedido, generarPdf, imprimirPedido } from "../services/pedidoService";
import EditarPedidoModal from "../components/EditarPedidoModal";

function PedidosPage() {

    const [pedidos, setPedidos] = useState([]);

    const [exportandoExcel, setExportandoExcel] = useState(false);

    // modal exportar excel
    const [modalExcelOpen, setModalExcelOpen] = useState(false);

    // rango de fechas exportación
    const [fechaInicio, setFechaInicio] = useState("");

    const [fechaFin, setFechaFin] = useState("");

    // búsqueda
    const [busqueda, setBusqueda] = useState("");

    // filtro estado
    const [filtroEstado, setFiltroEstado] = useState("TODOS");

    // filtro vendedor ("TODOS" o vendedorId)
    const [filtroVendedor, setFiltroVendedor] = useState("TODOS");

    // vendedores con pedidos para el selector
    const [vendedores, setVendedores] = useState([]);

    // modal editar
    const [modalOpen, setModalOpen] = useState(false);

    // pedido seleccionado
    const [pedidoSeleccionado, setPedidoSeleccionado] = useState(null);

    // modal cancelación
    const [modalCancelacionOpen, setModalCancelacionOpen] = useState(false);

    // pedido a cancelar
    const [pedidoCancelar, setPedidoCancelar] = useState(null);

    // motivo cancelación
    const [motivoCancelacion, setMotivoCancelacion] = useState("");

    // página actual
    const [paginaActual, setPaginaActual] = useState(1);

    // cantidad de filas por página
    const filasPorPagina = 10;

    const cargarPedidos = async () => {

        try {

            const data = await obtenerPedidos();

            setPedidos(data);

        } catch (error) {

            console.log(error);
        }
    };

    const cargarVendedores = async () => {

        try {

            const data = await obtenerVendedores();

            setVendedores(data);

        } catch (error) {

            console.log(error);
        }
    };

    useEffect(() => {

        // cargar pedidos inicialmente
        cargarPedidos();

        // cargar vendedores para el selector
        cargarVendedores();

        // auto refresh cada 5 segundos
        const interval = setInterval(() => {

            cargarPedidos();

        }, 3000);

        // limpiar intervalo al salir
        return () => clearInterval(interval);

    }, []);

    const abrirModalExcel = () => {

        setFechaInicio("");

        setFechaFin("");

        setModalExcelOpen(true);
    };

    const confirmarExportacion = async () => {

        if (!fechaInicio || !fechaFin) {

            alert(
                "Debe seleccionar la fecha de inicio y la fecha de fin"
            );

            return;
        }

        if (fechaInicio > fechaFin) {

            alert(
                "La fecha de inicio no puede ser posterior a la fecha de fin"
            );

            return;
        }

        if (exportandoExcel) {
            return;
        }

        setExportandoExcel(true);

        try {

            // la exportación respeta exactamente
            // los filtros activos en pantalla
            const response = await exportarPedidosExcel(
                fechaInicio,
                fechaFin,
                {
                    vendedorId:
                        filtroVendedor === "TODOS"
                            ? null
                            : filtroVendedor,

                    estado:
                        filtroEstado === "TODOS"
                            ? null
                            : filtroEstado,

                    busqueda: busqueda.trim() || null
                }
            );

            if (response.status === 404) {

                alert(
                    "No se encontraron pedidos en el rango de fechas seleccionado."
                );

                return;
            }

            const data = response.data;
            const url = window.URL.createObjectURL(
                new Blob([data], {
                    type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                })
            );
            const link = document.createElement("a");

            link.href = url;
            link.setAttribute(
                "download",
                `pedidos_${fechaInicio}_al_${fechaFin}.xlsx`
            );

            document.body.appendChild(link);
            link.click();
            link.remove();
            setTimeout(() => window.URL.revokeObjectURL(url), 100);

            setModalExcelOpen(false);

        } catch (error) {

            console.log(error);

            alert("Error exportando pedidos a Excel");

        } finally {

            setExportandoExcel(false);
        }
    };

    // cambiar estado del pedido
    const cambiarEstado = async (id, estado) => {

        try {

            // ==========================
            // cancelar pedido
            // ==========================

            if (estado === "CANCELADO") {

                setPedidoCancelar(id);

                setMotivoCancelacion("");

                setModalCancelacionOpen(true);

                return;
            }

            // ==========================
            // cambios normales
            // ==========================

            await actualizarEstadoPedido(
                id,
                estado
            );

            cargarPedidos();

        } catch (error) {

            console.log(error);
        }
    };

    // ====================================
    // confirmar cancelación pedido
    // ====================================

    const confirmarCancelacion = async () => {

        try {

            if (!motivoCancelacion.trim()) {

                alert(
                    "Debe ingresar un motivo de cancelación"
                );

                return;
            }

            await cancelarPedido(

                pedidoCancelar,

                motivoCancelacion
            );

            setModalCancelacionOpen(false);

            setPedidoCancelar(null);

            setMotivoCancelacion("");

            cargarPedidos();

        } catch (error) {

            console.log(error);
        }
    };

    // abrir modal editar
    const abrirModalEditar = (pedido) => {

        setPedidoSeleccionado(pedido);

        setModalOpen(true);
    };

    // guardar cambios del pedido
    const guardarEdicion = async (pedidoEditado) => {

        try {

            await editarPedido(

                pedidoSeleccionado.id,
                pedidoEditado
            );

            // cerrar modal
            setModalOpen(false);

            // recargar pedidos
            cargarPedidos();

        } catch (error) {

            console.log(error);
        }
    };

    /*// generar y descargar pdf
    const descargarPdf = async () => {

        try {

            const data = await generarPdf();

            // crear url temporal
            const url =
                window.URL.createObjectURL(
                    new Blob([data])
                );

            // crear enlace descarga
            const link =
                document.createElement("a");

            link.href = url;

            link.setAttribute(
                "download",
                "pedidos.pdf"
            );

            document.body.appendChild(link);

            link.click();

            link.remove();

            // refrescar pedidos
            cargarPedidos();

        } catch (error) {

            console.log(error);
        }
    };*/

    /*const descargarPdf = async () => {

        try {

            const mensaje = await generarPdf();

            alert(mensaje);

            // refrescar pedidos
            cargarPedidos();

        } catch (error) {

            console.log(error);

            alert("Error generando tickets");
        }
    };*/

    const descargarPdf = async () => {

        try {

            const data = await generarPdf();

            // crear archivo blob
            const url =
                window.URL.createObjectURL(
                    new Blob([data])
                );

            // crear enlace descarga
            const link =
                document.createElement("a");

            link.href = url;

            // nombre descarga
            link.setAttribute(
                "download",
                "etiqueta.pdf"
            );

            document.body.appendChild(link);

            link.click();

            link.remove();

            // actualizar tabla
            cargarPedidos();

        } catch (error) {

            console.log(error);

            alert("Error generando PDF");
        }
    };

    // ==========================================
    // imprimir pedido individual
    // ==========================================

    const handleImprimir = async (id) => {

        try {

            // ==============================
            // solicitar pdf backend
            // ==============================

            const pdfBlob = await imprimirPedido(id);

            // ==============================
            // crear url temporal
            // ==============================

            const url = window.URL.createObjectURL(
                pdfBlob
            );
            // ==============================
            // crear enlace descarga
            // ==============================

            const link = document.createElement("a");

            link.href = url;

            link.setAttribute(
                "download",
                `pedido_${id}.pdf`
            );

            // ==============================
            // descargar automáticamente
            // ==============================

            document.body.appendChild(link);

            link.click();

            link.remove();

            // ==============================
            // refrescar pedidos
            // ==============================

            cargarPedidos();

        } catch (error) {

            console.log(error);

            alert(
                "Error imprimiendo pedido"
            );
        }
    };


    const contarPedidosPorEstado = (estado) =>
        pedidos.filter(pedido => pedido.estado === estado).length;

    const pendientes = contarPedidosPorEstado("PENDIENTE");

    const impresos = contarPedidosPorEstado("IMPRESO");

    const cancelados = contarPedidosPorEstado("CANCELADO");

    // filtrar pedidos (filtros acumulativos)
    const pedidosFiltrados = pedidos.filter((pedido) => {

        // buscar por cliente o producto
        const coincideBusqueda =

            pedido.cliente
                .toLowerCase()
                .includes(busqueda.toLowerCase())

            ||

            pedido.producto
                .toLowerCase()
                .includes(busqueda.toLowerCase());

        // filtro por estado
        const coincideEstado =

            filtroEstado === "TODOS"
                ? true
                : pedido.estado === filtroEstado;

        // filtro por vendedor (identidad real vendedorId)
        const coincideVendedor =

            filtroVendedor === "TODOS"
                ? true
                : pedido.vendedorId === filtroVendedor;

        return coincideBusqueda && coincideEstado && coincideVendedor;
    });

    // índices de paginación
    const indiceUltimaFila =
        paginaActual * filasPorPagina;

    const indicePrimeraFila =
        indiceUltimaFila - filasPorPagina;

    // pedidos visibles
    const pedidosPaginados =
        pedidosFiltrados.slice(
            indicePrimeraFila,
            indiceUltimaFila
        );

    // total páginas
    const totalPaginas =
        Math.ceil(
            pedidosFiltrados.length / filasPorPagina
        );

    return (

        <div className="min-h-screen bg-slate-50 px-4 py-6 sm:px-6 lg:px-8">

            {/* titulo */}
            <div className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">

                <div>

                    <div className="flex flex-wrap items-baseline gap-x-3">

                        <span className="text-3xl font-bold text-blue-600 sm:text-4xl">
                            INVENTIA
                        </span>

                        <span className="text-3xl font-bold text-slate-300 sm:text-4xl">
                            |
                        </span>

                        <h1 className="text-3xl font-bold text-slate-900 sm:text-4xl">
                            Gestión de pedidos
                        </h1>

                    </div>

                    <p className="mt-1 text-sm text-slate-500">
                        Registros de pedidos recibidos por WhatsApp
                    </p>

                </div>

                <button
                    onClick={abrirModalExcel}
                    className="
                        inline-flex
                        items-center
                        justify-center
                        gap-2
                        rounded-lg
                        bg-blue-600
                        px-5
                        py-2.5
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
                    Exportar Excel
                </button>

                {/*<button
                    onClick={descargarPdf}
                    className="
                        bg-red-600
                        text-white
                        px-5
                        py-3
                        rounded-xl
                        hover:bg-red-700
                        shadow
                    "
                >
                    Generar PDF
                </button> */}

            </div>

            {/* modal exportar excel */}
            {
                modalExcelOpen && (

                    <div
                        className="
                            fixed
                            inset-0
                            bg-slate-900/40
                            flex
                            items-center
                            justify-center
                            z-50
                        "
                    >

                        <div
                            className="
                                w-full
                                max-w-md
                                rounded-xl
                                border
                                border-slate-200
                                bg-white
                                p-6
                                shadow-xl
                            "
                        >

                            <h2 className="mb-1 text-lg font-semibold text-slate-900">
                                Exportar Pedidos a Excel
                            </h2>

                            <p className="mb-5 text-sm text-slate-500">
                                Seleccione el rango de fechas
                            </p>

                            <div className="flex flex-col gap-4">

                                <label>

                                    <span className="mb-1.5 block text-sm font-medium text-slate-600">
                                        Fecha de inicio
                                    </span>

                                    <input
                                        type="date"
                                        value={fechaInicio}
                                        onChange={(e) =>
                                            setFechaInicio(e.target.value)
                                        }
                                        className="
                                            w-full
                                            rounded-lg
                                            border
                                            border-slate-300
                                            bg-white
                                            px-3
                                            py-2.5
                                            text-sm
                                            text-slate-900
                                            shadow-sm
                                            transition
                                            focus:border-blue-500
                                            focus:outline-none
                                            focus:ring-2
                                            focus:ring-blue-500/20
                                        "
                                    />

                                </label>

                                <label>

                                    <span className="mb-1.5 block text-sm font-medium text-slate-600">
                                        Fecha de fin
                                    </span>

                                    <input
                                        type="date"
                                        value={fechaFin}
                                        onChange={(e) =>
                                            setFechaFin(e.target.value)
                                        }
                                        className="
                                            w-full
                                            rounded-lg
                                            border
                                            border-slate-300
                                            bg-white
                                            px-3
                                            py-2.5
                                            text-sm
                                            text-slate-900
                                            shadow-sm
                                            transition
                                            focus:border-blue-500
                                            focus:outline-none
                                            focus:ring-2
                                            focus:ring-blue-500/20
                                        "
                                    />

                                </label>

                            </div>

                            <div
                                className="
                                    mt-6
                                    flex
                                    justify-end
                                    gap-3
                                "
                            >

                                <button
                                    onClick={() =>
                                        setModalExcelOpen(false)
                                    }
                                    className="
                                        inline-flex
                                        items-center
                                        justify-center
                                        rounded-lg
                                        border
                                        border-slate-300
                                        bg-white
                                        px-4
                                        py-2.5
                                        text-sm
                                        font-medium
                                        text-slate-700
                                        shadow-sm
                                        transition-colors
                                        hover:bg-slate-50
                                        focus:outline-none
                                        focus:ring-2
                                        focus:ring-blue-500/20
                                    "
                                >
                                    Cerrar
                                </button>

                                <button
                                    onClick={confirmarExportacion}
                                    disabled={exportandoExcel}
                                    className="
                                        inline-flex
                                        items-center
                                        justify-center
                                        rounded-lg
                                        bg-blue-600
                                        px-4
                                        py-2.5
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
                                        disabled:cursor-not-allowed
                                        disabled:opacity-50
                                    "
                                >
                                    {exportandoExcel
                                        ? "Exportando..."
                                        : "Exportar"}
                                </button>

                            </div>

                        </div>

                    </div>
                )
            }

            {/* cards */}
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
                        {pendientes}
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
                        {impresos}
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
                        {cancelados}
                    </p>
                </div>

            </div>

            {/* filtros */}
            <div className="mb-6 flex flex-col gap-3 sm:flex-row sm:gap-4">

                {/* buscador */}
                <input
                    type="text"
                    placeholder="Buscar cliente o producto..."
                    value={busqueda}
                    onChange={(e) =>
                        setBusqueda(e.target.value)
                    }
                    className="
                        h-11
                        w-full
                        rounded-lg
                        border
                        border-slate-300
                        bg-white
                        px-4
                        text-sm
                        text-slate-900
                        placeholder-slate-400
                        shadow-sm
                        transition
                        focus:border-blue-500
                        focus:outline-none
                        focus:ring-2
                        focus:ring-blue-500/20
                    "
                />

                {/* filtro estado */}
                <select
                    value={filtroEstado}
                    onChange={(e) =>
                        setFiltroEstado(e.target.value)
                    }
                    className="
                        h-11
                        w-full
                        rounded-lg
                        border
                        border-slate-300
                        bg-white
                        px-4
                        text-sm
                        text-slate-700
                        shadow-sm
                        transition
                        focus:border-blue-500
                        focus:outline-none
                        focus:ring-2
                        focus:ring-blue-500/20
                        sm:w-48
                    "
                >
                    <option value="TODOS">
                        Todos
                    </option>

                    <option value="PENDIENTE">
                        Pendientes
                    </option>

                    <option value="IMPRESO">
                        Impresos
                    </option>

                    <option value="CANCELADO">
                        Cancelados
                    </option>

                </select>

                {/* filtro vendedor */}
                <select
                    value={filtroVendedor}
                    onChange={(e) => {

                        setFiltroVendedor(e.target.value);

                        // volver a la primera página
                        setPaginaActual(1);
                    }}
                    aria-label="Filtrar por vendedor"
                    className="
                        h-11
                        w-full
                        rounded-lg
                        border
                        border-slate-300
                        bg-white
                        px-4
                        text-sm
                        text-slate-700
                        shadow-sm
                        transition
                        focus:border-blue-500
                        focus:outline-none
                        focus:ring-2
                        focus:ring-blue-500/20
                        sm:w-48
                    "
                >
                    <option value="TODOS">
                        Todos
                    </option>

                    {vendedores.map((vendedor) => (
                        <option
                            key={vendedor.vendedorId}
                            value={vendedor.vendedorId}
                        >
                            {vendedor.vendedor}
                        </option>
                    ))}

                </select>

            </div>

            {/* tabla */}
            <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">

                <div className="overflow-x-auto">

                <table className="w-full min-w-[760px]">

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

                            <th className="whitespace-nowrap px-5 py-3.5 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                                Acciones
                            </th>

                        </tr>

                    </thead>

                    <tbody>

                        {
                            pedidosPaginados.map((pedido, index) => (

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

                                    <td className="whitespace-nowrap px-5 py-4">

                                        <div className="flex gap-2">

                                            <button
                                                onClick={() =>
                                                    abrirModalEditar(pedido)
                                                }
                                                className="
                                                    inline-flex
                                                    items-center
                                                    justify-center
                                                    rounded-lg
                                                    border
                                                    border-blue-200
                                                    bg-white
                                                    px-3
                                                    py-1.5
                                                    text-sm
                                                    font-medium
                                                    text-blue-700
                                                    transition-colors
                                                    hover:border-blue-300
                                                    hover:bg-blue-50
                                                    focus:outline-none
                                                    focus:ring-2
                                                    focus:ring-blue-500/20
                                                "
                                            >
                                                Editar
                                            </button>



                                            {
                                                pedido.estado === "PENDIENTE" && (

                                                    <select
                                                        defaultValue=""
                                                        onChange={(e) =>
                                                            cambiarEstado(
                                                                pedido.id,
                                                                e.target.value
                                                            )
                                                        }
                                                        className="
                                                            h-9
                                                            rounded-lg
                                                            border
                                                            border-slate-300
                                                            bg-white
                                                            px-2
                                                            text-sm
                                                            text-slate-700
                                                            transition
                                                            focus:border-blue-500
                                                            focus:outline-none
                                                            focus:ring-2
                                                            focus:ring-blue-500/20
                                                        "
                                                    >
                                                        <option value="" disabled>
                                                            Pendiente
                                                        </option>

                                                        <option value="IMPRESO">
                                                            Marcar Impreso
                                                        </option>

                                                        <option value="CANCELADO">
                                                            Cancelar
                                                        </option>

                                                    </select>
                                                )
                                            }

                                            {
                                                pedido.estado === "IMPRESO" && (

                                                    <select
                                                        defaultValue=""
                                                        onChange={(e) =>
                                                            cambiarEstado(
                                                                pedido.id,
                                                                e.target.value
                                                            )
                                                        }
                                                        className="
                                                            h-9
                                                            rounded-lg
                                                            border
                                                            border-slate-300
                                                            bg-white
                                                            px-2
                                                            text-sm
                                                            text-slate-700
                                                            transition
                                                            focus:border-blue-500
                                                            focus:outline-none
                                                            focus:ring-2
                                                            focus:ring-blue-500/20
                                                        "
                                                    >
                                                        <option value="" disabled>
                                                            Impreso
                                                        </option>

                                                        <option value="CANCELADO">
                                                            Cancelar
                                                        </option>

                                                    </select>
                                                )
                                            }

                                            {
                                                pedido.estado === "CANCELADO" && (

                                                    <span
                                                        className="
                                                            inline-flex
                                                            items-center
                                                            rounded-lg
                                                            border
                                                            border-red-200
                                                            bg-red-50
                                                            px-3
                                                            py-1.5
                                                            text-sm
                                                            font-medium
                                                            text-red-600
                                                        "
                                                    >
                                                        Cancelado
                                                    </span>
                                                )
                                            }

                                            {
                                                pedido.estado !== "CANCELADO" && (

                                                    <button
                                                        onClick={() =>
                                                            handleImprimir(pedido.id)
                                                        }
                                                        className="
                                                            inline-flex
                                                            items-center
                                                            justify-center
                                                            gap-1.5
                                                            rounded-lg
                                                            bg-blue-600
                                                            px-3
                                                            py-1.5
                                                            text-sm
                                                            font-medium
                                                            text-white
                                                            shadow-sm
                                                            transition-colors
                                                            hover:bg-blue-700
                                                            focus:outline-none
                                                            focus:ring-2
                                                            focus:ring-blue-500/30
                                                        "
                                                    >
                                                        🖨 Imprimir
                                                    </button>
                                                )
                                            }
                                        </div>

                                    </td>

                                </tr>
                            ))
                        }

                    </tbody>

                </table>

                </div>

                {/* paginación */}
                <div
                    className="
                        flex
                        flex-col
                        items-center
                        justify-between
                        gap-3
                        border-t
                        border-slate-200
                        bg-slate-50
                        px-5
                        py-4
                        sm:flex-row
                    "
                >

                    <p className="text-sm text-slate-500">

                        Página {paginaActual} de {totalPaginas}

                    </p>

                    <div className="flex gap-2">

                        {/* anterior */}
                        <button
                            disabled={paginaActual === 1}
                            onClick={() =>
                                setPaginaActual(
                                    paginaActual - 1
                                )
                            }
                            className="
                                inline-flex
                                items-center
                                justify-center
                                rounded-lg
                                border
                                border-slate-300
                                bg-white
                                px-4
                                py-2
                                text-sm
                                font-medium
                                text-slate-700
                                shadow-sm
                                transition-colors
                                hover:bg-slate-100
                                focus:outline-none
                                focus:ring-2
                                focus:ring-blue-500/20
                                disabled:cursor-not-allowed
                                disabled:opacity-50
                            "
                        >
                            Anterior
                        </button>

                        {/* siguiente */}
                        <button
                            disabled={
                                paginaActual === totalPaginas
                            }
                            onClick={() =>
                                setPaginaActual(
                                    paginaActual + 1
                                )
                            }
                            className="
                                inline-flex
                                items-center
                                justify-center
                                rounded-lg
                                bg-blue-600
                                px-4
                                py-2
                                text-sm
                                font-medium
                                text-white
                                shadow-sm
                                transition-colors
                                hover:bg-blue-700
                                focus:outline-none
                                focus:ring-2
                                focus:ring-blue-500/30
                                disabled:cursor-not-allowed
                                disabled:opacity-50
                            "
                        >
                            Siguiente
                        </button>

                    </div>

                </div>

            </div>

            {/* modal editar pedido */}
            {
                modalOpen && pedidoSeleccionado && (

                    <EditarPedidoModal

                        pedido={pedidoSeleccionado}

                        onClose={() =>
                            setModalOpen(false)
                        }

                        onGuardar={guardarEdicion}
                    />
                )
            }

            {/* modal cancelar pedido */}
            {
                modalCancelacionOpen && (

                    <div
                        className="
                            fixed
                            inset-0
                            bg-black/30
                            flex
                            items-center
                            justify-center
                            z-50
                        "
                    >

                        <div
                            className="
                                w-full
                                max-w-md
                                rounded-xl
                                border
                                border-slate-200
                                bg-white
                                p-6
                                shadow-xl
                            "
                        >

                            <h2 className="mb-1 text-lg font-semibold text-slate-900">
                                Cancelar Pedido
                            </h2>

                            <p className="mb-5 text-sm text-slate-500">
                                Ingrese el motivo de cancelación
                            </p>

                            <textarea
                                value={motivoCancelacion}
                                onChange={(e) =>
                                    setMotivoCancelacion(
                                        e.target.value
                                    )
                                }
                                rows="4"
                                className="
                                    w-full
                                    rounded-lg
                                    border
                                    border-slate-300
                                    px-3
                                    py-2.5
                                    text-sm
                                    text-slate-900
                                    shadow-sm
                                    transition
                                    focus:border-blue-500
                                    focus:outline-none
                                    focus:ring-2
                                    focus:ring-blue-500/20
                                "
                                placeholder="Ejemplo: Cliente canceló compra"
                            />

                            <div
                                className="
                                    mt-6
                                    flex
                                    justify-end
                                    gap-3
                                "
                            >

                                <button
                                    onClick={() => {

                                        setModalCancelacionOpen(false);

                                        setMotivoCancelacion("");

                                        setPedidoCancelar(null);
                                    }}
                                    className="
                                        inline-flex
                                        items-center
                                        justify-center
                                        rounded-lg
                                        border
                                        border-slate-300
                                        bg-white
                                        px-4
                                        py-2.5
                                        text-sm
                                        font-medium
                                        text-slate-700
                                        shadow-sm
                                        transition-colors
                                        hover:bg-slate-50
                                        focus:outline-none
                                        focus:ring-2
                                        focus:ring-blue-500/20
                                    "
                                >
                                    Cerrar
                                </button>

                                <button
                                    onClick={confirmarCancelacion}
                                    className="
                                        inline-flex
                                        items-center
                                        justify-center
                                        rounded-lg
                                        bg-red-600
                                        px-4
                                        py-2.5
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
                                    Confirmar Cancelación
                                </button>

                            </div>

                        </div>

                    </div>
                )
            }

            {/* Pie de pagina */}

            <div
                className="
                    mt-8
                    rounded-lg
                    bg-slate-800
                    px-6
                    py-4
                    text-center
                "
            >

                <p className="text-sm text-slate-300">

                    @2026 Inventia | Todos los derechos reservados. || desarrollado por : <a href="https://github.com/EDU11QR" target="_blank" className="text-blue-400 hover:underline">@Edudev</a>

                </p>

            </div>

        </div>
    );
}

export default PedidosPage;
