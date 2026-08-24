package com.edudev.pedidos_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

// ==========================================
// respuesta consolidada del dashboard
// sincronizada al periodo seleccionado:
//   - metricas: contadores acumulados hoy/semana/mes
//   - grafico: serie temporal filtrada por el periodo
//   - topEmpleados: ranking filtrado por el mismo periodo
// ==========================================
@Builder
@Getter
@AllArgsConstructor
public class DashboardPeriodoDTO {

    private MetricasPeriodoDTO metricas;

    private List<SerieTemporalDTO> grafico;

    private List<VendedorRankingDTO> topEmpleados;
}
