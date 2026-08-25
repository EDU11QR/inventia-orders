package com.edudev.pedidos_api.dto;

import com.edudev.pedidos_api.entity.EstadoPedido;
import com.edudev.pedidos_api.entity.Pedido;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PedidoResumenDTO {

    private Long id;

    private String cliente;

    private String telefono;

    private String producto;

    private EstadoPedido estado;

    private LocalDateTime fechaRegistro;

    public static PedidoResumenDTO from(Pedido pedido) {

        return PedidoResumenDTO.builder()
                .id(pedido.getId())
                .cliente(pedido.getCliente())
                .telefono(pedido.getTelefono())
                .producto(pedido.getProducto())
                .estado(pedido.getEstado())
                .fechaRegistro(pedido.getFechaRegistro())
                .build();
    }
}
