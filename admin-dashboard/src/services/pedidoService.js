import axios from "axios";

// URL base del backend Spring Boot
const API_URL = "http://localhost:8081/api/pedidos";

// obtener todos los pedidos
export const obtenerPedidos = async () => {

    const response = await axios.get(API_URL);

    return response.data;
};

export const exportarPedidosExcel = async (fechaInicio, fechaFin) => {

    const response = await axios.get(
        `${API_URL}/exportar-excel`,
        {
            params: {
                fechaInicio,
                fechaFin
            },
            responseType: "blob"
        }
    );

    return response;
};

// actualizar estado del pedido
export const actualizarEstadoPedido = async (id, estado) => {

    const response = await axios.put(
        `${API_URL}/${id}/estado?estado=${estado}`
    );

    return response.data;
};

// ==========================================
// cancelar pedido
// ==========================================

export const cancelarPedido = async (
    id,
    motivoCancelacion
) => {

    const response = await axios.put(

        `${API_URL}/${id}/cancelar`,

        {
            motivoCancelacion
        }
    );

    return response.data;
};

// editar pedido
export const editarPedido = async (id, pedido) => {

    const response = await axios.put(

        `${API_URL}/${id}`,
        pedido
    );

    return response.data;
};

// generar pdf y descargar
export const generarPdf = async () => {

    const response = await axios.get(

        `${API_URL}/pdf`,
        {
            responseType: "blob"
        }
    );

    return response.data;
};

// ==========================================
// imprimir pedido individual
// ==========================================

export const imprimirPedido = async (id) => {

    const response = await axios.post(

        `http://localhost:8081/api/pedidos/${id}/imprimir`,

        {},

        {
            responseType: "blob"
        }
    );

    return response.data;
};
