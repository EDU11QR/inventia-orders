import { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { login as loginApi } from "../services/authService";

function LoginPage() {

    const { login } = useAuth();

    const [usuario, setUsuario] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [cargando, setCargando] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");

        if (!usuario.trim() || !password.trim()) {
            setError("Ingrese usuario y contraseña");
            return;
        }

        setCargando(true);

        try {
            const data = await loginApi(usuario.trim(), password);
            login(data);
        } catch (err) {
            const detalle = err?.response?.data?.error;
            setError(
                typeof detalle === "string"
                    ? detalle
                    : "Credenciales inválidas"
            );
        } finally {
            setCargando(false);
        }
    };

    return (
        <div className="flex min-h-screen items-center justify-center bg-slate-100 px-4">

            <div className="w-full max-w-sm">

                <div className="mb-8 text-center">
                    <p className="text-2xl font-bold tracking-tight text-blue-600">
                        INVENTIA
                    </p>
                    <p className="mt-0.5 text-sm font-medium text-slate-400">
                        ORDERS
                    </p>
                </div>

                <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm sm:p-8">

                    <h1 className="mb-6 text-center text-lg font-semibold text-slate-900">
                        Iniciar Sesión
                    </h1>

                    <form onSubmit={handleSubmit} className="space-y-4">

                        <label className="block">
                            <span className="mb-1.5 block text-sm font-medium text-slate-600">
                                Usuario
                            </span>
                            <input
                                type="text"
                                value={usuario}
                                onChange={(e) => {
                                    setUsuario(e.target.value);
                                    setError("");
                                }}
                                autoFocus
                                className="
                                    w-full rounded-lg border border-slate-300 bg-white
                                    px-3 py-2.5 text-sm text-slate-900 shadow-sm
                                    transition focus:border-blue-500 focus:outline-none
                                    focus:ring-2 focus:ring-blue-500/20
                                "
                            />
                        </label>

                        <label className="block">
                            <span className="mb-1.5 block text-sm font-medium text-slate-600">
                                Contraseña
                            </span>
                            <input
                                type="password"
                                value={password}
                                onChange={(e) => {
                                    setPassword(e.target.value);
                                    setError("");
                                }}
                                className="
                                    w-full rounded-lg border border-slate-300 bg-white
                                    px-3 py-2.5 text-sm text-slate-900 shadow-sm
                                    transition focus:border-blue-500 focus:outline-none
                                    focus:ring-2 focus:ring-blue-500/20
                                "
                            />
                        </label>

                        {error && (
                            <p
                                role="alert"
                                className="rounded-lg border border-red-200 bg-red-50 px-3 py-2.5 text-sm font-medium text-red-700"
                            >
                                {error}
                            </p>
                        )}

                        <button
                            type="submit"
                            disabled={cargando}
                            className="
                                w-full rounded-lg bg-blue-600 px-4 py-2.5
                                text-sm font-semibold text-white shadow-sm
                                transition-colors hover:bg-blue-700
                                focus:outline-none focus:ring-2 focus:ring-blue-500
                                focus:ring-offset-2
                                disabled:cursor-not-allowed disabled:opacity-50
                            "
                        >
                            {cargando ? "Ingresando..." : "Ingresar"}
                        </button>

                    </form>

                </div>

            </div>

        </div>
    );
}

export default LoginPage;
