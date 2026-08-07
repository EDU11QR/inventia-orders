package com.edudev.pedidos_api.service;

import com.edudev.pedidos_api.entity.Pedido;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class ExcelService {

    private static final String[] ENCABEZADOS = {
            "FECHA DEL PEDIDO REALIZADO",
            "NOMBRES COMPLETOS",
            "DNI",
            "CELULAR",
            "DIRECCIÓN",
            "CIUDAD",
            "PEDIDOS DE PRENDAS",
            "ESTADO",
            "TOTAL A PAGAR",
            "MEDIO DE COMPRA",
            "MÉTODO DE PAGO",
            "COSTO DE ENVÍO",
            "N. DE OPERACIÓN",
            "ENVÍO/AGENCIA",
            "MOTIVO DE CANCELACIÓN",
            "OBSERVACIONES"
    };

    // anchos en unidades de caracteres de Apache POI
    private static final int[] ANCHOS_EN_CARACTERES = {
            12,  // FECHA DEL PEDIDO REALIZADO
            28,  // NOMBRES COMPLETOS
            12,  // DNI
            15,  // CELULAR
            35,  // DIRECCIÓN
            18,  // CIUDAD
            35,  // PEDIDOS DE PRENDAS
            16,  // ESTADO
            15,  // TOTAL A PAGAR
            15,  // MEDIO DE COMPRA
            15,  // MÉTODO DE PAGO
            15,  // COSTO DE ENVÍO
            15,  // N. DE OPERACIÓN
            18,  // ENVÍO/AGENCIA
            40,  // MOTIVO DE CANCELACIÓN
            40   // OBSERVACIONES
    };

    public byte[] generarExcel(List<Pedido> pedidos) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream salida = new ByteArrayOutputStream()) {

            Sheet hoja = workbook.createSheet("Pedidos");

            // estilos únicos, reutilizados en todas las filas
            CellStyle estiloEncabezado = crearEstiloEncabezado(workbook);
            CellStyle estiloFecha = crearEstiloFecha(workbook);
            CellStyle estiloTexto = crearEstiloTexto(workbook, false);
            CellStyle estiloTextoWrap = crearEstiloTexto(workbook, true);

            // 1. congelar la primera fila
            hoja.createFreezePane(0, 1);

            // 2. autofiltro nativo sobre las 16 columnas
            hoja.setAutoFilter(new CellRangeAddress(
                    0,
                    0,
                    0,
                    ENCABEZADOS.length - 1
            ));

            // 4. anchos definidos según el tipo de información
            configurarAnchos(hoja);

            crearEncabezados(hoja, estiloEncabezado);

            int numeroFila = 1;
            for (Pedido pedido : pedidos) {
                crearFila(
                        hoja.createRow(numeroFila++),
                        pedido,
                        estiloFecha,
                        estiloTexto,
                        estiloTextoWrap
                );
            }

            workbook.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo generar el archivo Excel", e);
        }
    }

    private void configurarAnchos(Sheet hoja) {
        for (int columna = 0; columna < ANCHOS_EN_CARACTERES.length; columna++) {
            hoja.setColumnWidth(
                    columna,
                    ANCHOS_EN_CARACTERES[columna] * 256
            );
        }
    }

    private void crearEncabezados(Sheet hoja, CellStyle estilo) {
        Row fila = hoja.createRow(0);
        for (int columna = 0; columna < ENCABEZADOS.length; columna++) {
            Cell celda = fila.createCell(columna);
            celda.setCellValue(ENCABEZADOS[columna]);
            celda.setCellStyle(estilo);
        }
    }

    private void crearFila(
            Row fila,
            Pedido pedido,
            CellStyle estiloFecha,
            CellStyle estiloTexto,
            CellStyle estiloTextoWrap
    ) {
        // Col 0: fecha real, no texto
        escribirFecha(fila, 0, pedido.getFechaRegistro(), estiloFecha);
        escribirTexto(fila, 1, pedido.getCliente(), estiloTexto);
        escribirTexto(fila, 2, pedido.getDni(), estiloTexto);
        escribirTexto(fila, 3, pedido.getTelefono(), estiloTexto);
        escribirTexto(fila, 4, pedido.getDireccion(), estiloTextoWrap);
        escribirTexto(fila, 5, pedido.getCiudad(), estiloTexto);
        escribirTexto(fila, 6, pedido.getProducto(), estiloTextoWrap);
        escribirTexto(fila, 7, pedido.getEstado() == null ? null : pedido.getEstado().name(), estiloTexto);
        // Columnas 8 a 13: TOTAL A PAGAR, MEDIO DE COMPRA, MÉTODO DE PAGO,
        // COSTO DE ENVÍO, N. DE OPERACIÓN y ENVÍO/AGENCIA quedan vacías
        escribirTexto(fila, 14, pedido.getMotivoCancelacion(), estiloTextoWrap);
        escribirTexto(fila, 15, pedido.getObservaciones(), estiloTextoWrap);
    }

    private void escribirTexto(Row fila, int columna, String valor, CellStyle estilo) {
        Cell celda = fila.createCell(columna);
        celda.setCellValue(valor == null ? "" : valor);
        celda.setCellStyle(estilo);
    }

    private CellStyle crearEstiloEncabezado(Workbook workbook) {
        Font fuente = workbook.createFont();
        fuente.setBold(true);

        CellStyle estilo = workbook.createCellStyle();
        estilo.setFont(fuente);
        estilo.setAlignment(HorizontalAlignment.CENTER);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);
        estilo.setWrapText(true);
        estilo.setFillForegroundColor(
                IndexedColors.GREY_25_PERCENT.getIndex()
        );
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        return estilo;
    }

    private CellStyle crearEstiloFecha(Workbook workbook) {
        DataFormat formato = workbook.createDataFormat();
        CellStyle estilo = workbook.createCellStyle();
        estilo.setDataFormat(formato.getFormat("dd/MM/yyyy"));
        estilo.setAlignment(HorizontalAlignment.CENTER);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);
        return estilo;
    }

    private CellStyle crearEstiloTexto(Workbook workbook, boolean wrap) {
        CellStyle estilo = workbook.createCellStyle();
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);
        estilo.setWrapText(wrap);
        return estilo;
    }

    private void escribirFecha(
            Row fila,
            int columna,
            java.time.LocalDateTime fecha,
            CellStyle estilo
    ) {
        Cell celda = fila.createCell(columna);
        if (fecha == null) {
            celda.setBlank();
            celda.setCellStyle(estilo);
            return;
        }
        celda.setCellValue(
                Date.from(
                        fecha.atZone(ZoneId.systemDefault()).toInstant()
                )
        );
        celda.setCellStyle(estilo);
    }
}
