import api from "./api";

export const obtenerResumenDashboard = async () => {
    const response = await api.get("/api/dashboard/resumen");
    return response.data;
};

export const obtenerDashboard = async (periodo) => {
    const response = await api.get("/api/dashboard", {
        params: { periodo },
    });
    return response.data;
};
