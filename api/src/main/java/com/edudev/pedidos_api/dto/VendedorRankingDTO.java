package com.edudev.pedidos_api.dto;

import lombok.Builder;
import lombok.Getter;

// ==========================================
// ranking de vendedores (preparado para
// próxima fase; sin endpoint expuesto)
// ==========================================
@Getter
@Builder
public class VendedorRankingDTO {

    private String vendedorNombre;

    private Long totalPedidos;
}
