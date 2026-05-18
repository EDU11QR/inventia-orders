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
        public String generarTicket(Pedido pedido){

        try {

            // ==========================================
            // crear carpeta pdfs
            // ==========================================
            new File("pdfs").mkdirs();

            // ==========================================
            // nombre archivo
            // ==========================================
            String archivo =
                    "pdfs/ticket_"
                            + System.currentTimeMillis()
                            + ".pdf";

            // ==========================================
            // tamaño real etiqueta 10.5cm x 10.5cm
            // 105 mm = 297.64 puntos PDF
            // ==========================================

            float width = 297.64f;
            float height = 297.64f;

            Rectangle pageSize =
                    new Rectangle(width, height);

            Document document =
                    new Document(
                            pageSize,
                            0,
                            0,
                            0,
                            0
                    );

            PdfWriter.getInstance(
                    document,
                    new FileOutputStream(archivo)
            );

            document.open();

            // ==========================================
            // fuentes
            // ==========================================
            Font titleFont = new Font(
                    Font.HELVETICA,
                    18,
                    Font.BOLD,
                    new Color(30, 30, 30)
            );

            Font subtitleFont = new Font(
                    Font.HELVETICA,
                    10,
                    Font.NORMAL,
                    Color.GRAY
            );

            Font labelFont = new Font(
                    Font.HELVETICA,
                    12,
                    Font.BOLD,
                    new Color(120,120,120)
            );

            Font valueFont = new Font(
                    Font.HELVETICA,
                    12,
                    Font.NORMAL,
                    new Color(30,30,30)
            );

            Font footerFont = new Font(
                    Font.HELVETICA,
                    10,
                    Font.BOLD,
                    Color.BLACK
            );

//            // ==========================================
//            // titulo
//            // ==========================================
//            Paragraph titulo = new Paragraph(
//                    "REPORTE PEDIDOS",
//                    titleFont
//            );
//
//            titulo.setAlignment(
//                    Element.ALIGN_CENTER
//            );
//
//            titulo.setSpacingBefore(5);
//            titulo.setSpacingAfter(3);
//
//            document.add(titulo);
//
//            // ==========================================
//            // fecha general
//            // ==========================================
//            Paragraph fechaGeneral = new Paragraph(
//                    "Generado: "
//                            + java.time.LocalDateTime.now()
//                            .format(
//                                    DateTimeFormatter.ofPattern(
//                                            "dd/MM/yyyy HH:mm"
//                                    )
//                            ),
//                    subtitleFont
//            );
//
//            fechaGeneral.setAlignment(
//                    Element.ALIGN_CENTER
//            );
//
//            fechaGeneral.setSpacingAfter(10);
//
//            document.add(fechaGeneral);

            // ==========================================
            // recorrer pedidos
            // ==========================================

            int contador = 1;

//            for (Pedido pedido : pedidos) {
//                    .........
//                      contador++;
//            }

            // ======================================
            // contenedor principal
            // ======================================

            PdfPTable container =
                    new PdfPTable(1);

            container.setWidthPercentage(92);

            PdfPCell cell =
                    new PdfPCell();

            cell.setBorderWidth(2f);

            cell.setPadding(15);

            cell.setBorderColor(
                    new Color(180, 180, 180)
            );


            // ==========================================
            // logo
            // ==========================================
            try {

                Image logo = Image.getInstance(
                        getClass().getResource(
                                "/static/logo_lady.png"
                        )
                );

                logo.scaleToFit(55, 55);

                logo.setAlignment(
                        Element.ALIGN_CENTER
                );

                document.add(logo);

            } catch (Exception e) {

                System.out.println(
                        "No se pudo cargar logo"
                );
            }

            // ======================================
            // linea superior
            // ======================================
            Paragraph linea = new Paragraph(
                    "----------------------------",
                    subtitleFont
            );

            linea.setAlignment(
                    Element.ALIGN_CENTER
            );

            document.add(linea);

            Paragraph pedidoTitle = new Paragraph(

                    "PEDIDO #" + pedido.getId(),

                    new Font(
                            Font.HELVETICA,
                            20,
                            Font.BOLD,
                            new Color(35,35,35)
                    )
            );

            pedidoTitle.setAlignment(
                    Element.ALIGN_CENTER
            );

            pedidoTitle.setSpacingBefore(4);
            pedidoTitle.setSpacingAfter(6);

            document.add(pedidoTitle);


            // ======================================
// TABLA CENTRAL DE DATOS
// ======================================

            PdfPTable dataTable = new PdfPTable(2);

            dataTable.setWidthPercentage(88);

            dataTable.setHorizontalAlignment(
                    Element.ALIGN_CENTER
            );

            dataTable.setSpacingBefore(5);

            dataTable.setSpacingAfter(5);

            dataTable.setWidths(
                    new float[]{2f, 3f}
            );

// ======================================
// ESTILOS
// ======================================

            Font dataLabelFont = new Font(
                    Font.HELVETICA,
                    13,
                    Font.BOLD,
                    Color.BLACK
            );

            Font dataValueFont = new Font(
                    Font.HELVETICA,
                    13,
                    Font.NORMAL,
                    Color.BLACK
            );

// ======================================
// CLIENTE
// ======================================

            agregarFila(
                    dataTable,
                    "Cliente",
                    pedido.getCliente(),
                    dataLabelFont,
                    dataValueFont
            );

// ======================================
// DNI
// ======================================

            agregarFila(
                    dataTable,
                    "DNI",
                    pedido.getDni(),
                    dataLabelFont,
                    dataValueFont
            );

// ======================================
// TELEFONO
// ======================================

            agregarFila(
                    dataTable,
                    "Teléfono",
                    pedido.getTelefono(),
                    dataLabelFont,
                    dataValueFont
            );

// ======================================
// DIRECCION
// ======================================

            agregarFila(
                    dataTable,
                    "Dirección",
                    pedido.getDireccion(),
                    dataLabelFont,
                    dataValueFont
            );

// ======================================
// PRODUCTO
// ======================================

            agregarFila(
                    dataTable,
                    "Producto",
                    pedido.getProducto(),
                    dataLabelFont,
                    dataValueFont
            );

// ======================================
// AGREGAR TABLA
// ======================================

            document.add(dataTable);



            // ==========================================
            // total pedidos
            // ==========================================
//            Paragraph total = new Paragraph(
//                    "TOTAL: " + pedidos.size(),
//                    titleFont
//            );
//
//            total.setAlignment(
//                    Element.ALIGN_CENTER
//            );
//
//            total.setSpacingBefore(10);
//
//            document.add(total);

            // ==========================================
            // footer
            // ======================================
            // fecha pedido
            // ======================================

            Paragraph fechaPedido = new Paragraph(

                    "Fecha: "
                            + pedido.getFechaRegistro()
                            .format(
                                    DateTimeFormatter.ofPattern(
                                            "dd/MM/yyyy HH:mm"
                                    )
                            ),

                    footerFont
            );

            fechaPedido.setAlignment(
                    Element.ALIGN_CENTER
            );

            fechaPedido.setSpacingBefore(10);

            document.add(fechaPedido);

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
}