package com.edudev.pedidos_api.service;

import com.edudev.pedidos_api.dto.DashboardPeriodoDTO;
import com.edudev.pedidos_api.dto.DashboardResumenDTO;
import com.edudev.pedidos_api.dto.MetricasPeriodoDTO;
import com.edudev.pedidos_api.dto.PedidoResumenDTO;
import com.edudev.pedidos_api.dto.SerieTemporalDTO;
import com.edudev.pedidos_api.dto.VendedorRankingDTO;
import com.edudev.pedidos_api.dto.projection.EstadoCountProjection;
import com.edudev.pedidos_api.dto.projection.PeriodoCountProjection;
import com.edudev.pedidos_api.dto.projection.SerieTemporalProjection;
import com.edudev.pedidos_api.dto.projection.VendedorRankingProjection;
import com.edudev.pedidos_api.entity.EstadoPedido;
import com.edudev.pedidos_api.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.Duration;
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

    // =========================================================
    // RANKING DE VENDEDORES
    // =========================================================
    // agrupado por vendedor_id: un registro por vendedor
    // aunque haya usado varios nombres; el nombre mostrado
    // es el más reciente asociado a ese id
    // =========================================================
    public List<VendedorRankingDTO> obtenerTopVendedores() {

        return pedidoRepository.obtenerRankingVendedores()
                .stream()
                .map(fila ->
                        VendedorRankingDTO.builder()
                                .vendedorId(fila.getVendedorId())
                                .vendedor(fila.getVendedor())
                                .total(fila.getTotal())
                                .build()
                )
                .toList();
    }

    // =========================================================
    // VENTANA TEMPORAL DEL PERIODO
    // =========================================================
    // normaliza el periodo (acepta dia/hoy/semana/mes en
    // cualquier combinación de mayúsculas) y devuelve el
    // rango [inicio, fin) correspondiente
    // =========================================================
    private record Ventana(LocalDateTime inicio, LocalDateTime fin) {
    }

    private Ventana resolverVentana(String periodo) {

        String normalizado =
                periodo == null ? "" : periodo.trim().toLowerCase();

        LocalDate hoy = LocalDate.now();

        return switch (normalizado) {

            case "dia", "hoy" -> new Ventana(
                    hoy.atStartOfDay(),
                    hoy.plusDays(1).atStartOfDay()
            );

            case "semana" -> {
                LocalDateTime inicio =
                        hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                                .atStartOfDay();
                yield new Ventana(inicio, inicio.plusDays(7));
            }

            case "mes" -> {
                LocalDateTime inicio =
                        hoy.withDayOfMonth(1).atStartOfDay();
                yield new Ventana(inicio, inicio.plusMonths(1));
            }

            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Periodo inválido. Valores permitidos: dia, semana, mes"
            );
        };
    }

    // =========================================================
    // serie de la ventana: un día -> por hora;
    // semana -> por día natural; mes -> por día del mes
    // =========================================================
    private List<SerieTemporalDTO> serieDeVentana(Ventana ventana) {

        long dias = Duration.between(
                ventana.inicio(),
                ventana.fin()
        ).toDays();

        List<SerieTemporalProjection> filas;

        if (dias <= 1) {

            filas = pedidoRepository.contarPedidosPorHora(
                    ventana.inicio(), ventana.fin());

        } else if (dias <= 7) {

            filas = pedidoRepository.contarPedidosPorDia(
                    ventana.inicio(), ventana.fin());

        } else {

            filas = pedidoRepository.contarPedidosPorDiaDelMes(
                    ventana.inicio(), ventana.fin());
        }

        return filas
                .stream()
                .map(fila ->
                        SerieTemporalDTO.builder()
                                .etiqueta(fila.getEtiqueta())
                                .total(fila.getTotal())
                                .build()
                )
                .toList();
    }

    // =========================================================
    // SERIE TEMPORAL "PEDIDOS POR HORA"
    // =========================================================
    // endpoint conservado por compatibilidad;
    // el dashboard consolidado usa obtenerDashboardPorPeriodo()
    // =========================================================
    public List<SerieTemporalDTO> obtenerPedidosPorPeriodo(String periodo) {

        return serieDeVentana(resolverVentana(periodo));
    }

    // =========================================================
    // DASHBOARD CONSOLIDADO POR PERIODO
    // =========================================================
    // una sola respuesta para sincronizar gráfico y ranking:
    //   - metricas: contadores acumulados hoy/semana/mes
    //   - grafico: serie filtrada al periodo elegido
    //   - topEmpleados: ranking filtrado a la misma ventana
    // =========================================================
    public DashboardPeriodoDTO obtenerDashboardPorPeriodo(String periodo) {

        Ventana ventana = resolverVentana(periodo);

        LocalDate hoy = LocalDate.now();

        PeriodoCountProjection conteos =
                pedidoRepository.contarPedidosPorPeriodo(
                        hoy.atStartOfDay(),
                        hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                                .atStartOfDay(),
                        hoy.withDayOfMonth(1).atStartOfDay()
                );

        MetricasPeriodoDTO metricas = MetricasPeriodoDTO.builder()
                .hoy(conteos.getPedidosHoy())
                .semana(conteos.getPedidosSemana())
                .mes(conteos.getPedidosMes())
                .build();

        List<VendedorRankingDTO> topEmpleados =
                pedidoRepository.obtenerRankingVendedoresPorPeriodo(
                                ventana.inicio(), ventana.fin())
                        .stream()
                        .map(fila ->
                                VendedorRankingDTO.builder()
                                        .vendedorId(fila.getVendedorId())
                                        .vendedor(fila.getVendedor())
                                        .total(fila.getTotal())
                                        .build()
                        )
                        .toList();

        return DashboardPeriodoDTO.builder()
                .metricas(metricas)
                .grafico(serieDeVentana(ventana))
                .topEmpleados(topEmpleados)
                .build();
    }
}
