package com.edudev.pedidos_api.entity;

import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDateTime;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cliente;

    private String dni;

    private String telefono;

    private String direccion;

    private String ciudad;

    private Integer correlativo;

    private String producto;

    // ==========================================
// id único mensaje whatsapp
// evita pedidos duplicados
// ==========================================
    @Column(unique = true)
    private String messageId;

    // ==========================================
    // chat origen whatsapp
    // ejemplo: 51999999999@s.whatsapp.net
    // ==========================================
    private String remoteJid;

    // ==========================================
    // fecha original mensaje whatsapp
    // ==========================================
    private LocalDateTime fechaMensaje;

    // ==========================================
    // fecha impresión ticket
    // ==========================================
    private LocalDateTime fechaImpresion;

    // ==========================================
    // notas administrativas
    // ==========================================
    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Enumerated(EnumType.STRING)
    private EstadoPedido estado;

    private LocalDateTime fechaRegistro;
}