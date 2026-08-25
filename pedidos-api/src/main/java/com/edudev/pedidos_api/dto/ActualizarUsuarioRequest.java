package com.edudev.pedidos_api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActualizarUsuarioRequest {
    private String nombre;
    private String rol;
}
