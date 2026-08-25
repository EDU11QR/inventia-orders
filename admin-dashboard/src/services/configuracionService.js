import api from "./api";

export const obtenerConfiguracionEmpresa = async () => {
    const response = await api.get("/api/configuracion/empresa");
    return response.data;
};

export const guardarConfiguracionEmpresa = async (configuracion) => {
    const response = await api.post("/api/configuracion/empresa", configuracion);
    return response.data;
};
