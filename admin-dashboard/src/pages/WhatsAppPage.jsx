import { useEffect, useState } from "react";
import { obtenerEstadoWhatsApp, obtenerQrWhatsApp, desvincularWhatsApp } from "../services/whatsappService";

const ESTADOS = {
    STARTING: {
        titulo: "Iniciando listener",
        descripcion: "El servicio de WhatsApp se está preparando.",
        clase: "bg-blue-100 text-blue-700"
    },
    CONNECTING: {
        titulo: "Conectando",
        descripcion: "WhatsApp está validando la sesión existente.",
        clase: "bg-blue-100 text-blue-700"
    },
    QR_READY: {
        titulo: "Escanea el código QR",
        descripcion: "Abre WhatsApp en tu teléfono y vincula el dispositivo.",
        clase: "bg-amber-100 text-amber-700"
    },
    CONNECTED: {
        titulo: "WhatsApp conectado",
        descripcion: "El listener está listo para recibir pedidos.",
        clase: "bg-emerald-100 text-emerald-700"
    },
    RECONNECTING: {
        titulo: "Reconectando",
        descripcion: "El listener intentará restablecer la conexión automáticamente.",
        clase: "bg-amber-100 text-amber-700"
    },
    LOGGED_OUT: {
        titulo: "Sesión cerrada",
        descripcion: "La sesión de WhatsApp se cerró y requiere una nueva vinculación.",
        clase: "bg-slate-200 text-slate-700"
    },
    ERROR: {
        titulo: "Error de conexión",
        descripcion: "No fue posible preparar la conexión de WhatsApp.",
        clase: "bg-red-100 text-red-700"
    }
};

