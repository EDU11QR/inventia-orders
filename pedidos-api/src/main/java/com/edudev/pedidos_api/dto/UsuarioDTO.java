package com.edudev.pedidos_api.dto;

import com.edudev.pedidos_api.entity.Usuario;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UsuarioDTO {
    private Long id;
    private String nombre;
    private String usuario;
    private String rol;
    private Boolean activo;
    private LocalDateTime fechaCreacion;

    public static UsuarioDTO fromEntity(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.id = usuario.getId();
        dto.nombre = usuario.getNombre();
        dto.usuario = usuario.getUsuario();
        dto.rol = usuario.getRol().name();
        dto.activo = usuario.getActivo();
        dto.fechaCreacion = usuario.getFechaCreacion();
        return dto;
    }
}
