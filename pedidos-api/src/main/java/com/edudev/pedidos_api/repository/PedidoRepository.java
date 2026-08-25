package com.edudev.pedidos_api.repository;

import com.edudev.pedidos_api.dto.projection.EstadoCountProjection;
import com.edudev.pedidos_api.dto.projection.PeriodoCountProjection;
import com.edudev.pedidos_api.dto.projection.SerieTemporalProjection;
import com.edudev.pedidos_api.dto.projection.VendedorOpcionProjection;
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
    // pedidos de un vendedor específico
    // (filtro ?vendedorId del listado)
    // ==========================================
    List<Pedido> findByVendedorIdOrderByFechaRegistroDesc(
            String vendedorId
    );

    // ==========================================
    // vendedores con al menos un pedido
    // agrupado por vendedor_id; se muestra el nombre
    // más reciente registrado para ese id,
    // con fallback al propio id si nunca tuvo nombre;
    // ordenado alfabéticamente para el selector
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
                ), p.vendedor_id) AS vendedor
            FROM pedidos p
            WHERE p.vendedor_id IS NOT NULL
            GROUP BY p.vendedor_id
            ORDER BY vendedor
            """)
    List<VendedorOpcionProjection> obtenerVendedoresConPedidos();

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

    // ==========================================
    // ranking de vendedores filtrado por ventana
    // temporal [inicio, fin); misma lógica de
    // agrupación por vendedor_id y nombre más reciente
    // que el ranking global
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
              AND p.fecha_registro >= :inicio
              AND p.fecha_registro < :fin
            GROUP BY p.vendedor_id
            ORDER BY COUNT(*) DESC
            """)
    List<VendedorRankingProjection> obtenerRankingVendedoresPorPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    // ==========================================
    // series temporales del panel
    // "Pedidos por Hora" (agrupan por fecha_registro;
    // solo devuelven los tramos con pedidos, el front
    // rellena los huecos con ceros)
    // ==========================================

    // hoy: agrupado por hora del día (00-23)
    // se usa DATE_FORMAT (y no CAST/HOUR) para que la
    // expresión del SELECT sea idéntica a la del GROUP BY
    // y pasar el modo only_full_group_by de MySQL
    @Query(nativeQuery = true, value = """
            SELECT
                DATE_FORMAT(p.fecha_registro, '%H') AS etiqueta,
                COUNT(*) AS total
            FROM pedidos p
            WHERE p.fecha_registro >= :inicio
              AND p.fecha_registro < :fin
            GROUP BY DATE_FORMAT(p.fecha_registro, '%H')
            ORDER BY DATE_FORMAT(p.fecha_registro, '%H')
            """)
    List<SerieTemporalProjection> contarPedidosPorHora(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    // semana: agrupado por día natural
    @Query(nativeQuery = true, value = """
            SELECT
                DATE_FORMAT(p.fecha_registro, '%Y-%m-%d') AS etiqueta,
                COUNT(*) AS total
            FROM pedidos p
            WHERE p.fecha_registro >= :inicio
              AND p.fecha_registro < :fin
            GROUP BY DATE_FORMAT(p.fecha_registro, '%Y-%m-%d')
            ORDER BY DATE_FORMAT(p.fecha_registro, '%Y-%m-%d')
            """)
    List<SerieTemporalProjection> contarPedidosPorDia(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    // mes: agrupado por día del mes (01-31),
    // con la misma técnica DATE_FORMAT
    @Query(nativeQuery = true, value = """
            SELECT
                DATE_FORMAT(p.fecha_registro, '%d') AS etiqueta,
                COUNT(*) AS total
            FROM pedidos p
            WHERE p.fecha_registro >= :inicio
              AND p.fecha_registro < :fin
            GROUP BY DATE_FORMAT(p.fecha_registro, '%d')
            ORDER BY DATE_FORMAT(p.fecha_registro, '%d')
            """)
    List<SerieTemporalProjection> contarPedidosPorDiaDelMes(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

}
