package com.edudev.pedidos_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class ConfiguracionEmpresaDTO {

    private String nombreEmpresa;
    private String telefono;
    private String direccion;
    private String mensajeTicket;
    private Boolean mostrarTelefono;
    private Boolean mostrarDireccion;
    private Boolean mostrarMensaje;
}
