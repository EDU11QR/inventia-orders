package com.edudev.pedidos_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// ==========================================
// contadores temporales del dashboard
// (siempre acumulados, independientes
// del periodo seleccionado en el gráfico)
// ==========================================
@Builder
@Getter
@AllArgsConstructor
public class MetricasPeriodoDTO {

    private Long hoy;

    private Long semana;

    private Long mes;
}
