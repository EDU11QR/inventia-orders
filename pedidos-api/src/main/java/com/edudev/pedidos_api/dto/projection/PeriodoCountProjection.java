package com.edudev.pedidos_api.dto.projection;

public interface PeriodoCountProjection {

    long getPedidosHoy();

    long getPedidosSemana();

    long getPedidosMes();
}