function WhatsAppPage() {
    const [estado, setEstado] = useState(null);
    const [qr, setQr] = useState(null);
    const [error, setError] = useState(null);
    const [ultimaActualizacion, setUltimaActualizacion] = useState(null);
    const [desvinculando, setDesvinculando] = useState(false);

    const manejarDesvincular = async () => {

        if (desvinculando) {
            return;
        }

        if (!window.confirm("Se cerrará la sesión de WhatsApp y se eliminarán las credenciales locales. Deberás escanear un nuevo código QR. ¿Continuar?")) {
            return;
        }

        setDesvinculando(true);

        try {

            await desvincularWhatsApp();
            setError(null);

        } catch {
            setError("No fue posible desvincular WhatsApp.");
        } finally {
            setDesvinculando(false);
        }
    };

    useEffect(() => {
        let activo = true;

        async function cargarEstado() {
            try {
                const nuevoEstado = await obtenerEstadoWhatsApp();

                if (!activo) {
                    return;
                }

                setEstado(nuevoEstado);
                setError(null);
                setUltimaActualizacion(new Date());

                if (!nuevoEstado.qrAvailable) {
                    setQr(null);
                    return;
                }

                try {
                    const respuestaQr = await obtenerQrWhatsApp();

                    if (activo) {
                        setQr(respuestaQr.qr);
                    }
                } catch (qrError) {
                    if (activo && qrError.response?.status !== 409) {
                        setError("No fue posible obtener el código QR.");
                    }
                }
            } catch {
                if (activo) {
                    setError("No fue posible comunicarse con el listener de WhatsApp.");
                    setQr(null);
                }
            }
        }

        cargarEstado();
        const intervalo = setInterval(cargarEstado, 3000);

        return () => {
            activo = false;
            clearInterval(intervalo);
        };
    }, []);

    const detalleEstado = ESTADOS[estado?.status] || {
        titulo: "Estado no disponible",
        descripcion: "Esperando información del listener de WhatsApp.",
        clase: "bg-slate-200 text-slate-700"
    };

    return (
        <div className="min-h-screen bg-slate-50 px-4 py-6 sm:px-6 lg:px-8">
            <div className="mb-8">
                <div className="flex flex-wrap items-baseline gap-x-3">
                    <span className="text-3xl font-bold text-blue-600 sm:text-4xl">
                        INVENTIA
                    </span>
                    <span className="text-3xl font-bold text-slate-300 sm:text-4xl">|</span>
                    <h1 className="text-3xl font-bold text-slate-900 sm:text-4xl">
                        WhatsApp
                    </h1>
                </div>
                <p className="mt-1 text-sm text-slate-500">
                    Estado de la conexión y vinculación del listener.
                </p>
            </div>

            <div className="grid max-w-5xl gap-6 lg:grid-cols-[1fr_360px]">
                <section className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6">
                    <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
                        <div>
                            <p className="text-sm font-medium text-slate-500">Estado de conexión</p>
                            <h2 className="mt-1 text-2xl font-bold text-slate-900">
                                {detalleEstado.titulo}
                            </h2>
                            <p className="mt-2 max-w-lg text-sm leading-6 text-slate-500">
                                {detalleEstado.descripcion}
                            </p>
                        </div>
                        <span className={`inline-flex w-fit rounded-full px-3 py-1 text-xs font-semibold ${detalleEstado.clase}`}>
                            {estado?.status || "SIN CONEXIÓN"}
                        </span>
                    </div>

                    {error && (
                        <div className="mt-6 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                            {error}
                        </div>
                    )}

                    {estado?.lastError && (
                        <div className="mt-6 rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800">
                            Último error: {estado.lastError}
                        </div>
                    )}

                    <dl className="mt-8 grid gap-4 border-t border-slate-100 pt-6 sm:grid-cols-2">
                        <div>
                            <dt className="text-xs font-medium uppercase tracking-wide text-slate-400">Conectado desde</dt>
                            <dd className="mt-1 text-sm font-medium text-slate-700">
                                {estado?.connectedAt ? new Date(estado.connectedAt).toLocaleString() : "Sin conexión"}
                            </dd>
                        </div>
                        <div>
                            <dt className="text-xs font-medium uppercase tracking-wide text-slate-400">Reintentos</dt>
                            <dd className="mt-1 text-sm font-medium text-slate-700">
                                {estado?.retryCount ?? 0}
                            </dd>
                        </div>
                    </dl>

                    <div className="mt-6 flex justify-end border-t border-slate-100 pt-4">
                        <button
                            type="button"
                            onClick={manejarDesvincular}
                            disabled={desvinculando}
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
                                focus:ring-red-500
                                focus:ring-offset-2
                                disabled:cursor-not-allowed
                                disabled:opacity-50
                            "
                        >
                            {desvinculando ? "Desvinculando..." : "Desvincular WhatsApp"}
                        </button>
                    </div>

                    {ultimaActualizacion && (
                        <p className="mt-6 text-xs text-slate-400">
                            Actualizado {ultimaActualizacion.toLocaleTimeString()}. Se actualiza cada 3 segundos.
                        </p>
                    )}
                </section>

                <section className="flex min-h-80 flex-col items-center justify-center rounded-xl border border-slate-200 bg-white p-5 text-center shadow-sm sm:p-6">
                    {qr ? (
                        <>
                            <h2 className="text-lg font-semibold text-slate-900">Vincular WhatsApp</h2>
                            <p className="mt-1 text-sm text-slate-500">
                                Escanea este código desde WhatsApp en tu teléfono.
                            </p>
                            <img
                                src={qr}
                                alt="Código QR para vincular WhatsApp"
                                className="mt-5 w-full max-w-64 rounded-lg border border-slate-200 p-3"
                            />
                        </>
                    ) : (
                        <>
                            <div className={`flex h-14 w-14 items-center justify-center rounded-full text-xl font-bold ${detalleEstado.clase}`}>
                                WA
                            </div>
                            <h2 className="mt-4 text-lg font-semibold text-slate-900">
                                {estado?.status === "QR_READY" ? "Generando código QR" : "Sin código QR disponible"}
                            </h2>
                            <p className="mt-2 max-w-64 text-sm leading-6 text-slate-500">
                                {estado?.status === "CONNECTED"
                                    ? "La cuenta ya está vinculada correctamente."
                                    : "El código aparecerá aquí cuando WhatsApp solicite vincular un dispositivo."}
                            </p>
                        </>
                    )}
                </section>
            </div>
        </div>
    );
}

export default WhatsAppPage;
