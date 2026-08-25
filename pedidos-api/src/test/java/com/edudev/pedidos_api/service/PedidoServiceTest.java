package com.edudev.pedidos_api.service;

import com.edudev.pedidos_api.dto.VendedorOpcionDTO;
import com.edudev.pedidos_api.dto.projection.VendedorOpcionProjection;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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

    // ==========================================
    // filtros acumulativos de exportación
    // ==========================================

    private Pedido pedidoFiltro(
            Long id,
            String cliente,
            String producto,
            String vendedorId,
            EstadoPedido estado
    ) {
        return Pedido.builder()
                .id(id)
                .cliente(cliente)
                .producto(producto)
                .vendedorId(vendedorId)
                .estado(estado)
                .fechaRegistro(LocalDateTime.of(2026, 8, 3, 10, 0))
                .build();
    }

    @Test
    void exportarAplicaFiltrosDeVendedorYEstadoAcumulativos() {
        LocalDate fechaInicio = LocalDate.of(2026, 8, 1);
        LocalDate fechaFin = LocalDate.of(2026, 8, 5);

        Pedido eduPendiente = pedidoFiltro(
                1L, "Ana", "Vestido", "edu-id", EstadoPedido.PENDIENTE);
        Pedido eduImpreso = pedidoFiltro(
                2L, "Beto", "Polo", "edu-id", EstadoPedido.IMPRESO);
        Pedido rousPendiente = pedidoFiltro(
                3L, "Carla", "Casaca", "rous-id", EstadoPedido.PENDIENTE);

        when(pedidoRepository.findByFechaRegistroEntre(any(), any()))
                .thenReturn(List.of(eduPendiente, eduImpreso, rousPendiente));

        when(excelService.generarExcel(List.of(eduPendiente)))
                .thenReturn(new byte[]{7});

        byte[] resultado = pedidoService.exportarPedidosExcel(
                fechaInicio,
                fechaFin,
                "edu-id",
                EstadoPedido.PENDIENTE,
                null
        );

        assertArrayEquals(new byte[]{7}, resultado);
    }

    @Test
    void exportarAplicaBusquedaInsensibleAMayusculasPorClienteOProducto() {
        LocalDate fechaInicio = LocalDate.of(2026, 8, 1);
        LocalDate fechaFin = LocalDate.of(2026, 8, 5);

        // coincide por cliente (aunque la búsqueda venga en minúsculas)
        Pedido porCliente = pedidoFiltro(
                1L, "Annjanet Mendivil", "Cárdigan", "edu-id", EstadoPedido.IMPRESO);
        // no coincide ni cliente ni producto
        Pedido sinCoincidencia = pedidoFiltro(
                2L, "María Torres", "Polo", "rous-id", EstadoPedido.IMPRESO);

        when(pedidoRepository.findByFechaRegistroEntre(any(), any()))
                .thenReturn(List.of(porCliente, sinCoincidencia));

        when(excelService.generarExcel(List.of(porCliente)))
                .thenReturn(new byte[]{9});

        byte[] resultado = pedidoService.exportarPedidosExcel(
                fechaInicio,
                fechaFin,
                null,
                null,
                "annjanet"
        );

        assertArrayEquals(new byte[]{9}, resultado);
    }

    @Test
    void exportarConFiltrosQueExcluyenTodoDevuelveNull() {
        LocalDate fechaInicio = LocalDate.of(2026, 8, 1);
        LocalDate fechaFin = LocalDate.of(2026, 8, 5);

        Pedido eduPendiente = pedidoFiltro(
                1L, "Ana", "Vestido", "edu-id", EstadoPedido.PENDIENTE);

        when(pedidoRepository.findByFechaRegistroEntre(any(), any()))
                .thenReturn(List.of(eduPendiente));

        byte[] resultado = pedidoService.exportarPedidosExcel(
                fechaInicio,
                fechaFin,
                "carlos-id",
                null,
                null
        );

        assertNull(resultado);

        verify(excelService, never()).generarExcel(anyList());
    }

    // ==========================================
    // listado con y sin filtro de vendedor
    // ==========================================

    @Test
    void listarSinVendedorDevuelveTodosLosPedidos() {

        pedidoService.listarPedidos(null);

        verify(pedidoRepository).findAllByOrderByFechaRegistroDesc();
    }

    @Test
    void listarConVendedorDevuelveSoloSuHistorial() {

        pedidoService.listarPedidos("edu-id");

        verify(pedidoRepository)
                .findByVendedorIdOrderByFechaRegistroDesc("edu-id");
    }

    // ==========================================
    // selector de vendedores
    // ==========================================

    @Test
    void listarVendedoresMapeaIdentidadYNombreReciente() {

        VendedorOpcionProjection fila = new VendedorOpcionProjection() {

            @Override
            public String getVendedorId() {
                return "edu-id";
            }

            @Override
            public String getVendedor() {
                return "Edu";
            }
        };

        when(pedidoRepository.obtenerVendedoresConPedidos())
                .thenReturn(List.of(fila));

        List<VendedorOpcionDTO> vendedores =
                pedidoService.listarVendedores();

        assertEquals(1, vendedores.size());

        assertEquals("edu-id", vendedores.get(0).getVendedorId());

        assertEquals("Edu", vendedores.get(0).getVendedor());
    }
}
