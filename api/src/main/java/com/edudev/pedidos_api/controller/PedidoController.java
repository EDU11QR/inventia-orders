package com.edudev.pedidos_api.controller;

import com.edudev.pedidos_api.dto.PedidoDTO;
import com.edudev.pedidos_api.entity.EstadoPedido;
import com.edudev.pedidos_api.entity.Pedido;
import com.edudev.pedidos_api.service.PedidoService;
//import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@CrossOrigin("*")
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    public Pedido recibirPedido(@RequestBody PedidoDTO pedidoDTO) {

        System.out.println("\n========================");
        System.out.println("📦 NUEVO PEDIDO RECIBIDO");
        System.out.println("========================");

        return pedidoService.guardarPedido(pedidoDTO);
    }
    // =========================================================
    // LISTAR TODOS LOS PEDIDOS
    // =========================================================
    // Endpoint utilizado por el panel administrativo
    // para mostrar todos los pedidos registrados
    // =========================================================
    @GetMapping
    public List<Pedido> listarPedidos() {

        return pedidoService.listarPedidos();
    }

    @GetMapping("/exportar-excel")
    public ResponseEntity<?> exportarPedidosExcel(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaInicio,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaFin
    ) {

        if (fechaInicio.isAfter(fechaFin)) {

            return ResponseEntity.badRequest()
                    .body("La fecha de inicio no puede ser posterior a la fecha de fin");
        }

        byte[] excel =
                pedidoService.exportarPedidosExcel(
                        fechaInicio,
                        fechaFin
                );

        if (excel == null) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No se encontraron pedidos en el rango de fechas seleccionado");
        }

        String nombreArchivo = "pedidos_"
                + fechaInicio
                + "_al_"
                + fechaFin
                + ".xlsx";

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + nombreArchivo + "\""
                )
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .body(excel);
    }

    // =========================================================
// GENERAR PDF DE PEDIDOS PENDIENTES
// =========================================================
    @GetMapping("/pdf")
    public ResponseEntity<Resource> generarPdfPendientes()
            throws Exception {

        String archivo =
                pedidoService.generarPedidosPendientesPdf();

        if (archivo == null) {

            return ResponseEntity.notFound().build();
        }

        Path path = Paths.get(archivo);

        Resource resource =
                new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename="
                                + resource.getFilename()
                )
                .contentType(
                        MediaType.APPLICATION_PDF
                )
                .body(resource);
    }

//    @GetMapping("/pdf")
//    public ResponseEntity<Resource> generarPdfPendientes()
//            throws Exception {
//
//        // generar pdf
//        String archivo =
//                pedidoService.generarPedidosPendientesPdf();
//
//        // ruta archivo pdf
//        Path path = Paths.get(archivo);
//
//        // convertir archivo a resource
//        Resource resource =
//                new UrlResource(path.toUri());
//
//        return ResponseEntity.ok()
//
//                // descarga automática
//                .header(
//                        HttpHeaders.CONTENT_DISPOSITION,
//                        "attachment; filename="
//                                + resource.getFilename()
//                )
//
//                .contentType(MediaType.APPLICATION_PDF)
//
//                .body(resource);
//    }

    // =========================================================
    // ACTUALIZAR ESTADO DEL PEDIDO
    // =========================================================
    @PutMapping("/{id}/estado")
    public Pedido actualizarEstado(

            @PathVariable Long id,

            @RequestParam EstadoPedido estado
    ) {

        return pedidoService.actualizarEstado(id, estado);
    }

    @PutMapping("/{id}/cancelar")
    public Pedido cancelarPedido(
            @PathVariable Long id,
            @RequestBody PedidoDTO dto
    ){
        return pedidoService.cancelarPedido(
                id,
                dto.getMotivoCancelacion()
        );
    }

    // =========================================================
    // EDITAR PEDIDO
    // =========================================================
    @PutMapping("/{id}")
    public Pedido actualizarPedido(

            @PathVariable Long id,

            @RequestBody PedidoDTO dto
    ) {

        return pedidoService.actualizarPedido(id, dto);
    }

    // ==========================================
// imprimir pedido individual
// ==========================================

    @PostMapping("/{id}/imprimir")

    public ResponseEntity<byte[]> imprimirPedido(
            @PathVariable Long id
    ) {

        byte[] pdf = pedidoService.imprimirPedido(id);

        return ResponseEntity.ok()

                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=etiqueta.pdf"
                )

                .contentType(
                        MediaType.APPLICATION_PDF
                )

                .body(pdf);
    }

}
