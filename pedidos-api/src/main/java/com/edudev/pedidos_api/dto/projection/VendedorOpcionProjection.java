package com.edudev.pedidos_api.dto.projection;

// ==========================================
// opción del selector de vendedores:
// identidad real (vendedor_id) + nombre
// más reciente para mostrar
// ==========================================
public interface VendedorOpcionProjection {

    String getVendedorId();

    String getVendedor();
}
