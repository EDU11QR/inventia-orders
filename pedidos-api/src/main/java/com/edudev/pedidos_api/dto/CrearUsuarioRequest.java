package com.edudev.pedidos_api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrearUsuarioRequest {
    private String nombre;
    private String usuario;
    private String password;
    private String rol;
}
