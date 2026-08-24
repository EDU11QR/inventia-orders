package com.edudev.pedidos_api.service;

import com.edudev.pedidos_api.dto.PedidoDTO;
import com.edudev.pedidos_api.entity.EstadoPedido;
import com.edudev.pedidos_api.entity.Pedido;
import com.edudev.pedidos_api.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService {

    // Inyección automática del repository
    // Aquí manejamos todas las operaciones hacia la BD
    private final PedidoRepository pedidoRepository;

    // Servicio encargado de generar el PDF consolidado
    private final PdfService pdfService;

    private final ExcelService excelService;


    // =========================================================
    // GUARDAR NUEVO PEDIDO
    // =========================================================
    // Este método recibe la información del pedido desde WhatsApp
    // y construye una entidad Pedido para guardarla en MySQL
    // =========================================================
    public Pedido guardarPedido(PedidoDTO dto) {

        // ==========================================
        // evitar pedidos duplicados whatsapp
        // ==========================================
        System.out.println(
                "MESSAGE ID: "
                        + dto.getMessageId()
        );

        System.out.println(
                "CLIENTE: "
                        + dto.getCliente()
        );
        if (

                dto.getMessageId() != null &&

                        pedidoRepository.existsByMessageId(
                                dto.getMessageId()
                        )
        ) {

            System.out.println(
                    "⚠️ Pedido duplicado detectado"
            );

            return null;
        }
        System.out.println(
                "MESSAGE ID RECIBIDO: "
                        + dto.getMessageId()
        );

        // Construcción de la entidad Pedido usando Builder Pattern
        Pedido pedido = Pedido.builder()

                // Datos enviados desde el listener de WhatsApp
                .cliente(dto.getCliente())
                .dni(dto.getDni())
                .telefono(dto.getTelefono())
                .direccion(dto.getDireccion())
                .ciudad(dto.getCiudad())
                .producto(dto.getProducto())
                .messageId(dto.getMessageId())
                .remoteJid(dto.getRemoteJid())
                .vendedorNombre(dto.getVendedorNombre())
                .vendedorId(dto.getVendedorId())
                .fechaMensaje(dto.getFechaMensaje())
                //Todo pedido nuevo inicia como PENDIENTE
                .estado(EstadoPedido.PENDIENTE)
                // Fecha automática del registro
                .fechaRegistro(LocalDateTime.now())
                .build();

        // Guardar pedido en la base de datos
        return pedidoRepository.save(pedido);
    }

    // LISTAR TODOS LOS PEDIDOS
    // =========================================================
    // Obtiene pedidos ordenados desde el más reciente
    // hacia el más antiguo
    // =========================================================
    public List<Pedido> listarPedidos() {

        return pedidoRepository
                .findAllByOrderByFechaRegistroDesc();
    }

    // =========================================================
    // EXPORTAR PEDIDOS POR RANGO DE FECHA
    // =========================================================
    // Consulta exclusivamente los pedidos registrados dentro
    // del rango [fechaInicio, fechaFin + 1 dia) sobre
    // fechaRegistro, sin cargar toda la tabla
    // =========================================================
    public byte[] exportarPedidosExcel(
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {

        if (fechaInicio.isAfter(fechaFin)) {

            throw new IllegalArgumentException(
                    "La fecha de inicio no puede ser posterior a la fecha de fin"
            );
        }

        LocalDateTime inicio = fechaInicio.atStartOfDay();

        LocalDateTime finExclusivo =
                fechaFin.plusDays(1).atStartOfDay();

        List<Pedido> pedidos =
                pedidoRepository.findByFechaRegistroEntre(
                        inicio,
                        finExclusivo
                );

        if (pedidos.isEmpty()) {

            return null;
        }

        return excelService.generarExcel(pedidos);
    }

    // =========================================================
    // ACTUALIZAR ESTADO DEL PEDIDO
    // =========================================================
    // Permite cambiar el estado manualmente desde
    // el dashboard administrativo
    // =========================================================
    public Pedido actualizarEstado(Long id, EstadoPedido estado) {

        // buscar pedido por id
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Pedido no encontrado"));

        // actualizar estado
        pedido.setEstado(estado);

        // guardar cambios
        return pedidoRepository.save(pedido);
    }

    // =========================================================
    // CANCELAR PEDIDO
    // =========================================================
    // Permite cancelar un pedido registrando
    // motivo y fecha de cancelacion
    // =========================================================

    public Pedido cancelarPedido(Long id, String motivo){
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Pedido no encontrado"));

        pedido.setEstado(EstadoPedido.CANCELADO);
        pedido.setMotivoCancelacion(motivo);
        pedido.setFechaCancelacion(LocalDateTime.now());

        return pedidoRepository.save(pedido);
    }





    // =========================================================
// ACTUALIZAR PEDIDO
// =========================================================
// Permite editar manualmente información del pedido
// desde el dashboard administrativo
// =========================================================
    public Pedido actualizarPedido(Long id, PedidoDTO dto) {

        // buscar pedido
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Pedido no encontrado"));


        if (pedido.getEstado() == EstadoPedido.CANCELADO) {

            throw new RuntimeException(
                    "No se puede imprimir un pedido cancelado"
            );
        }

        // actualizar datos
        pedido.setCliente(dto.getCliente());
        pedido.setDni(dto.getDni());
        pedido.setTelefono(dto.getTelefono());
        pedido.setDireccion(dto.getDireccion());
        pedido.setCiudad(dto.getCiudad());
        pedido.setProducto(dto.getProducto());

        // guardar cambios
        return pedidoRepository.save(pedido);
    }


    // =========================================================
    // GENERAR PDF DE PEDIDOS PENDIENTES
    // =========================================================
    // Busca todos los pedidos con estado PENDIENTE,
    // genera un PDF consolidado y luego cambia el estado
    // de esos pedidos a IMPRESO
    // =========================================================
    public String generarPedidosPendientesPdf() {

        List<Pedido> pendientes =
                pedidoRepository.findByEstado(EstadoPedido.PENDIENTE);

        if (pendientes.isEmpty()) {

            return null;
        }

        // tomar solo el primer pedido pendiente
        Pedido pedido = pendientes.get(0);

        // generar ticket individual
        String archivo =
                pdfService.generarTicket(pedido);

        // actualizar estado
        pedido.setEstado(
                EstadoPedido.IMPRESO
        );

        pedidoRepository.save(pedido);

        return archivo;
    }

    public void procesarColaImpresion() {

        List<Pedido> pendientes =

                pedidoRepository
                        .findByEstadoOrderByFechaRegistroAsc(
                                EstadoPedido.PENDIENTE
                        );

        for (Pedido pedido : pendientes) {

            try {

                // ==============================
                // marcar en proceso
                // ==============================

                pedido.setEstado(
                        EstadoPedido.EN_PROCESO
                );

                pedidoRepository.save(pedido);

                // ==============================
                // generar ticket
                // ==============================

                pdfService.generarTicket(pedido);

                // ==============================
                // marcar impreso
                // ==============================

                pedido.setEstado(
                        EstadoPedido.IMPRESO
                );

                pedido.setFechaImpresion(
                        LocalDateTime.now()
                );

                pedidoRepository.save(pedido);

                System.out.println(
                        "✅ Pedido impreso: "
                                + pedido.getId()
                );

            } catch (Exception e) {

                pedido.setEstado(
                        EstadoPedido.ERROR_IMPRESION
                );

                pedidoRepository.save(pedido);

                System.out.println(
                        "❌ Error impresión pedido: "
                                + pedido.getId()
                );

                System.out.println(
                        e.getMessage()
                );
            }
        }
    }

    // ==========================================
// imprimir pedido individual
// ==========================================

    public byte[] imprimirPedido(Long id) {

        try {

            Pedido pedido =

                    pedidoRepository.findById(id)

                            .orElseThrow(() ->

                                    new RuntimeException(
                                            "Pedido no encontrado"
                                    )
                            );

            // ======================================
            // marcar en proceso
            // ======================================

            pedido.setEstado(
                    EstadoPedido.EN_PROCESO
            );

            pedidoRepository.save(pedido);

            // ======================================
            // generar ticket pdf
            // ======================================

            String archivo =

                    pdfService.generarTicket(pedido);

            // ======================================
            // convertir pdf a bytes
            // ======================================

            byte[] pdf = java.nio.file.Files.readAllBytes(
                    java.nio.file.Paths.get(archivo)
            );

            // ======================================
            // marcar impreso
            // ======================================

            pedido.setEstado(
                    EstadoPedido.IMPRESO
            );

            pedido.setFechaImpresion(
                    LocalDateTime.now()
            );

            pedidoRepository.save(pedido);

            System.out.println(
                    "✅ Pedido impreso correctamente"
            );

            return pdf;

        } catch (Exception e) {

            e.printStackTrace();

            return null;
        }
    }

}
