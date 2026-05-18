package com.edudev.pedidos_api.repository;

import com.edudev.pedidos_api.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import com.edudev.pedidos_api.entity.EstadoPedido;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByEstado(EstadoPedido estado);

    // obtener pedidos ordenados por fecha descendente
    List<Pedido> findAllByOrderByFechaRegistroDesc();

    // ==========================================
    // verificar si mensaje whatsapp ya existe
    // evita pedidos duplicados
    // ==========================================
    boolean existsByMessageId(String messageId);

    // entra en cola cronológicamente
    List<Pedido> findByEstadoOrderByFechaRegistroAsc(
            EstadoPedido estado
    );

}
