package com.edudev.pedidos_api.service;

import com.edudev.pedidos_api.dto.DashboardResumenDTO;
import com.edudev.pedidos_api.dto.PedidoResumenDTO;
import com.edudev.pedidos_api.dto.projection.EstadoCountProjection;
import com.edudev.pedidos_api.dto.projection.PeriodoCountProjection;
import com.edudev.pedidos_api.entity.EstadoPedido;
import com.edudev.pedidos_api.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PedidoRepository pedidoRepository;

    public DashboardResumenDTO obtenerResumen() {

        LocalDate hoy = LocalDate.now();

        LocalDateTime inicioDia =
                hoy.atStartOfDay();

        LocalDateTime inicioSemana =
                hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                        .atStartOfDay();

        LocalDateTime inicioMes =
                hoy.withDayOfMonth(1)
                        .atStartOfDay();

        PeriodoCountProjection periodo =
                pedidoRepository.contarPedidosPorPeriodo(
                        inicioDia,
                        inicioSemana,
                        inicioMes
                );

        Map<EstadoPedido, Long> porEstado =
                new EnumMap<>(EstadoPedido.class);

        for (EstadoCountProjection fila :
                pedidoRepository.contarPedidosPorEstado()) {

            porEstado.put(fila.getEstado(), fila.getTotal());
        }

        List<PedidoResumenDTO> ultimosPedidos =
                pedidoRepository.findTop10ByOrderByFechaRegistroDesc()
                        .stream()
                        .map(PedidoResumenDTO::from)
                        .toList();

        return DashboardResumenDTO.builder()
                .pedidosHoy(periodo.getPedidosHoy())
                .pedidosSemana(periodo.getPedidosSemana())
                .pedidosMes(periodo.getPedidosMes())
                .pendientes(porEstado.getOrDefault(EstadoPedido.PENDIENTE, 0L))
                .enProceso(porEstado.getOrDefault(EstadoPedido.EN_PROCESO, 0L))
                .impresos(porEstado.getOrDefault(EstadoPedido.IMPRESO, 0L))
                .errorImpresion(porEstado.getOrDefault(EstadoPedido.ERROR_IMPRESION, 0L))
                .cancelados(porEstado.getOrDefault(EstadoPedido.CANCELADO, 0L))
                .ultimosPedidos(ultimosPedidos)
                .build();
    }
}
