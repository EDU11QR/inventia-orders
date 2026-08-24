package com.edudev.pedidos_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// ==========================================
// ranking comercial de vendedores
// ==========================================
@Builder
@Getter
@AllArgsConstructor
public class VendedorRankingDTO {

    private String vendedorId;

    private String vendedor;

    private Long total;
}
