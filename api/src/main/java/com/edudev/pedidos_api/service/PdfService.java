//----------------------------------------------------------

/*package com.edudev.pedidos_api.service;

import com.edudev.pedidos_api.entity.Pedido;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import org.springframework.stereotype.Service;

import java.awt.Color;

import java.io.FileOutputStream;

import java.time.format.DateTimeFormatter;

import java.util.List;

@Service
public class PdfService {

    // =====================================================
    // GENERAR PDF
    // =====================================================
    public String generarPdfConsolidado(
            List<Pedido> pedidos
    ) {

        try {

            // =================================================
            // NOMBRE PDF
            // =================================================
            String archivo = "pedidos.pdf";

            // =================================================
            // CREAR DOCUMENTO
            // =================================================
            Document document = new Document(
                    PageSize.A4,
                    25,
                    25,
                    30,
                    30
            );

            PdfWriter.getInstance(
                    document,
                    new FileOutputStream(archivo)
            );

            document.open();

            // =================================================
            // FUENTES
            // =================================================
            Font tituloPrincipalFont = new Font(
                    Font.HELVETICA,
                    28,
                    Font.BOLD,
                    new Color(25, 35, 50)
            );

            Font pedidoFont = new Font(
                    Font.HELVETICA,
                    24,
                    Font.BOLD,
                    new Color(25, 35, 50)
            );

            Font labelFont = new Font(
                    Font.HELVETICA,
                    15,
                    Font.BOLD,
                    new Color(30, 30, 30)
            );

            Font valueFont = new Font(
                    Font.HELVETICA,
                    15,
                    Font.NORMAL,
                    new Color(45, 45, 45)
            );

            Font fechaFont = new Font(
                    Font.HELVETICA,
                    12,
                    Font.ITALIC,
                    new Color(120, 120, 120)
            );

            Font footerFont = new Font(
                    Font.HELVETICA,
                    12,
                    Font.ITALIC,
                    new Color(120, 120, 120)
            );

            // =================================================
            // TITULO GENERAL
            // =================================================
            Paragraph titulo = new Paragraph(
                    "REPORTE DE PEDIDOS",
                    tituloPrincipalFont
            );

            titulo.setAlignment(
                    Element.ALIGN_CENTER
            );

            titulo.setSpacingAfter(25);

            document.add(titulo);

            // =================================================
            // RECORRER PEDIDOS
            // =================================================
            int contador = 1;

            for (Pedido pedido : pedidos) {

                // =============================================
                // CONTENEDOR PRINCIPAL
                // =============================================
                PdfPTable card = new PdfPTable(1);

                card.setWidthPercentage(100);

                card.setSpacingAfter(18);

                PdfPCell container = new PdfPCell();

                container.setPadding(20);

                container.setBorderColor(
                        new Color(190, 190, 190)
                );

                container.setBorderWidth(1.3f);

                // =============================================
                // HEADER
                // =============================================
                PdfPTable headerTable =
                        new PdfPTable(3);

                headerTable.setWidthPercentage(100);

                headerTable.setWidths(
                        new float[]{1f, 5f, 1f}
                );

                // =============================================
                // LOGO
                // =============================================
                PdfPCell logoCell;

                try {

                    Image logo = Image.getInstance(
                            getClass().getResource(
                                    "/static/logo.png"
                            )
                    );

                    logo.scaleToFit(70, 70);

                    logoCell = new PdfPCell(logo);

                } catch (Exception e) {

                    logoCell = new PdfPCell(
                            new Phrase("")
                    );
                }

                logoCell.setBorder(
                        Rectangle.NO_BORDER
                );

                logoCell.setVerticalAlignment(
                        Element.ALIGN_MIDDLE
                );

                // =============================================
                // CENTRO HEADER
                // =============================================
                PdfPCell centerCell =
                        new PdfPCell();

                centerCell.setBorder(
                        Rectangle.NO_BORDER
                );

                PdfPTable titleTable =
                        new PdfPTable(3);

                titleTable.setWidthPercentage(100);

                titleTable.setWidths(
                        new float[]{2f, 2f, 2f}
                );

                // línea izquierda
                PdfPCell lineLeft =
                        new PdfPCell();

                lineLeft.setBorder(
                        Rectangle.NO_BORDER
                );

                Paragraph leftLine =
                        new Paragraph(
                                "━━━━━━━━━━━━",
                                new Font(
                                        Font.HELVETICA,
                                        16,
                                        Font.NORMAL,
                                        new Color(116, 59, 189)
                                )
                        );

                leftLine.setAlignment(
                        Element.ALIGN_CENTER
                );

                lineLeft.addElement(leftLine);

                // titulo
                PdfPCell titleCell =
                        new PdfPCell();

                titleCell.setBorder(
                        Rectangle.NO_BORDER
                );

                Paragraph pedidoTitulo =
                        new Paragraph(
                                "PEDIDO #" + contador,
                                pedidoFont
                        );

                pedidoTitulo.setAlignment(
                        Element.ALIGN_CENTER
                );

                titleCell.addElement(
                        pedidoTitulo
                );

                // línea derecha
                PdfPCell lineRight =
                        new PdfPCell();

                lineRight.setBorder(
                        Rectangle.NO_BORDER
                );

                Paragraph rightLine =
                        new Paragraph(
                                "━━━━━━━━━━━━",
                                new Font(
                                        Font.HELVETICA,
                                        16,
                                        Font.NORMAL,
                                        new Color(116, 59, 189)
                                )
                        );

                rightLine.setAlignment(
                        Element.ALIGN_CENTER
                );

                lineRight.addElement(rightLine);

                titleTable.addCell(lineLeft);
                titleTable.addCell(titleCell);
                titleTable.addCell(lineRight);

                centerCell.addElement(titleTable);

                // celda vacía derecha
                PdfPCell emptyCell =
                        new PdfPCell(
                                new Phrase("")
                        );

                emptyCell.setBorder(
                        Rectangle.NO_BORDER
                );

                // agregar header
                headerTable.addCell(logoCell);
                headerTable.addCell(centerCell);
                headerTable.addCell(emptyCell);

                container.addElement(headerTable);

                // espacio
                container.addElement(
                        new Paragraph(" ")
                );

                // =============================================
                // TABLA DATOS
                // =============================================
                PdfPTable dataTable =
                        new PdfPTable(3);

                dataTable.setWidthPercentage(100);

                dataTable.setWidths(
                        new float[]{2f, 0.4f, 6f}
                );

                agregarFila(
                        dataTable,
                        "Cliente",
                        pedido.getCliente(),
                        labelFont,
                        valueFont
                );

                agregarFila(
                        dataTable,
                        "DNI",
                        pedido.getDni(),
                        labelFont,
                        valueFont
                );

                agregarFila(
                        dataTable,
                        "Teléfono",
                        pedido.getTelefono(),
                        labelFont,
                        valueFont
                );

                agregarFila(
                        dataTable,
                        "Dirección",
                        pedido.getDireccion(),
                        labelFont,
                        valueFont
                );

                agregarFila(
                        dataTable,
                        "Producto",
                        pedido.getProducto(),
                        labelFont,
                        valueFont
                );

                container.addElement(dataTable);

                // =============================================
                // FECHA
                // =============================================
                Paragraph fecha =
                        new Paragraph(

                                "\nFecha: "
                                        + pedido.getFechaRegistro()
                                        .format(
                                                DateTimeFormatter.ofPattern(
                                                        "dd/MM/yyyy HH:mm"
                                                )
                                        ),

                                fechaFont
                        );

                fecha.setAlignment(
                        Element.ALIGN_RIGHT
                );

                container.addElement(fecha);

                // =============================================
                // AGREGAR CARD
                // =============================================
                card.addCell(container);

                document.add(card);

                contador++;
            }

            // =================================================
            // TOTAL
            // =================================================
            Paragraph total =
                    new Paragraph(

                            "TOTAL PEDIDOS: "
                                    + pedidos.size(),

                            new Font(
                                    Font.HELVETICA,
                                    18,
                                    Font.BOLD,
                                    new Color(25, 35, 50)
                            )
                    );

            total.setAlignment(
                    Element.ALIGN_RIGHT
            );

            total.setSpacingBefore(10);

            document.add(total);

            // =================================================
            // FOOTER
            // =================================================
            Paragraph footer =
                    new Paragraph(

                            "\nSistema generado automáticamente",

                            footerFont
                    );

            footer.setAlignment(
                    Element.ALIGN_CENTER
            );

            footer.setSpacingBefore(25);

            document.add(footer);

            // =================================================
            // CERRAR
            // =================================================
            document.close();

            return archivo;

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    // =====================================================
    // FILAS
    // =====================================================
    private void agregarFila(

            PdfPTable table,
            String titulo,
            String valor,
            Font labelFont,
            Font valueFont

    ) {

        PdfPCell labelCell =
                new PdfPCell(
                        new Phrase(
                                titulo,
                                labelFont
                        )
                );

        labelCell.setBorderColor(
                new Color(230, 230, 230)
        );

        labelCell.setBorderWidthBottom(1f);

        labelCell.setBorderWidthTop(0);

        labelCell.setBorderWidthLeft(0);

        labelCell.setBorderWidthRight(0);

        labelCell.setPadding(10);

        PdfPCell dotsCell =
                new PdfPCell(
                        new Phrase(
                                ":",
                                labelFont
                        )
                );

        dotsCell.setHorizontalAlignment(
                Element.ALIGN_CENTER
        );

        dotsCell.setBorderColor(
                new Color(230, 230, 230)
        );

        dotsCell.setBorderWidthBottom(1f);

        dotsCell.setBorderWidthTop(0);

        dotsCell.setBorderWidthLeft(0);

        dotsCell.setBorderWidthRight(0);

        dotsCell.setPadding(10);

        PdfPCell valueCell =
                new PdfPCell(
                        new Phrase(
                                valor,
                                valueFont
                        )
                );

        valueCell.setBorderColor(
                new Color(230, 230, 230)
        );

        valueCell.setBorderWidthBottom(1f);

        valueCell.setBorderWidthTop(0);

        valueCell.setBorderWidthLeft(0);

        valueCell.setBorderWidthRight(0);

        valueCell.setPadding(10);

        table.addCell(labelCell);

        table.addCell(dotsCell);

        table.addCell(valueCell);
    }
}
*/


