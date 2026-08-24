package com.edudev.pedidos_api.repository;

import com.edudev.pedidos_api.dto.projection.EstadoCountProjection;
import com.edudev.pedidos_api.dto.projection.PeriodoCountProjection;
import com.edudev.pedidos_api.dto.projection.VendedorRankingProjection;
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

    // ==========================================
    // métricas temporales del dashboard
    // una sola pasada sobre el mes en curso
    // usando el índice de fecha_registro
    // ==========================================
    @Query(nativeQuery = true, value = """
            SELECT
                COALESCE(SUM(p.fecha_registro >= :inicioDia), 0) AS pedidosHoy,
                COALESCE(SUM(p.fecha_registro >= :inicioSemana), 0) AS pedidosSemana,
                COUNT(*) AS pedidosMes
            FROM pedidos p
            WHERE p.fecha_registro >= :inicioMes
            """)
    PeriodoCountProjection contarPedidosPorPeriodo(
            @Param("inicioDia") LocalDateTime inicioDia,
            @Param("inicioSemana") LocalDateTime inicioSemana,
            @Param("inicioMes") LocalDateTime inicioMes
    );

    // ==========================================
    // conteos por estado (histórico global)
    // ==========================================
    @Query("""
            SELECT p.estado AS estado, COUNT(p) AS total
            FROM Pedido p
            GROUP BY p.estado
            """)
    List<EstadoCountProjection> contarPedidosPorEstado();

    // ==========================================
    // últimos 10 pedidos para el dashboard
    // ==========================================
    List<Pedido> findTop10ByOrderByFechaRegistroDesc();

    // ==========================================
    // ranking comercial de vendedores
    // agrupado por vendedor_id (identidad real),
    // sumando todos sus pedidos aunque cambie
    // el nombre; se muestra el nombre más
    // reciente registrado para ese id,
    // con fallback al propio id si nunca tuvo nombre
    // ==========================================
    @Query(nativeQuery = true, value = """
            SELECT
                p.vendedor_id AS vendedorId,
                COALESCE((
                    SELECT p2.vendedor_nombre
                    FROM pedidos p2
                    WHERE p2.vendedor_id = p.vendedor_id
                      AND p2.vendedor_nombre IS NOT NULL
                    ORDER BY p2.fecha_registro DESC, p2.id DESC
                    LIMIT 1
                ), p.vendedor_id) AS vendedor,
                COUNT(*) AS total
            FROM pedidos p
            WHERE p.vendedor_id IS NOT NULL
            GROUP BY p.vendedor_id
            ORDER BY COUNT(*) DESC
            """)
    List<VendedorRankingProjection> obtenerRankingVendedores();

}
