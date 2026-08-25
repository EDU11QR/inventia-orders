import { createContext, useContext, useEffect, useState } from "react";
import { obtenerUsuarioActual } from "../services/authService";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {

    const [usuario, setUsuario] = useState(null);
    const [cargando, setCargando] = useState(true);

    useEffect(() => {

        const token = localStorage.getItem("token");

        if (!token) {
            setCargando(false);
            return;
        }

        obtenerUsuarioActual()
            .then((data) => {
                setUsuario(data);
            })
            .catch(() => {
                localStorage.removeItem("token");
            })
            .finally(() => {
                setCargando(false);
            });
    }, []);

    const login = (datos) => {
        localStorage.setItem("token", datos.token);
        setUsuario({
            nombre: datos.nombre,
            usuario: datos.usuario,
            rol: datos.rol,
        });
    };

    const logout = () => {
        localStorage.removeItem("token");
        setUsuario(null);
    };

    const tieneRol = (...roles) => {
        return usuario && roles.includes(usuario.rol);
    };

    return (
        <AuthContext.Provider value={{ usuario, cargando, login, logout, tieneRol }}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    return useContext(AuthContext);
}
