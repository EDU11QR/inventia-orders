import api from "./api";

export const obtenerPedidos = async () => {
    const response = await api.get("/api/pedidos");
    return response.data;
};

export const obtenerVendedores = async () => {
    const response = await api.get("/api/pedidos/vendedores");
    return response.data;
};

export const exportarPedidosExcel = async (fechaInicio, fechaFin, filtros = {}) => {
    const params = { fechaInicio, fechaFin };

    if (filtros.vendedorId) params.vendedorId = filtros.vendedorId;
    if (filtros.estado) params.estado = filtros.estado;
    if (filtros.busqueda) params.busqueda = filtros.busqueda;

    const response = await api.get("/api/pedidos/exportar-excel", {
        params,
        responseType: "blob",
    });

    return response;
};

export const actualizarEstadoPedido = async (id, estado) => {
    const response = await api.put(`/api/pedidos/${id}/estado?estado=${estado}`);
    return response.data;
};

export const cancelarPedido = async (id, motivoCancelacion) => {
    const response = await api.put(`/api/pedidos/${id}/cancelar`, {
        motivoCancelacion,
    });
    return response.data;
};

export const editarPedido = async (id, pedido) => {
    const response = await api.put(`/api/pedidos/${id}`, pedido);
    return response.data;
};

export const generarPdf = async () => {
    const response = await api.get("/api/pedidos/pdf", {
        responseType: "blob",
    });
    return response.data;
};

export const imprimirPedido = async (id) => {
    const response = await api.post(`/api/pedidos/${id}/imprimir`, {}, {
        responseType: "blob",
    });
    return response.data;
};
