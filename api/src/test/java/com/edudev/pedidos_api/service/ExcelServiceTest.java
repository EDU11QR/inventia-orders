package com.edudev.pedidos_api.service;

import com.edudev.pedidos_api.entity.EstadoPedido;
import com.edudev.pedidos_api.entity.Pedido;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.PaneInformation;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExcelServiceTest {

    @Test
    void generaExcelConLas16ColumnasAdministrativas() throws Exception {
        Pedido pedido = Pedido.builder()
                .id(25L)
                .cliente("Ana Pérez")
                .dni("12345678")
                .telefono("999888777")
                .direccion("Av. Principal 123")
                .ciudad("Lima")
                .producto("Vestido|Zapatos")
                .estado(EstadoPedido.CANCELADO)
                .fechaRegistro(LocalDateTime.of(2026, 8, 5, 14, 30))
                .motivoCancelacion("Cliente se retractó")
                .observaciones("Reembolso pendiente")
                .build();

        byte[] archivo = new ExcelService().generarExcel(List.of(pedido));

        assertNotNull(archivo);
        assertFalse(archivo.length == 0);

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(archivo))) {
            assertNotNull(workbook.getSheet("Pedidos"));

            Row encabezados = workbook.getSheet("Pedidos").getRow(0);
            String[] encabezadosEsperados = {
                    "FECHA DEL PEDIDO REALIZADO", "NOMBRES COMPLETOS", "DNI",
                    "CELULAR", "DIRECCIÓN", "CIUDAD", "PEDIDOS DE PRENDAS",
                    "ESTADO", "TOTAL A PAGAR", "MEDIO DE COMPRA", "MÉTODO DE PAGO",
                    "COSTO DE ENVÍO", "N. DE OPERACIÓN", "ENVÍO/AGENCIA",
                    "MOTIVO DE CANCELACIÓN", "OBSERVACIONES"
            };

            assertEquals(16, encabezados.getLastCellNum());
            for (int columna = 0; columna < encabezadosEsperados.length; columna++) {
                assertEquals(
                        encabezadosEsperados[columna],
                        encabezados.getCell(columna).getStringCellValue()
                );
            }

            Row fila = workbook.getSheet("Pedidos").getRow(1);

            // Columna 0: celda de fecha REAL con formato dd/MM/yyyy
            Cell celdaFecha = fila.getCell(0);
            assertNotNull(celdaFecha);
            assertEquals(CellType.NUMERIC, celdaFecha.getCellType());
            Date esperado = Date.from(
                    pedido.getFechaRegistro().atZone(ZoneId.systemDefault()).toInstant()
            );
            assertEquals(
                    esperado.getTime(),
                    celdaFecha.getDateCellValue().getTime()
            );
            assertEquals(
                    "dd/MM/yyyy",
                    celdaFecha.getCellStyle().getDataFormatString()
            );

            assertEquals("Ana Pérez", fila.getCell(1).getStringCellValue());
            assertEquals("12345678", fila.getCell(2).getStringCellValue());
            assertEquals("999888777", fila.getCell(3).getStringCellValue());
            assertEquals("Av. Principal 123", fila.getCell(4).getStringCellValue());
            assertEquals("Lima", fila.getCell(5).getStringCellValue());
            assertEquals("Vestido|Zapatos", fila.getCell(6).getStringCellValue());
            assertEquals("CANCELADO", fila.getCell(7).getStringCellValue());

            // Campos administrativos vacíos: columnas 8 a 13
            for (int columna = 8; columna <= 13; columna++) {
                assertNull(fila.getCell(columna));
            }

            assertEquals("Cliente se retractó", fila.getCell(14).getStringCellValue());
            assertEquals("Reembolso pendiente", fila.getCell(15).getStringCellValue());
        }
    }

    @Test
    void noExportaLasColumnasEliminadas() throws Exception {
        Pedido pedido = Pedido.builder()
                .id(1L)
                .cliente("Pedro")
                .producto("Polo")
                .estado(EstadoPedido.PENDIENTE)
                .fechaRegistro(LocalDateTime.of(2026, 8, 1, 9, 0))
                .build();

        byte[] archivo = new ExcelService().generarExcel(List.of(pedido));

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(archivo))) {
            Row encabezados = workbook.getSheet("Pedidos").getRow(0);
            assertEquals(16, encabezados.getLastCellNum());

            for (int columna = 0; columna < encabezados.getLastCellNum(); columna++) {
                String encabezado = encabezados.getCell(columna).getStringCellValue();
                assertFalse(encabezado.equals("ID"));
                assertFalse(encabezado.equals("Correlativo"));
                assertFalse(encabezado.equals("Fecha del mensaje"));
                assertFalse(encabezado.equals("Fecha de impresión"));
                assertFalse(encabezado.equals("Fecha de cancelación"));
            }
        }
    }

    @Test
    void congelaLaPrimeraFilaYActivaElAutoFiltro() throws Exception {
        Pedido pedido = Pedido.builder()
                .id(1L)
                .cliente("Pedro")
                .producto("Polo")
                .estado(EstadoPedido.PENDIENTE)
                .fechaRegistro(LocalDateTime.of(2026, 8, 1, 9, 0))
                .build();

        byte[] archivo = new ExcelService().generarExcel(List.of(pedido));

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(archivo))) {
            Sheet hoja = workbook.getSheet("Pedidos");

            // freeze pane: primera fila visible al desplazarse
            PaneInformation pane = hoja.getPaneInformation();
            assertNotNull(pane);
            assertTrue(pane.isFreezePane());
            assertEquals(1, pane.getHorizontalSplitPosition());
            assertEquals(0, pane.getVerticalSplitPosition());

            // autofiltro nativo sobre las 16 columnas
            XSSFSheet hojaXssf = (XSSFSheet) hoja;
            assertNotNull(hojaXssf.getCTWorksheet().getAutoFilter());
            assertEquals(
                    "A1:P1",
                    hojaXssf.getCTWorksheet().getAutoFilter().getRef()
            );
        }
    }

    @Test
    void aplicaEstilosDeEncabezadoAnchosYWrapAdecuados() throws Exception {
        Pedido pedido = Pedido.builder()
                .id(1L)
                .cliente("Pedro")
                .producto("Polo")
                .estado(EstadoPedido.PENDIENTE)
                .fechaRegistro(LocalDateTime.of(2026, 8, 1, 9, 0))
                .build();

        byte[] archivo = new ExcelService().generarExcel(List.of(pedido));

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(archivo))) {
            Sheet hoja = workbook.getSheet("Pedidos");

            // encabezado: negrita, centrado, wrap
            CellStyle estiloEncabezado = hoja.getRow(0).getCell(0).getCellStyle();
            assertTrue(
                    workbook.getFontAt(estiloEncabezado.getFontIndex()).getBold()
            );
            assertEquals(
                    HorizontalAlignment.CENTER,
                    estiloEncabezado.getAlignment()
            );
            assertEquals(
                    VerticalAlignment.CENTER,
                    estiloEncabezado.getVerticalAlignment()
            );
            assertTrue(estiloEncabezado.getWrapText());

            // anchos definidos según el tipo de información
            assertTrue(hoja.getColumnWidth(1) > hoja.getColumnWidth(0));
            assertTrue(hoja.getColumnWidth(4) > hoja.getColumnWidth(2));
            assertTrue(hoja.getColumnWidth(14) > hoja.getColumnWidth(7));

            // wrap text en columnas de texto largo
            assertTrue(hoja.getRow(1).getCell(4).getCellStyle().getWrapText());
            assertTrue(hoja.getRow(1).getCell(6).getCellStyle().getWrapText());
            assertTrue(hoja.getRow(1).getCell(14).getCellStyle().getWrapText());
            assertTrue(hoja.getRow(1).getCell(15).getCellStyle().getWrapText());
            assertFalse(hoja.getRow(1).getCell(2).getCellStyle().getWrapText());
        }
    }
}
