import api from "./api";

export async function obtenerEstadoWhatsApp() {
    const response = await api.get("/api/whatsapp/status");
    return response.data;
}

export async function obtenerQrWhatsApp() {
    const response = await api.get("/api/whatsapp/qr");
    return response.data;
}

export async function desvincularWhatsApp() {
    const response = await api.post("/api/whatsapp/logout");
    return response.data;
}

export async function reconectarWhatsApp() {
    const response = await api.post("/api/whatsapp/reconnect");
    return response.data;
}
