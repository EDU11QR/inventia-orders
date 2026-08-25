import { useEffect, useState, useMemo } from "react";
import {
    listarUsuarios,
    crearUsuario,
    actualizarUsuario,
    cambiarPassword,
    activarUsuario,
    desactivarUsuario
} from "../services/usuarioService";

function UsuariosPage() {

    const [usuarios, setUsuarios] = useState([]);
    const [cargando, setCargando] = useState(true);
    const [errorCarga, setErrorCarga] = useState(false);

    const [filtroNombre, setFiltroNombre] = useState("");
    const [filtroUsuario, setFiltroUsuario] = useState("");
    const [filtroRol, setFiltroRol] = useState("");
    const [filtroEstado, setFiltroEstado] = useState("");

    const [modalCrear, setModalCrear] = useState(false);
    const [modalEditar, setModalEditar] = useState(false);
    const [modalPassword, setModalPassword] = useState(false);
    const [usuarioSeleccionado, setUsuarioSeleccionado] = useState(null);

    const [formNombre, setFormNombre] = useState("");
    const [formUsuario, setFormUsuario] = useState("");
    const [formPassword, setFormPassword] = useState("");
    const [formRol, setFormRol] = useState("VENTAS");
    const [formNuevoPassword, setFormNuevoPassword] = useState("");

    const [guardando, setGuardando] = useState(false);
    const [mensajeExito, setMensajeExito] = useState("");
    const [mensajeError, setMensajeError] = useState("");

    const cargarUsuarios = async () => {
        setCargando(true);
        setErrorCarga(false);
        try {
            const data = await listarUsuarios();
            setUsuarios(data);
        } catch (err) {
            console.error(err);
            setErrorCarga(true);
        } finally {
            setCargando(false);
        }
    };

    useEffect(() => {
        cargarUsuarios();
    }, []);

    const usuariosFiltrados = useMemo(() => {
        return usuarios.filter((u) => {
            const nombreMatch = u.nombre.toLowerCase().includes(filtroNombre.toLowerCase());
            const usuarioMatch = u.usuario.toLowerCase().includes(filtroUsuario.toLowerCase());
            const rolMatch = !filtroRol || u.rol === filtroRol;
            const estadoMatch =
                filtroEstado === "" ||
                (filtroEstado === "activo" && u.activo) ||
                (filtroEstado === "inactivo" && !u.activo);
            return nombreMatch && usuarioMatch && rolMatch && estadoMatch;
        });
    }, [usuarios, filtroNombre, filtroUsuario, filtroRol, filtroEstado]);

    const abrirModalCrear = () => {
        setFormNombre("");
        setFormUsuario("");
        setFormPassword("");
        setFormRol("VENTAS");
        setMensajeError("");
        setMensajeExito("");
        setModalCrear(true);
    };

    const abrirModalEditar = (usuario) => {
        setUsuarioSeleccionado(usuario);
        setFormNombre(usuario.nombre);
        setFormRol(usuario.rol);
        setMensajeError("");
        setMensajeExito("");
        setModalEditar(true);
    };

    const abrirModalPassword = (usuario) => {
        setUsuarioSeleccionado(usuario);
        setFormNuevoPassword("");
        setMensajeError("");
        setMensajeExito("");
        setModalPassword(true);
    };

    const cerrarModales = () => {
        setModalCrear(false);
        setModalEditar(false);
        setModalPassword(false);
        setUsuarioSeleccionado(null);
        setMensajeError("");
    };

    const handleCrear = async () => {
        setMensajeError("");
        setMensajeExito("");

        if (!formNombre.trim()) {
            setMensajeError("El nombre es obligatorio");
            return;
        }
        if (!formUsuario.trim()) {
            setMensajeError("El usuario es obligatorio");
            return;
        }
        if (formPassword.length < 8) {
            setMensajeError("La contraseña debe tener al menos 8 caracteres");
            return;
        }

        setGuardando(true);
        try {
            await crearUsuario({
                nombre: formNombre.trim(),
                usuario: formUsuario.trim(),
                password: formPassword,
                rol: formRol
            });
            setMensajeExito("Usuario creado correctamente");
            setModalCrear(false);
            await cargarUsuarios();
        } catch (err) {
            const detalle = err?.response?.data;
            setMensajeError(
                typeof detalle === "object" && detalle?.error
                    ? detalle.error
                    : "No se pudo crear el usuario"
            );
        } finally {
            setGuardando(false);
        }
    };

    const handleEditar = async () => {
        setMensajeError("");
        setMensajeExito("");

        if (!formNombre.trim()) {
            setMensajeError("El nombre es obligatorio");
            return;
        }

        setGuardando(true);
        try {
            await actualizarUsuario(usuarioSeleccionado.id, {
                nombre: formNombre.trim(),
                rol: formRol
            });
            setMensajeExito("Usuario actualizado correctamente");
            setModalEditar(false);
            await cargarUsuarios();
        } catch (err) {
            const detalle = err?.response?.data;
            setMensajeError(
                typeof detalle === "object" && detalle?.error
                    ? detalle.error
                    : "No se pudo actualizar el usuario"
            );
        } finally {
            setGuardando(false);
        }
    };

    const handlePassword = async () => {
        setMensajeError("");
        setMensajeExito("");

        if (formNuevoPassword.length < 8) {
            setMensajeError("La contraseña debe tener al menos 8 caracteres");
            return;
        }

        setGuardando(true);
        try {
            await cambiarPassword(usuarioSeleccionado.id, formNuevoPassword);
            setMensajeExito("Contraseña actualizada correctamente");
            setModalPassword(false);
        } catch (err) {
            const detalle = err?.response?.data;
            setMensajeError(
                typeof detalle === "object" && detalle?.error
                    ? detalle.error
                    : "No se pudo cambiar la contraseña"
            );
        } finally {
            setGuardando(false);
        }
    };

    const handleDesactivar = async (usuario) => {
        if (!confirm(`¿Desactivar a ${usuario.nombre}?`)) return;

        try {
            await desactivarUsuario(usuario.id);
            setMensajeExito("Usuario desactivado correctamente");
            await cargarUsuarios();
        } catch (err) {
            const detalle = err?.response?.data;
            alert(
                typeof detalle === "object" && detalle?.error
                    ? detalle.error
                    : "No se pudo desactivar el usuario"
            );
        }
    };

    const handleActivar = async (usuario) => {
        try {
            await activarUsuario(usuario.id);
            setMensajeExito("Usuario activado correctamente");
            await cargarUsuarios();
        } catch (err) {
            const detalle = err?.response?.data;
            alert(
                typeof detalle === "object" && detalle?.error
                    ? detalle.error
                    : "No se pudo activar el usuario"
            );
        }
    };

    const inputClass = `
        w-full rounded-lg border border-slate-300 bg-white
        px-3 py-2.5 text-sm text-slate-900 shadow-sm
        transition focus:border-blue-500 focus:outline-none
        focus:ring-2 focus:ring-blue-500/20
    `;

    const btnPrimary = `
        inline-flex items-center justify-center
        rounded-lg bg-blue-600 px-4 py-2
        text-sm font-semibold text-white shadow-sm
        transition-colors hover:bg-blue-700
        focus:outline-none focus:ring-2 focus:ring-blue-500
        focus:ring-offset-2
        disabled:cursor-not-allowed disabled:opacity-50
    `;

    const btnSecondary = `
        inline-flex items-center justify-center
        rounded-lg border border-slate-300 bg-white px-4 py-2
        text-sm font-semibold text-slate-700 shadow-sm
        transition-colors hover:bg-slate-50
        focus:outline-none focus:ring-2 focus:ring-blue-500
        focus:ring-offset-2
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
                            Usuarios
                        </h1>
                    </div>
                    <p className="mt-1 text-sm text-slate-500">
                        Gestiona los usuarios del sistema
                    </p>
                </div>
                <button type="button" onClick={abrirModalCrear} className={btnPrimary}>
                    <svg className="mr-2 h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M12 5v14M5 12h14" />
                    </svg>
                    Nuevo Usuario
                </button>
            </div>

            {/* filtros */}
            <div className="mb-6 grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
                <input
                    type="text"
                    placeholder="Buscar por nombre..."
                    value={filtroNombre}
                    onChange={(e) => setFiltroNombre(e.target.value)}
                    className={inputClass}
                />
                <input
                    type="text"
                    placeholder="Buscar por usuario..."
                    value={filtroUsuario}
                    onChange={(e) => setFiltroUsuario(e.target.value)}
                    className={inputClass}
                />
                <select
                    value={filtroRol}
                    onChange={(e) => setFiltroRol(e.target.value)}
                    className={inputClass}
                >
                    <option value="">Todos los roles</option>
                    <option value="ADMIN">ADMIN</option>
                    <option value="VENTAS">VENTAS</option>
                    <option value="ALMACEN">ALMACEN</option>
                </select>
                <select
                    value={filtroEstado}
                    onChange={(e) => setFiltroEstado(e.target.value)}
                    className={inputClass}
                >
                    <option value="">Todos los estados</option>
                    <option value="activo">Activo</option>
                    <option value="inactivo">Inactivo</option>
                </select>
            </div>

            {/* mensajes */}
            {mensajeExito && (
                <p className="mb-4 rounded-lg border border-green-200 bg-green-50 px-4 py-3 text-sm font-medium text-green-700">
                    {mensajeExito}
                </p>
            )}

            {/* tabla */}
            {cargando && (
                <p className="py-10 text-center text-sm text-slate-400">Cargando usuarios...</p>
            )}

            {errorCarga && (
                <p className="py-10 text-center text-sm text-red-600">No se pudieron cargar los usuarios</p>
            )}

            {!cargando && !errorCarga && (
                <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
                    <div className="overflow-x-auto">
                        <table className="min-w-full divide-y divide-slate-200">
                            <thead className="bg-slate-50">
                                <tr>
                                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">
                                        Nombre
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">
                                        Usuario
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">
                                        Rol
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">
                                        Estado
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">
                                        Fecha Creación
                                    </th>
                                    <th className="px-6 py-3 text-right text-xs font-semibold uppercase tracking-wider text-slate-500">
                                        Acciones
                                    </th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-100">
                                {usuariosFiltrados.length === 0 ? (
                                    <tr>
                                        <td colSpan={6} className="px-6 py-10 text-center text-sm text-slate-400">
                                            No se encontraron usuarios
                                        </td>
                                    </tr>
                                ) : (
                                    usuariosFiltrados.map((u) => (
                                        <tr key={u.id} className="transition-colors hover:bg-slate-50">
                                            <td className="whitespace-nowrap px-6 py-4 text-sm font-medium text-slate-900">
                                                {u.nombre}
                                            </td>
                                            <td className="whitespace-nowrap px-6 py-4 text-sm text-slate-600">
                                                {u.usuario}
                                            </td>
                                            <td className="whitespace-nowrap px-6 py-4 text-sm">
                                                <span className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                                                    u.rol === "ADMIN"
                                                        ? "bg-purple-100 text-purple-700"
                                                        : u.rol === "VENTAS"
                                                            ? "bg-blue-100 text-blue-700"
                                                            : "bg-amber-100 text-amber-700"
                                                }`}>
                                                    {u.rol}
                                                </span>
                                            </td>
                                            <td className="whitespace-nowrap px-6 py-4 text-sm">
                                                <span className={`inline-flex items-center gap-1.5 text-xs font-semibold ${
                                                    u.activo ? "text-green-600" : "text-red-600"
                                                }`}>
                                                    {u.activo ? "🟢 Activo" : "🔴 Inactivo"}
                                                </span>
                                            </td>
                                            <td className="whitespace-nowrap px-6 py-4 text-sm text-slate-500">
                                                {u.fechaCreacion
                                                    ? new Date(u.fechaCreacion).toLocaleDateString("es-PE")
                                                    : "-"
                                                }
                                            </td>
                                            <td className="whitespace-nowrap px-6 py-4 text-right text-sm">
                                                <div className="flex items-center justify-end gap-2">
                                                    <button
                                                        type="button"
                                                        onClick={() => abrirModalEditar(u)}
                                                        className="inline-flex items-center justify-center rounded-lg border border-slate-200 bg-white px-3 py-1.5 text-xs font-medium text-slate-700 transition-colors hover:bg-slate-50"
                                                        title="Editar"
                                                    >
                                                        ✏️ Editar
                                                    </button>
                                                    <button
                                                        type="button"
                                                        onClick={() => abrirModalPassword(u)}
                                                        className="inline-flex items-center justify-center rounded-lg border border-slate-200 bg-white px-3 py-1.5 text-xs font-medium text-slate-700 transition-colors hover:bg-slate-50"
                                                        title="Cambiar contraseña"
                                                    >
                                                        🔑 Clave
                                                    </button>
                                                    {u.activo ? (
                                                        <button
                                                            type="button"
                                                            onClick={() => handleDesactivar(u)}
                                                            className="inline-flex items-center justify-center rounded-lg border border-red-200 bg-white px-3 py-1.5 text-xs font-medium text-red-600 transition-colors hover:bg-red-50"
                                                            title="Desactivar"
                                                        >
                                                            🔴 Off
                                                        </button>
                                                    ) : (
                                                        <button
                                                            type="button"
                                                            onClick={() => handleActivar(u)}
                                                            className="inline-flex items-center justify-center rounded-lg border border-green-200 bg-white px-3 py-1.5 text-xs font-medium text-green-600 transition-colors hover:bg-green-50"
                                                            title="Activar"
                                                        >
                                                            🟢 On
                                                        </button>
                                                    )}
                                                </div>
                                            </td>
                                        </tr>
                                    ))
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            {/* MODAL CREAR */}
            {modalCrear && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
                    <div className="w-full max-w-md rounded-xl bg-white p-6 shadow-xl">
                        <h2 className="text-lg font-semibold text-slate-900">Nuevo Usuario</h2>
                        <p className="mt-1 text-sm text-slate-500">Completa los datos para crear un usuario</p>

                        <div className="mt-5 space-y-4">
                            <label className="block">
                                <span className="mb-1.5 block text-sm font-medium text-slate-600">Nombre *</span>
                                <input type="text" value={formNombre} onChange={(e) => setFormNombre(e.target.value)} className={inputClass} placeholder="Karina Perez" />
                            </label>
                            <label className="block">
                                <span className="mb-1.5 block text-sm font-medium text-slate-600">Usuario *</span>
                                <input type="text" value={formUsuario} onChange={(e) => setFormUsuario(e.target.value)} className={inputClass} placeholder="karina" />
                            </label>
                            <label className="block">
                                <span className="mb-1.5 block text-sm font-medium text-slate-600">Contraseña * (mín. 8 caracteres)</span>
                                <input type="password" value={formPassword} onChange={(e) => setFormPassword(e.target.value)} className={inputClass} placeholder="••••••••" />
                            </label>
                            <label className="block">
                                <span className="mb-1.5 block text-sm font-medium text-slate-600">Rol *</span>
                                <select value={formRol} onChange={(e) => setFormRol(e.target.value)} className={inputClass}>
                                    <option value="ADMIN">ADMIN</option>
                                    <option value="VENTAS">VENTAS</option>
                                    <option value="ALMACEN">ALMACEN</option>
                                </select>
                            </label>
                        </div>

                        {mensajeError && (
                            <p className="mt-3 rounded-lg border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-700">{mensajeError}</p>
                        )}

                        <div className="mt-6 flex justify-end gap-3">
                            <button type="button" onClick={cerrarModales} className={btnSecondary}>Cancelar</button>
                            <button type="button" onClick={handleCrear} disabled={guardando} className={btnPrimary}>
                                {guardando ? "Guardando..." : "Guardar"}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* MODAL EDITAR */}
            {modalEditar && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
                    <div className="w-full max-w-md rounded-xl bg-white p-6 shadow-xl">
                        <h2 className="text-lg font-semibold text-slate-900">Editar Usuario</h2>
                        <p className="mt-1 text-sm text-slate-500">Modifica el nombre y rol</p>

                        <div className="mt-5 space-y-4">
                            <label className="block">
                                <span className="mb-1.5 block text-sm font-medium text-slate-600">Nombre *</span>
                                <input type="text" value={formNombre} onChange={(e) => setFormNombre(e.target.value)} className={inputClass} />
                            </label>
                            <label className="block">
                                <span className="mb-1.5 block text-sm font-medium text-slate-600">Rol *</span>
                                <select value={formRol} onChange={(e) => setFormRol(e.target.value)} className={inputClass}>
                                    <option value="ADMIN">ADMIN</option>
                                    <option value="VENTAS">VENTAS</option>
                                    <option value="ALMACEN">ALMACEN</option>
                                </select>
                            </label>
                        </div>

                        {mensajeError && (
                            <p className="mt-3 rounded-lg border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-700">{mensajeError}</p>
                        )}

                        <div className="mt-6 flex justify-end gap-3">
                            <button type="button" onClick={cerrarModales} className={btnSecondary}>Cancelar</button>
                            <button type="button" onClick={handleEditar} disabled={guardando} className={btnPrimary}>
                                {guardando ? "Guardando..." : "Guardar"}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* MODAL PASSWORD */}
            {modalPassword && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
                    <div className="w-full max-w-md rounded-xl bg-white p-6 shadow-xl">
                        <h2 className="text-lg font-semibold text-slate-900">Cambiar Contraseña</h2>
                        <p className="mt-1 text-sm text-slate-500">
                            Para: <span className="font-medium text-slate-700">{usuarioSeleccionado?.nombre}</span>
                        </p>

                        <div className="mt-5">
                            <label className="block">
                                <span className="mb-1.5 block text-sm font-medium text-slate-600">Nueva contraseña * (mín. 8 caracteres)</span>
                                <input type="password" value={formNuevoPassword} onChange={(e) => setFormNuevoPassword(e.target.value)} className={inputClass} placeholder="••••••••" />
                            </label>
                        </div>

                        {mensajeError && (
                            <p className="mt-3 rounded-lg border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-700">{mensajeError}</p>
                        )}

                        <div className="mt-6 flex justify-end gap-3">
                            <button type="button" onClick={cerrarModales} className={btnSecondary}>Cancelar</button>
                            <button type="button" onClick={handlePassword} disabled={guardando} className={btnPrimary}>
                                {guardando ? "Guardando..." : "Actualizar"}
                            </button>
                        </div>
                    </div>
                </div>
            )}

        </div>
    );
}

export default UsuariosPage;
