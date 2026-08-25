package com.edudev.pedidos_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// ==========================================
// opción del selector "Vendedor"
// de la gestión de pedidos
// ==========================================
@Builder
@Getter
@AllArgsConstructor
public class VendedorOpcionDTO {

    private String vendedorId;

    private String vendedor;
}
