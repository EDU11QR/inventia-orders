import axios from "axios";

const API_URL = "/api/whatsapp";

export async function obtenerEstadoWhatsApp() {
    const response = await axios.get(`${API_URL}/status`);
    return response.data;
}

export async function obtenerQrWhatsApp() {
    const response = await axios.get(`${API_URL}/qr`);
    return response.data;
}

export async function desvincularWhatsApp() {
    const response = await axios.post(`${API_URL}/logout`);
    return response.data;
}
