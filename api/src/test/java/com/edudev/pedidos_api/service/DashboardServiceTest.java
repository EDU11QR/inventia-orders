package com.edudev.pedidos_api.service;

import com.edudev.pedidos_api.dto.DashboardPeriodoDTO;
import com.edudev.pedidos_api.dto.SerieTemporalDTO;
import com.edudev.pedidos_api.dto.projection.PeriodoCountProjection;
import com.edudev.pedidos_api.dto.projection.SerieTemporalProjection;
import com.edudev.pedidos_api.dto.projection.VendedorRankingProjection;
import com.edudev.pedidos_api.repository.PedidoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private SerieTemporalProjection filaSerie;

    @Mock
    private VendedorRankingProjection filaRanking;

    @Mock
    private PeriodoCountProjection conteosPeriodo;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void periodoHoyAgrupaPorHora() {

        when(filaSerie.getEtiqueta()).thenReturn("9");
        when(filaSerie.getTotal()).thenReturn(4L);

        when(pedidoRepository.contarPedidosPorHora(
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of(filaSerie));

        List<SerieTemporalDTO> serie =
                dashboardService.obtenerPedidosPorPeriodo("HOY");

        assertEquals(1, serie.size());
        assertEquals("9", serie.get(0).getEtiqueta());
        assertEquals(4L, serie.get(0).getTotal());

        verify(pedidoRepository).contarPedidosPorHora(
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        );
    }

    @Test
    void periodoSemanaAgrupaPorDia() {

        when(filaSerie.getEtiqueta()).thenReturn("2026-08-24");
        when(filaSerie.getTotal()).thenReturn(7L);

        when(pedidoRepository.contarPedidosPorDia(
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of(filaSerie));

        List<SerieTemporalDTO> serie =
                dashboardService.obtenerPedidosPorPeriodo("semana");

        assertEquals("2026-08-24", serie.get(0).getEtiqueta());

        verify(pedidoRepository).contarPedidosPorDia(
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        );
    }

    @Test
    void periodoMesAgrupaPorDiaDelMes() {

        when(filaSerie.getEtiqueta()).thenReturn("15");
        when(filaSerie.getTotal()).thenReturn(12L);

        when(pedidoRepository.contarPedidosPorDiaDelMes(
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of(filaSerie));

        List<SerieTemporalDTO> serie =
                dashboardService.obtenerPedidosPorPeriodo("MES");

        assertEquals("15", serie.get(0).getEtiqueta());
        assertEquals(12L, serie.get(0).getTotal());
    }

    @Test
    void periodoInvalidoLanza400() {

        assertThrows(ResponseStatusException.class,
                () -> dashboardService.obtenerPedidosPorPeriodo("ANIO"));
    }

    @Test
    void dashboardConsolidadoSincronizaGraficoYRanking() {

        // contadores acumulados (independientes del periodo)
        when(conteosPeriodo.getPedidosHoy()).thenReturn(10L);
        when(conteosPeriodo.getPedidosSemana()).thenReturn(10L);
        when(conteosPeriodo.getPedidosMes()).thenReturn(91L);

        when(pedidoRepository.contarPedidosPorPeriodo(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(conteosPeriodo);

        // serie del día: agrupada por hora
        when(filaSerie.getEtiqueta()).thenReturn("09");
        when(filaSerie.getTotal()).thenReturn(4L);

        when(pedidoRepository.contarPedidosPorHora(
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of(filaSerie));

        // ranking filtrado a la misma ventana
        when(filaRanking.getVendedorId()).thenReturn("100429270642733");
        when(filaRanking.getVendedor()).thenReturn("Edu");
        when(filaRanking.getTotal()).thenReturn(10L);

        when(pedidoRepository.obtenerRankingVendedoresPorPeriodo(
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of(filaRanking));

        DashboardPeriodoDTO dashboard =
                dashboardService.obtenerDashboardPorPeriodo("dia");

        assertEquals(10L, dashboard.getMetricas().getHoy());
        assertEquals(10L, dashboard.getMetricas().getSemana());
        assertEquals(91L, dashboard.getMetricas().getMes());

        assertEquals(1, dashboard.getGrafico().size());
        assertEquals("09", dashboard.getGrafico().get(0).getEtiqueta());

        assertEquals(1, dashboard.getTopEmpleados().size());
        assertEquals("Edu", dashboard.getTopEmpleados().get(0).getVendedor());
        assertEquals(10L, dashboard.getTopEmpleados().get(0).getTotal());

        // gráfico y ranking deben consultarse con la misma ventana;
        // se comprueba indirectamente verificando que ambos métodos
        // fueron invocados exactamente una vez
        verify(pedidoRepository).contarPedidosPorHora(
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        );
        verify(pedidoRepository).obtenerRankingVendedoresPorPeriodo(
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        );
    }

    @Test
    void dashboardAceptaPeriodosEnMinusculasYMayusculas() {

        when(conteosPeriodo.getPedidosHoy()).thenReturn(1L);
        when(conteosPeriodo.getPedidosSemana()).thenReturn(2L);
        when(conteosPeriodo.getPedidosMes()).thenReturn(3L);

        when(pedidoRepository.contarPedidosPorPeriodo(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(conteosPeriodo);

        when(pedidoRepository.contarPedidosPorHora(
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of());

        when(pedidoRepository.obtenerRankingVendedoresPorPeriodo(
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of());

        DashboardPeriodoDTO enMayusculas =
                dashboardService.obtenerDashboardPorPeriodo("DIA");

        DashboardPeriodoDTO enMinusculas =
                dashboardService.obtenerDashboardPorPeriodo("dia");

        assertEquals(enMayusculas.getMetricas().getHoy(),
                enMinusculas.getMetricas().getHoy());
    }

    @Test
    void dashboardConPeriodoInvalidoLanza400() {

        assertThrows(ResponseStatusException.class,
                () -> dashboardService.obtenerDashboardPorPeriodo("anio"));
    }
}
