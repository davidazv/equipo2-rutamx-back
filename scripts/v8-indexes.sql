-- =============================================================
-- v8-indexes.sql — Índices propuestos para §4.5 (seccion-4-base-de-datos.md)
-- Aplicar después de capturar baseline en Postman Performance Test.
-- =============================================================
USE rutamx;

-- O1: cubriente para Q1 (travel-times) — subqueries sched + trips_freq
-- Cubre: filter stop_sequence=1, MIN(departure_time), MAX(arrival_time) por trip_id
CREATE INDEX idx_stop_times_cover
    ON stop_times (trip_id, stop_sequence, arrival_time, departure_time);

-- O2: cubriente para MAX(shape_dist_traveled) por shape_id
-- Beneficia Q1 (subquery rd) y Q3 (SP cmo dashboard)
CREATE INDEX idx_shapes_dist_cover
    ON shapes (shape_id, shape_dist_traveled);

-- O5: cubriente para Q2 (shapes endpoint) — elimina sort y lookup
CREATE INDEX idx_shapes_full_cover
    ON shapes (shape_id, shape_pt_sequence, shape_pt_lon, shape_pt_lat, shape_dist_traveled);

-- Verificación post-aplicación
SHOW INDEX FROM stop_times WHERE Key_name = 'idx_stop_times_cover';
SHOW INDEX FROM shapes WHERE Key_name IN ('idx_shapes_dist_cover','idx_shapes_full_cover');
