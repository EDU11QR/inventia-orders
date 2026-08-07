package com.edudev.pedidos_api.repository;

import com.edudev.pedidos_api.entity.EstadoPedido;
import com.edudev.pedidos_api.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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

    // ==========================================
    // pedidos registrados dentro del rango
    // [inicio, finExclusivo) sobre fechaRegistro
    // consulta ejecutada directamente en MySQL
    // ==========================================
    @Query("""
            SELECT p
            FROM Pedido p
            WHERE p.fechaRegistro >= :inicio
              AND p.fechaRegistro < :finExclusivo
            ORDER BY p.fechaRegistro DESC, p.id DESC
            """)
    List<Pedido> findByFechaRegistroEntre(
            @Param("inicio") LocalDateTime inicio,
            @Param("finExclusivo") LocalDateTime finExclusivo
    );

}
