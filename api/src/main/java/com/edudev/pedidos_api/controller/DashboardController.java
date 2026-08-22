package com.edudev.pedidos_api.controller;

import com.edudev.pedidos_api.dto.DashboardResumenDTO;
import com.edudev.pedidos_api.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

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
}
