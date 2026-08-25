package com.edudev.pedidos_api.service;
import com.edudev.pedidos_api.entity.Pedido;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;


@Service
public class PdfService {

    // nombre de la empresa leído de la configuración
    // (tabla configuracion_empresa); así los tickets
    // reflejan el cambio sin recompilar ni reiniciar
    private final ConfiguracionEmpresaService configuracionEmpresaService;

    public PdfService(ConfiguracionEmpresaService configuracionEmpresaService) {

        this.configuracionEmpresaService = configuracionEmpresaService;
    }

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
                8,
                Font.BOLD,
                Color.BLACK
        );

        Font valueFont = new Font(
                Font.HELVETICA,
                8,
                Font.NORMAL,
                Color.BLACK
        );

        Font itemsFont = new Font(
                Font.HELVETICA,
                7,
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

        String nombreEmpresa = configuracionEmpresaService.obtenerNombreEmpresa();
        String telefonoEmpresa = configuracionEmpresaService.obtenerTelefono();
        String direccionEmpresa = configuracionEmpresaService.obtenerDireccion();
        boolean mostrarTelefono = configuracionEmpresaService.debeMostrarTelefono();
        boolean mostrarDireccion = configuracionEmpresaService.debeMostrarDireccion();

        int filasHeader = 1;
        if (mostrarTelefono && telefonoEmpresa != null && !telefonoEmpresa.isEmpty()) filasHeader++;
        if (mostrarDireccion && direccionEmpresa != null && !direccionEmpresa.isEmpty()) filasHeader++;

        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);

        PdfPCell empresaCell = new PdfPCell(
                new Phrase(" " + nombreEmpresa + " - PEDIDOS ", headerFont)
        );
        empresaCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        empresaCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        empresaCell.setFixedHeight(24);
        empresaCell.setBorderWidth(1.2f);
        header.addCell(empresaCell);

        if (mostrarTelefono && telefonoEmpresa != null && !telefonoEmpresa.isEmpty()) {
            PdfPCell telCell = new PdfPCell(
                    new Phrase("Tel: " + telefonoEmpresa, valueFont)
            );
            telCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            telCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            telCell.setFixedHeight(14);
            telCell.setBorderWidth(1.2f);
            header.addCell(telCell);
        }

        if (mostrarDireccion && direccionEmpresa != null && !direccionEmpresa.isEmpty()) {
            PdfPCell dirCell = new PdfPCell(
                    new Phrase(direccionEmpresa, valueFont)
            );
            dirCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            dirCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            dirCell.setFixedHeight(14);
            dirCell.setBorderWidth(1.2f);
            header.addCell(dirCell);
        }

        main.addCell(new PdfPCell(header));

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
                                "NRO DE PEDIDO",
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

        boolean mostrarMensaje = configuracionEmpresaService.debeMostrarMensaje();
        String mensajeTicket = configuracionEmpresaService.obtenerMensajeTicket();

        if (mostrarMensaje && mensajeTicket != null && !mensajeTicket.isEmpty()) {

            Paragraph msgParagraph = new Paragraph(mensajeTicket, footerFont);
            msgParagraph.setAlignment(Element.ALIGN_CENTER);

            PdfPCell msgCell = new PdfPCell();
            msgCell.addElement(msgParagraph);
            msgCell.setFixedHeight(16);
            msgCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            msgCell.setBorderWidth(1.2f);
            main.addCell(msgCell);
        }

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