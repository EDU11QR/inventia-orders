package com.edudev.pedidos_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "configuracion_empresa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionEmpresa {

    @Id
    private Long id;

    @Column(name = "nombre_empresa", nullable = false, length = 100)
    private String nombreEmpresa;

    @Column(name = "telefono", length = 30)
    private String telefono;

    @Column(name = "direccion", length = 200)
    private String direccion;

    @Column(name = "mensaje_ticket", length = 255)
    private String mensajeTicket;

    @Column(name = "mostrar_telefono")
    private Boolean mostrarTelefono;

    @Column(name = "mostrar_direccion")
    private Boolean mostrarDireccion;

    @Column(name = "mostrar_mensaje")
    private Boolean mostrarMensaje;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}
