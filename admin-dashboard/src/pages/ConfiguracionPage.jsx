import { useEffect, useState } from "react";
import {
    obtenerConfiguracionEmpresa,
    guardarConfiguracionEmpresa
} from "../services/configuracionService";

function ConfiguracionPage() {

    const [nombreEmpresa, setNombreEmpresa] = useState("");
    const [telefono, setTelefono] = useState("");
    const [direccion, setDireccion] = useState("");
    const [mensajeTicket, setMensajeTicket] = useState("");
    const [mostrarTelefono, setMostrarTelefono] = useState(false);
    const [mostrarDireccion, setMostrarDireccion] = useState(false);
    const [mostrarMensaje, setMostrarMensaje] = useState(false);

    const [cargando, setCargando] = useState(true);
    const [errorCarga, setErrorCarga] = useState(false);
    const [guardando, setGuardando] = useState(false);
    const [mensajeExito, setMensajeExito] = useState("");
    const [mensajeError, setMensajeError] = useState("");

    useEffect(() => {

        let activo = true;

        async function cargarConfiguracion() {
            try {
                const data = await obtenerConfiguracionEmpresa();
                if (!activo) return;

                setNombreEmpresa(data.nombreEmpresa ?? "");
                setTelefono(data.telefono ?? "");
                setDireccion(data.direccion ?? "");
                setMensajeTicket(data.mensajeTicket ?? "");
                setMostrarTelefono(data.mostrarTelefono ?? false);
                setMostrarDireccion(data.mostrarDireccion ?? false);
                setMostrarMensaje(data.mostrarMensaje ?? false);

            } catch (err) {
                console.log(err);
                if (activo) setErrorCarga(true);
            } finally {
                if (activo) setCargando(false);
            }
        }

        cargarConfiguracion();
        return () => { activo = false; };
    }, []);

    const guardarCambios = async () => {

        setMensajeExito("");
        setMensajeError("");

        if (!nombreEmpresa.trim()) {
            setMensajeError("El nombre de la empresa es obligatorio");
            return;
        }

        setGuardando(true);

        try {
            await guardarConfiguracionEmpresa({
                nombreEmpresa: nombreEmpresa.trim(),
                telefono: telefono.trim(),
                direccion: direccion.trim(),
                mensajeTicket: mensajeTicket.trim(),
                mostrarTelefono,
                mostrarDireccion,
                mostrarMensaje
            });

            setNombreEmpresa(nombreEmpresa.trim());
            setTelefono(telefono.trim());
            setDireccion(direccion.trim());
            setMensajeTicket(mensajeTicket.trim());

            setMensajeExito("Configuración actualizada correctamente");

        } catch (err) {
            console.log(err);
            const detalle = err?.response?.data;
            setMensajeError(
                typeof detalle === "string" && detalle
                    ? detalle
                    : "No se pudo guardar la configuración"
            );
        } finally {
            setGuardando(false);
        }
    };

    const inputClass = `
        w-full rounded-lg border border-slate-300 bg-white
        px-3 py-2.5 text-sm text-slate-900 shadow-sm
        transition focus:border-blue-500 focus:outline-none
        focus:ring-2 focus:ring-blue-500/20
    `;

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
                            Configuración Empresa
                        </h1>
                    </div>
                    <p className="mt-1 text-sm text-slate-500">
                        Personaliza los datos de tu empresa y el formato del ticket
                    </p>
                </div>
            </div>

            {cargando && (
                <p className="py-10 text-center text-sm text-slate-400">
                    Cargando configuración...
                </p>
            )}

            {errorCarga && (
                <p className="py-10 text-center text-sm text-red-600">
                    No se pudo cargar la configuración
                </p>
            )}

            {!cargando && !errorCarga && (
                <div className="mx-auto max-w-2xl space-y-6">

                    {/* SECCION: Información Empresa */}
                    <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6">
                        <h2 className="text-lg font-semibold text-slate-900">
                            Información de la Empresa
                        </h2>
                        <p className="mt-1 text-sm text-slate-500">
                            Datos que aparecen en tickets y documentos
                        </p>

                        <div className="mt-5 space-y-4">
                            <label className="block">
                                <span className="mb-1.5 block text-sm font-medium text-slate-600">
                                    Nombre de la empresa *
                                </span>
                                <input
                                    type="text"
                                    value={nombreEmpresa}
                                    maxLength={100}
                                    onChange={(e) => {
                                        setNombreEmpresa(e.target.value);
                                        setMensajeExito("");
                                        setMensajeError("");
                                    }}
                                    placeholder="INVENTIA"
                                    className={inputClass}
                                />
                            </label>

                            <label className="block">
                                <span className="mb-1.5 block text-sm font-medium text-slate-600">
                                    Teléfono
                                </span>
                                <input
                                    type="text"
                                    value={telefono}
                                    maxLength={30}
                                    onChange={(e) => {
                                        setTelefono(e.target.value);
                                        setMensajeExito("");
                                        setMensajeError("");
                                    }}
                                    placeholder="+51 999888777"
                                    className={inputClass}
                                />
                            </label>

                            <label className="block">
                                <span className="mb-1.5 block text-sm font-medium text-slate-600">
                                    Dirección
                                </span>
                                <input
                                    type="text"
                                    value={direccion}
                                    maxLength={200}
                                    onChange={(e) => {
                                        setDireccion(e.target.value);
                                        setMensajeExito("");
                                        setMensajeError("");
                                    }}
                                    placeholder="Arequipa - Perú"
                                    className={inputClass}
                                />
                            </label>
                        </div>
                    </div>

                    {/* SECCION: Ticket */}
                    <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6">
                        <h2 className="text-lg font-semibold text-slate-900">
                            Configuración del Ticket
                        </h2>
                        <p className="mt-1 text-sm text-slate-500">
                            Controla qué información se muestra en el ticket PDF
                        </p>

                        <div className="mt-5 space-y-4">
                            <label className="block">
                                <span className="mb-1.5 block text-sm font-medium text-slate-600">
                                    Mensaje pie de ticket
                                </span>
                                <input
                                    type="text"
                                    value={mensajeTicket}
                                    maxLength={255}
                                    onChange={(e) => {
                                        setMensajeTicket(e.target.value);
                                        setMensajeExito("");
                                        setMensajeError("");
                                    }}
                                    placeholder="Gracias por su compra"
                                    className={inputClass}
                                />
                            </label>

                            <div className="space-y-3 pt-2">
                                <label className="flex items-center gap-3 cursor-pointer">
                                    <input
                                        type="checkbox"
                                        checked={mostrarTelefono}
                                        onChange={(e) => {
                                            setMostrarTelefono(e.target.checked);
                                            setMensajeExito("");
                                        }}
                                        className="h-4 w-4 rounded border-slate-300 text-blue-600 focus:ring-blue-500"
                                    />
                                    <span className="text-sm text-slate-700">
                                        Mostrar teléfono en ticket
                                    </span>
                                </label>

                                <label className="flex items-center gap-3 cursor-pointer">
                                    <input
                                        type="checkbox"
                                        checked={mostrarDireccion}
                                        onChange={(e) => {
                                            setMostrarDireccion(e.target.checked);
                                            setMensajeExito("");
                                        }}
                                        className="h-4 w-4 rounded border-slate-300 text-blue-600 focus:ring-blue-500"
                                    />
                                    <span className="text-sm text-slate-700">
                                        Mostrar dirección en ticket
                                    </span>
                                </label>

                                <label className="flex items-center gap-3 cursor-pointer">
                                    <input
                                        type="checkbox"
                                        checked={mostrarMensaje}
                                        onChange={(e) => {
                                            setMostrarMensaje(e.target.checked);
                                            setMensajeExito("");
                                        }}
                                        className="h-4 w-4 rounded border-slate-300 text-blue-600 focus:ring-blue-500"
                                    />
                                    <span className="text-sm text-slate-700">
                                        Mostrar mensaje final
                                    </span>
                                </label>
                            </div>
                        </div>
                    </div>

                    {/* mensajes de resultado */}
                    {mensajeExito && (
                        <p
                            role="status"
                            className="rounded-lg border border-green-200 bg-green-50 px-4 py-3 text-sm font-medium text-green-700"
                        >
                            {mensajeExito}
                        </p>
                    )}

                    {mensajeError && (
                        <p
                            role="alert"
                            className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700"
                        >
                            {mensajeError}
                        </p>
                    )}

                    {/* boton guardar */}
                    <div className="flex justify-end pb-6">
                        <button
                            type="button"
                            onClick={guardarCambios}
                            disabled={guardando}
                            className="
                                inline-flex items-center justify-center
                                rounded-lg bg-blue-600 px-6 py-2.5
                                text-sm font-semibold text-white shadow-sm
                                transition-colors hover:bg-blue-700
                                focus:outline-none focus:ring-2 focus:ring-blue-500
                                focus:ring-offset-2
                                disabled:cursor-not-allowed disabled:opacity-50
                            "
                        >
                            {guardando ? "Guardando..." : "Guardar Configuración"}
                        </button>
                    </div>

                </div>
            )}

        </div>
    );
}

export default ConfiguracionPage;
