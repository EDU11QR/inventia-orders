package com.edudev.pedidos_api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PedidoDTO {

    private String cliente;

    private String dni;

    private String telefono;

    private String direccion;

    private String ciudad;

    private String producto;

    private String motivoCancelacion;

    private String messageId;

    private String remoteJid;

    private String vendedorNombre;

    private LocalDateTime fechaMensaje;
}