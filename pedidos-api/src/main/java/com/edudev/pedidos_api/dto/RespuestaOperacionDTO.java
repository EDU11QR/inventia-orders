package com.edudev.pedidos_api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// ==========================================
// respuesta genérica de operaciones
// de escritura (ej. PUT configuración):
// { "success": true }
// ==========================================
@Getter
@AllArgsConstructor
public class RespuestaOperacionDTO {

    private boolean success;
}
