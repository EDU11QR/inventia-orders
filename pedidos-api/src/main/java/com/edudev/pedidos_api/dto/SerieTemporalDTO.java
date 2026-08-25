package com.edudev.pedidos_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// ==========================================
// punto de una serie temporal del dashboard
// etiqueta: hora ("8") o día ("2026-08-24" / "24")
// total: cantidad de pedidos en ese periodo
// ==========================================
@Builder
@Getter
@AllArgsConstructor
public class SerieTemporalDTO {

    private String etiqueta;

    private Long total;
}
