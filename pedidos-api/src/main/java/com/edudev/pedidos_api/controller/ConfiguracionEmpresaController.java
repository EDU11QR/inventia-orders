package com.edudev.pedidos_api.controller;

import com.edudev.pedidos_api.dto.ConfiguracionEmpresaDTO;
import com.edudev.pedidos_api.dto.RespuestaOperacionDTO;
import com.edudev.pedidos_api.service.ConfiguracionEmpresaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/configuracion")
@RequiredArgsConstructor
public class ConfiguracionEmpresaController {

    private final ConfiguracionEmpresaService configuracionEmpresaService;

    @GetMapping("/empresa")
    public ConfiguracionEmpresaDTO obtenerConfiguracion() {
        return configuracionEmpresaService.obtenerConfiguracion();
    }

    @PostMapping("/empresa")
    public ResponseEntity<?> guardarConfiguracion(
            @RequestBody(required = false) ConfiguracionEmpresaDTO request
    ) {
        try {

            ConfiguracionEmpresaDTO resultado =
                    configuracionEmpresaService.guardarConfiguracion(request);

            return ResponseEntity.ok(resultado);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
