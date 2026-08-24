import axios from "axios";

// URL base del backend Spring Boot (módulo dashboard)
const API_URL = "http://localhost:8081/api/dashboard";

// obtener resumen del dashboard
// métricas del día/semana/mes, conteos por estado
// y últimos 10 pedidos
export const obtenerResumenDashboard = async () => {

    const response = await axios.get(`${API_URL}/resumen`);

    return response.data;
};

// dashboard consolidado sincronizado al periodo
// periodo: "dia" | "semana" | "mes"
// devuelve { metricas, grafico, topEmpleados }
export const obtenerDashboard = async (periodo) => {

    const response = await axios.get(API_URL, {
        params: { periodo }
    });

    return response.data;
};