//-----------------------------------------------------------------

package com.edudev.pedidos_api.service;
import com.edudev.pedidos_api.entity.Pedido;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.awt.Color;
import com.lowagie.text.pdf.draw.LineSeparator;
import java.util.List;

@Service
public class PdfService {

//    public String generarPdfConsolidado(List<Pedido> pedidos)
public String generarTicket(Pedido pedido) {

    try {

        new File("pdfs").mkdirs();

        String archivo =
                "pdfs/ticket_"
                        + System.currentTimeMillis()
                        + ".pdf";

        // ==================================================
        // TAMAÑO TICKET
        // ==================================================

        Rectangle pageSize =
                new Rectangle(283, 283);

        Document document =
                new Document(
                        pageSize,
                        4,
                        4,
                        4,
                        4
                );

        PdfWriter writer =
                PdfWriter.getInstance(
                        document,
                        new FileOutputStream(archivo)
                );

        document.open();

        // ==================================================
        // CORRELATIVO
        // ==================================================

        String correlativo =
                String.format(
                        "%09d",
                        pedido.getId()
                );

        // ==================================================
        // FUENTES
        // ==================================================

        Font headerFont = new Font(
                Font.HELVETICA,
                12,
                Font.BOLD,
                Color.BLACK
        );

        Font labelFont = new Font(
                Font.HELVETICA,
                7,
                Font.BOLD,
                Color.BLACK
        );

        Font valueFont = new Font(
                Font.HELVETICA,
                7,
                Font.NORMAL,
                Color.BLACK
        );

        Font itemsFont = new Font(
                Font.HELVETICA,
                6,
                Font.NORMAL,
                Color.BLACK
        );

        Font barcodeTextFont = new Font(
                Font.HELVETICA,
                9,
                Font.BOLD,
                Color.BLACK
        );

        Font footerFont = new Font(
                Font.HELVETICA,
                8,
                Font.NORMAL,
                Color.BLACK
        );

        // ==================================================
        // TABLA PRINCIPAL
        // ==================================================

        PdfPTable main =
                new PdfPTable(1);

        main.setWidthPercentage(100);

        // ==================================================
        // HEADER
        // ==================================================

        PdfPTable header =
                new PdfPTable(1);

        header.setWidthPercentage(100);

        PdfPCell empresaCell =
                new PdfPCell(
                        new Phrase(
                                "CORPORACION POSH SAC",
                                headerFont
                        )
                );

        empresaCell.setHorizontalAlignment(
                Element.ALIGN_CENTER
        );

        empresaCell.setVerticalAlignment(
                Element.ALIGN_MIDDLE
        );

        empresaCell.setFixedHeight(24);

        empresaCell.setBorderWidth(1.2f);

        header.addCell(empresaCell);

        main.addCell(
                new PdfPCell(header)
        );

        // ==================================================
        // DATOS CLIENTE
        // ==================================================

        PdfPTable datos =
                new PdfPTable(3);

        datos.setWidthPercentage(100);

        datos.setWidths(
                new float[]{2f, 0.5f, 4f}
        );

        agregarFilaBox(
                datos,
                "CLIENTE",
                pedido.getCliente(),
                labelFont,
                valueFont
        );

        agregarFilaBox(
                datos,
                "TELEFONO",
                pedido.getTelefono(),
                labelFont,
                valueFont
        );

        agregarFilaBox(
                datos,
                "DNI",
                pedido.getDni(),
                labelFont,
                valueFont
        );

        agregarFilaBox(
                datos,
                "DIRECCION",
                pedido.getDireccion(),
                labelFont,
                valueFont
        );

        agregarFilaBox(
                datos,
                "CIUDAD",
                pedido.getCiudad(),
                labelFont,
                valueFont
        );

        PdfPCell datosContainer =
                new PdfPCell(datos);

        datosContainer.setPadding(4);

        datosContainer.setBorderWidth(0.8f);

        main.addCell(datosContainer);

        // ==================================================
        // PEDIDO + BARCODE
        // ==================================================

        PdfPTable pedidoTable =
                new PdfPTable(2);

        pedidoTable.setWidthPercentage(100);

        pedidoTable.setWidths(
                new float[]{1.3f, 2.7f}
        );

        // izquierda
        PdfPCell pedidoTextCell =
                new PdfPCell(
                        new Phrase(
                                "NUMERO DE PEDIDO",
                                labelFont
                        )
                );

        pedidoTextCell.setVerticalAlignment(
                Element.ALIGN_MIDDLE
        );

        pedidoTextCell.setPaddingLeft(8);

        pedidoTextCell.setFixedHeight(40);

        pedidoTable.addCell(pedidoTextCell);

        // derecha barcode
        PdfPCell barcodeCell =
                new PdfPCell();

        barcodeCell.setPaddingTop(5);

        barcodeCell.setHorizontalAlignment(
                Element.ALIGN_CENTER
        );

        Barcode128 barcode =
                new Barcode128();

        barcode.setCode(correlativo);

        barcode.setFont(null);

        Image barcodeImage =
                barcode.createImageWithBarcode(
                        writer.getDirectContent(),
                        Color.BLACK,
                        Color.BLACK
                );

        barcodeImage.scalePercent(60);

        barcodeImage.setAlignment(
                Element.ALIGN_CENTER
        );

        barcodeCell.addElement(barcodeImage);

        Paragraph codigo =
                new Paragraph(
                        correlativo,
                        barcodeTextFont
                );

        codigo.setAlignment(
                Element.ALIGN_CENTER
        );

        barcodeCell.addElement(codigo);

        pedidoTable.addCell(barcodeCell);

        PdfPCell pedidoContainer =
                new PdfPCell(pedidoTable);

        pedidoContainer.setBorderWidth(1.2f);

        main.addCell(pedidoContainer);

        // ==================================================
        // ITEMS
        // ==================================================

        PdfPTable itemsTable =
                new PdfPTable(2);

        itemsTable.setWidthPercentage(100);

        itemsTable.setWidths(
                new float[]{1f, 1f}
        );

        PdfPCell titleItems =
                new PdfPCell(
                        new Phrase(
                                "ITEMS",
                                labelFont
                        )
                );

        titleItems.setColspan(2);

        titleItems.setPadding(6);

        itemsTable.addCell(titleItems);

        String[] items =
                pedido.getProducto()
                        .split("\\|");

        // ======================================
        // límite máximo productos
        // evita romper ticket 10x10
        // ======================================

        int limite =
                Math.min(
                        items.length,
                        8
                );

        int mitad =
                (int) Math.ceil(
                        limite / 2.0
                );

        PdfPCell col1 =
                new PdfPCell();

        PdfPCell col2 =
                new PdfPCell();

        col1.setFixedHeight(70);
        col2.setFixedHeight(70);

        for (int i = 0; i < mitad; i++) {

            Paragraph p =
                    new Paragraph(
                            "• " + items[i],
                            itemsFont
                    );

            p.setSpacingAfter(6);

            col1.addElement(p);
        }

        for (int i = mitad; i < limite; i++) {

            Paragraph p =
                    new Paragraph(
                            "• " + items[i],
                            itemsFont
                    );

            p.setSpacingAfter(6);

            col2.addElement(p);
        }

        itemsTable.addCell(col1);
        itemsTable.addCell(col2);

        PdfPCell itemsContainer =
                new PdfPCell(itemsTable);

        itemsContainer.setBorderWidth(1.2f);

        main.addCell(itemsContainer);

        // ==================================================
        // FOOTER
        // ==================================================

        Paragraph fecha =
                new Paragraph(

                        pedido.getFechaRegistro()
                                .format(
                                        DateTimeFormatter.ofPattern(
                                                "dd/MM/yyyy HH:mm"
                                        )
                                ),

                        footerFont
                );

        fecha.setAlignment(
                Element.ALIGN_CENTER
        );

        PdfPCell footerCell =
                new PdfPCell();

        footerCell.setFixedHeight(18);

        footerCell.setVerticalAlignment(
                Element.ALIGN_MIDDLE
        );

        footerCell.addElement(fecha);

        footerCell.setBorderWidth(1.2f);

        main.addCell(footerCell);

        // ==================================================
        // AGREGAR TODO
        // ==================================================

        document.add(main);

        document.close();

        return archivo;

    } catch (Exception e) {

        throw new RuntimeException(e);
    }
}

