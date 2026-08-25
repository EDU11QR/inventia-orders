import api from "./api";

export const login = async (usuario, password) => {
    const response = await api.post("/api/auth/login", {
        usuario,
        password,
    });
    return response.data;
};

export const obtenerUsuarioActual = async () => {
    const response = await api.get("/api/auth/me");
    return response.data;
};
