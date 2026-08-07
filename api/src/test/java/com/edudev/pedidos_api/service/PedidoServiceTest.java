package com.edudev.pedidos_api.service;

import com.edudev.pedidos_api.entity.EstadoPedido;
import com.edudev.pedidos_api.entity.Pedido;
import com.edudev.pedidos_api.repository.PedidoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ExcelService excelService;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    void exportaPedidosDelRangoConLimitesCorrectos() {
        LocalDate fechaInicio = LocalDate.of(2026, 8, 1);
        LocalDate fechaFin = LocalDate.of(2026, 8, 5);

        Pedido pedido = Pedido.builder()
                .id(1L)
                .cliente("Ana")
                .producto("Vestido")
                .estado(EstadoPedido.PENDIENTE)
                .fechaRegistro(LocalDateTime.of(2026, 8, 3, 10, 0))
                .build();

        byte[] excelEsperado = new byte[]{1, 2, 3};

        when(pedidoRepository.findByFechaRegistroEntre(
                LocalDateTime.of(2026, 8, 1, 0, 0),
                LocalDateTime.of(2026, 8, 6, 0, 0)
        )).thenReturn(List.of(pedido));

        when(excelService.generarExcel(List.of(pedido)))
                .thenReturn(excelEsperado);

        byte[] resultado = pedidoService.exportarPedidosExcel(
                fechaInicio,
                fechaFin
        );

        assertArrayEquals(excelEsperado, resultado);
        verify(pedidoRepository).findByFechaRegistroEntre(
                LocalDateTime.of(2026, 8, 1, 0, 0),
                LocalDateTime.of(2026, 8, 6, 0, 0)
        );
    }

    @Test
    void incluyeLas12DeLaNocheDelDiaFinal() {
        // Un pedido registrado el 2026-08-05 a las 23:59 debe
        // quedar dentro del rango porque el límite superior
        // exclusivo es el inicio del día siguiente
        LocalDate fechaInicio = LocalDate.of(2026, 8, 1);
        LocalDate fechaFin = LocalDate.of(2026, 8, 5);

        when(pedidoRepository.findByFechaRegistroEntre(any(), any()))
                .thenReturn(List.of());

        pedidoService.exportarPedidosExcel(fechaInicio, fechaFin);

        verify(pedidoRepository).findByFechaRegistroEntre(
                eq(LocalDateTime.of(2026, 8, 1, 0, 0)),
                eq(LocalDateTime.of(2026, 8, 6, 0, 0))
        );
    }

    @Test
    void devuelveNullSinGenerarExcelCuandoNoHayPedidos() {
        LocalDate fechaInicio = LocalDate.of(2026, 8, 1);
        LocalDate fechaFin = LocalDate.of(2026, 8, 5);

        when(pedidoRepository.findByFechaRegistroEntre(any(), any()))
                .thenReturn(List.of());

        byte[] resultado = pedidoService.exportarPedidosExcel(
                fechaInicio,
                fechaFin
        );

        assertNull(resultado);
        verify(excelService, never()).generarExcel(any());
    }

    @Test
    void rechazaRangoInvertidoSinConsultarElRepositorio() {
        LocalDate fechaInicio = LocalDate.of(2026, 8, 5);
        LocalDate fechaFin = LocalDate.of(2026, 8, 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> pedidoService.exportarPedidosExcel(fechaInicio, fechaFin)
        );

        verify(pedidoRepository, never()).findByFechaRegistroEntre(any(), any());
    }
}