    // ==============================================
    // agregar filas
    // ==============================================
    private void agregarFila(

            PdfPTable table,
            String titulo,
            String valor,
            Font labelFont,
            Font valueFont

    ) {

        PdfPCell labelCell =
                new PdfPCell(
                        new Phrase(
                                titulo + ":",
                                labelFont
                        )
                );

        labelCell.setBorder(
                Rectangle.NO_BORDER
        );

        labelCell.setHorizontalAlignment(
                Element.ALIGN_RIGHT
        );

        labelCell.setVerticalAlignment(
                Element.ALIGN_MIDDLE
        );

        labelCell.setPaddingTop(3);

        labelCell.setPaddingBottom(3);

        PdfPCell valueCell =
                new PdfPCell(
                        new Phrase(
                                valor,
                                valueFont
                        )
                );

        valueCell.setBorder(
                Rectangle.NO_BORDER
        );

        valueCell.setHorizontalAlignment(
                Element.ALIGN_LEFT
        );

        valueCell.setVerticalAlignment(
                Element.ALIGN_MIDDLE
        );

        valueCell.setPaddingTop(3);

        valueCell.setPaddingBottom(3);

        table.addCell(labelCell);

        table.addCell(valueCell);
    }

    private void agregarFilaCompacta(

            PdfPTable table,
            String titulo,
            String valor,
            Font labelFont,
            Font valueFont

    ) {

        PdfPCell labelCell =
                new PdfPCell(
                        new Phrase(
                                titulo,
                                labelFont
                        )
                );

        labelCell.setBorder(
                Rectangle.NO_BORDER
        );

        labelCell.setPaddingBottom(2);

        PdfPCell valueCell =
                new PdfPCell(
                        new Phrase(
                                valor != null ? valor : "",
                                valueFont
                        )
                );

        valueCell.setBorder(
                Rectangle.NO_BORDER
        );

        valueCell.setPaddingBottom(2);

        table.addCell(labelCell);

        table.addCell(valueCell);
    }

    private void agregarFilaBox(

            PdfPTable table,
            String titulo,
            String valor,
            Font labelFont,
            Font valueFont

    ) {

        PdfPCell label =
                new PdfPCell(
                        new Phrase(
                                titulo,
                                labelFont
                        )
                );

        label.setBorder(
                Rectangle.NO_BORDER
        );

        label.setPaddingBottom(5);

        PdfPCell dots =
                new PdfPCell(
                        new Phrase(
                                ":",
                                labelFont
                        )
                );

        dots.setBorder(
                Rectangle.NO_BORDER
        );

        dots.setHorizontalAlignment(
                Element.ALIGN_CENTER
        );

        PdfPCell value =
                new PdfPCell(
                        new Phrase(
                                valor != null
                                        ? valor
                                        : "",
                                valueFont
                        )
                );

        value.setBorder(
                Rectangle.NO_BORDER
        );

        value.setPaddingBottom(5);

        table.addCell(label);

        table.addCell(dots);

        table.addCell(value);
    }


}