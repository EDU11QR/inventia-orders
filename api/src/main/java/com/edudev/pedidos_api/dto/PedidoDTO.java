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

    private String messageId;

    private String remoteJid;

    private LocalDateTime fechaMensaje;
}