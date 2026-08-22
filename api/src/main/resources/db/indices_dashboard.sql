-- =========================================================
-- ÍNDICES PARA EL DASHBOARD (Fase 4)
-- Aplicar manualmente sobre la base pedidos_system
-- ddl-auto: update NO crea índices
-- =========================================================

-- Acelera métricas temporales (día/semana/mes)
-- y el ORDER BY fecha_registro DESC de los últimos 10
CREATE INDEX idx_pedidos_fecha_registro
    ON pedidos (fecha_registro);

-- Vuelve el GROUP BY estado un index-only scan
CREATE INDEX idx_pedidos_estado
    ON pedidos (estado);
