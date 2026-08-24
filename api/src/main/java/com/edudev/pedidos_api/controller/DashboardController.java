package com.edudev.pedidos_api.controller;

import com.edudev.pedidos_api.dto.DashboardPeriodoDTO;
import com.edudev.pedidos_api.dto.DashboardResumenDTO;
import com.edudev.pedidos_api.dto.SerieTemporalDTO;
import com.edudev.pedidos_api.dto.VendedorRankingDTO;
import com.edudev.pedidos_api.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // =========================================================
    // DASHBOARD CONSOLIDADO POR PERIODO
    // =========================================================
    // GET /api/dashboard?periodo=dia|semana|mes
    // métricas acumuladas + gráfico + ranking de empleados,
    // con gráfico y ranking filtrados a la MISMA ventana
    // =========================================================
    @GetMapping
    public ResponseEntity<DashboardPeriodoDTO> obtenerDashboard(
            @RequestParam(name = "periodo", defaultValue = "dia") String periodo
    ) {

        return ResponseEntity.ok(
                dashboardService.obtenerDashboardPorPeriodo(periodo)
        );
    }

    // =========================================================
    // RESUMEN PARA EL DASHBOARD
    // =========================================================
    // métricas del día/semana/mes, conteos por estado
    // y últimos 10 pedidos calculados en MySQL
    // =========================================================
    @GetMapping("/resumen")
    public ResponseEntity<DashboardResumenDTO> obtenerResumen() {

        return ResponseEntity.ok(
                dashboardService.obtenerResumen()
        );
    }

    // =========================================================
    // RANKING DE VENDEDORES
    // =========================================================
    // vendedores ordenados por cantidad de pedidos
    // =========================================================
    @GetMapping("/vendedores")
    public ResponseEntity<List<VendedorRankingDTO>> obtenerVendedores() {

        return ResponseEntity.ok(
                dashboardService.obtenerTopVendedores()
        );
    }

    // =========================================================
    // SERIE TEMPORAL "PEDIDOS POR HORA"
    // =========================================================
    // ?periodo=HOY   -> agrupado por hora del día
    // ?periodo=SEMANA-> agrupado por día (semana actual)
    // ?periodo=MES   -> agrupado por día del mes
    // =========================================================
    @GetMapping("/pedidos-por-periodo")
    public ResponseEntity<List<SerieTemporalDTO>> obtenerPedidosPorPeriodo(
            @RequestParam(name = "periodo", defaultValue = "HOY") String periodo
    ) {

        return ResponseEntity.ok(
                dashboardService.obtenerPedidosPorPeriodo(periodo)
        );
    }
}
