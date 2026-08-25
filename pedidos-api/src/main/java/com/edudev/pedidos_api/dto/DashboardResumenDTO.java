package com.edudev.pedidos_api.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DashboardResumenDTO {

    private long pedidosHoy;

    private long pedidosSemana;

    private long pedidosMes;

    private long pendientes;

    private long enProceso;

    private long impresos;

    private long errorImpresion;

    private long cancelados;

    private List<PedidoResumenDTO> ultimosPedidos;
}
