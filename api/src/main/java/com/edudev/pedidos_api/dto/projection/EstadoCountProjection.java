package com.edudev.pedidos_api.dto.projection;

import com.edudev.pedidos_api.entity.EstadoPedido;

public interface EstadoCountProjection {

    EstadoPedido getEstado();

    long getTotal();
}
