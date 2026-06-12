-- H2 test seed data — runs after drop-and-create
-- Tables with created_at/updated_at NOT NULL need explicit timestamps

-- H2 alias for MySQL TIME_TO_SEC function (not natively supported by H2 2.x)
CREATE ALIAS IF NOT EXISTS TIME_TO_SEC AS 'int timeToSec(String t) throws Exception { if (t == null) return 0; String[] p = t.split(":"); return Integer.parseInt(p[0]) * 3600 + Integer.parseInt(p[1]) * 60 + Integer.parseInt(p[2]); }';

-- H2 aliases for MySQL spatial functions used in the ELSE branch of ROUTE_BY_ID_WITH_DISTANCE_QUERY.
-- That branch is never executed in tests because all seed shapes have shape_dist_traveled > 0.
-- Aliases are required so H2 can parse (not just execute) the native SQL query.
CREATE ALIAS IF NOT EXISTS POINT AS 'byte[] mkPoint(double x, double y) { java.nio.ByteBuffer b = java.nio.ByteBuffer.allocate(21); b.order(java.nio.ByteOrder.LITTLE_ENDIAN); b.put((byte)1); b.putInt(1); b.putDouble(x); b.putDouble(y); return b.array(); }';
CREATE ALIAS IF NOT EXISTS ST_DISTANCE_SPHERE AS 'double stDistanceSphere(byte[] p1, byte[] p2) { return 0.0; }';

-- upload_metadata (H2 auto-creates from entity, but seed for tests)


INSERT INTO roles (name, description, created_at, updated_at) VALUES ('ADMIN', 'Administrador del sistema', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO roles (name, description, created_at, updated_at) VALUES ('CEO', 'Chief Executive Officer', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO roles (name, description, created_at, updated_at) VALUES ('COO', 'Chief Operating Officer', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO roles (name, description, created_at, updated_at) VALUES ('CMO', 'Chief Marketing Officer', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO users (email, firebase_uuid, first_name, last_name, role_id, status, created_at, updated_at) VALUES ('admin@rutamx.com', 'seed-admin-placeholder', 'Admin', 'RutaMx', 1, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO users (email, firebase_uuid, first_name, last_name, role_id, status, created_at, updated_at) VALUES ('other@rutamx.com', 'seed-other-placeholder', 'Other', 'User', 2, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Bus models (HU05)
INSERT INTO bus_models (name, manufacturer, fuel_type, autonomy_km, passenger_capacity, unit_cost_usd, battery_capacity_kwh, energy_consumption_kwh_km, fuel_consumption_l_km, maintenance_cost_per_km, co2_emissions_g_km, created_at, updated_at) VALUES ('Yutong E12PRO', 'Yutong', 'ELECTRIC', 300, 85, 420000, 352.08, 1.0, 0.0, 0.12, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO bus_models (name, manufacturer, fuel_type, autonomy_km, passenger_capacity, unit_cost_usd, battery_capacity_kwh, energy_consumption_kwh_km, fuel_consumption_l_km, maintenance_cost_per_km, co2_emissions_g_km, created_at, updated_at) VALUES ('Yutong ZK5120C', 'Yutong', 'ELECTRIC', 130, 85, 300000, 127.51, 1.0, 0.0, 0.12, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO bus_models (name, manufacturer, fuel_type, autonomy_km, passenger_capacity, unit_cost_usd, battery_capacity_kwh, energy_consumption_kwh_km, fuel_consumption_l_km, maintenance_cost_per_km, co2_emissions_g_km, created_at, updated_at) VALUES ('Yutong ZK5180C', 'Yutong', 'ELECTRIC', 120, 140, 550000, 155.33, 1.3, 0.0, 0.15, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO bus_models (name, manufacturer, fuel_type, autonomy_km, passenger_capacity, unit_cost_usd, battery_capacity_kwh, energy_consumption_kwh_km, fuel_consumption_l_km, maintenance_cost_per_km, co2_emissions_g_km, created_at, updated_at) VALUES ('Yutong DMT Hybrid H8', 'Yutong', 'DIESEL', 400, 61, 120000, 0.0, 0.0, 0.35, 0.22, 940, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO bus_models (name, manufacturer, fuel_type, autonomy_km, passenger_capacity, unit_cost_usd, battery_capacity_kwh, energy_consumption_kwh_km, fuel_consumption_l_km, maintenance_cost_per_km, co2_emissions_g_km, created_at, updated_at) VALUES ('Yutong DMT Hybrid H10', 'Yutong', 'DIESEL', 400, 80, 150000, 0.0, 0.0, 0.40, 0.25, 1070, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO bus_models (name, manufacturer, fuel_type, autonomy_km, passenger_capacity, unit_cost_usd, battery_capacity_kwh, energy_consumption_kwh_km, fuel_consumption_l_km, maintenance_cost_per_km, co2_emissions_g_km, created_at, updated_at) VALUES ('Yutong DMT Hybrid H12', 'Yutong', 'DIESEL', 700, 87, 200000, 0.0, 0.0, 0.35, 0.22, 940, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- GTFS seed data for route distance computation (HU05)
INSERT INTO agency (agency_id, agency_name, agency_url, agency_timezone, agency_lang, agency_color) VALUES ('SEMOVI', 'SEMOVI', 'https://semovi.cdmx.gob.mx/', 'America/Mexico_City', 'es', '009B3A');
INSERT INTO agency (agency_id, agency_name, agency_url, agency_timezone, agency_lang, agency_color) VALUES ('TROLE', 'Servicio de Transportes Electricos', 'https://www.ste.cdmx.gob.mx/', 'America/Mexico_City', 'es', '1F5AF0');

INSERT INTO calendar (service_id, monday, tuesday, wednesday, thursday, friday, saturday, sunday, start_date, end_date) VALUES ('TR13_SERVICE', 1, 1, 1, 1, 1, 1, 1, '2026-01-01', '2026-12-31');
INSERT INTO calendar (service_id, monday, tuesday, wednesday, thursday, friday, saturday, sunday, start_date, end_date) VALUES ('B_0', 1, 1, 1, 1, 1, 1, 1, '2024-12-01', '2025-12-31');

INSERT INTO routes (route_id, agency_id, route_short_name, route_long_name, route_type) VALUES ('TR13', 'SEMOVI', '13', 'Trolebus Linea 13', 11);
INSERT INTO routes (route_id, agency_id, route_short_name, route_long_name, route_type) VALUES ('TEST_ROUTE', 'TROLE', 'T1', 'Ruta de prueba', 3);

INSERT INTO trips (trip_id, route_id, service_id, shape_id, trip_headsign, trip_short_name, direction_id) VALUES ('TR13_TRIP_1', 'TR13', 'TR13_SERVICE', 'TR13_1', NULL, NULL, 0);
INSERT INTO trips (trip_id, route_id, service_id, shape_id, trip_headsign, trip_short_name, direction_id) VALUES ('TEST_TRIP_1', 'TEST_ROUTE', 'B_0', 'TEST_SHAPE_1', NULL, NULL, 0);

INSERT INTO shapes (shape_id, shape_pt_lat, shape_pt_lon, shape_pt_sequence, shape_dist_traveled) VALUES ('TR13_1', 19.345718, -99.065139, 1, 0.0);
INSERT INTO shapes (shape_id, shape_pt_lat, shape_pt_lon, shape_pt_sequence, shape_dist_traveled) VALUES ('TR13_1', 19.355000, -99.070000, 2, 5.5);
INSERT INTO shapes (shape_id, shape_pt_lat, shape_pt_lon, shape_pt_sequence, shape_dist_traveled) VALUES ('TR13_1', 19.365000, -99.080000, 3, 12.3);
INSERT INTO shapes (shape_id, shape_pt_lat, shape_pt_lon, shape_pt_sequence, shape_dist_traveled) VALUES ('TR13_1', 19.375000, -99.090000, 4, 20.0);
INSERT INTO shapes (shape_id, shape_pt_lat, shape_pt_lon, shape_pt_sequence, shape_dist_traveled) VALUES ('TEST_SHAPE_1', 19.400000, -99.100000, 1, 0.0);
INSERT INTO shapes (shape_id, shape_pt_lat, shape_pt_lon, shape_pt_sequence, shape_dist_traveled) VALUES ('TEST_SHAPE_1', 19.410000, -99.110000, 2, 15.0);

-- Stops required as FK for stop_times (HU19)
INSERT INTO stops (stop_id, stop_name, stop_lat, stop_lon) VALUES ('STOP_A', 'Parada Inicio', 19.345718, -99.065139);
INSERT INTO stops (stop_id, stop_name, stop_lat, stop_lon) VALUES ('STOP_B', 'Parada Final', 19.375000, -99.090000);

-- stop_times for TR13_TRIP_1: 08:00 → 09:00 = 60 min scheduled (HU19)
INSERT INTO stop_times (trip_id, stop_id, stop_sequence, arrival_time, departure_time) VALUES ('TR13_TRIP_1', 'STOP_A', 1, '08:00:00', '08:00:00');
INSERT INTO stop_times (trip_id, stop_id, stop_sequence, arrival_time, departure_time) VALUES ('TR13_TRIP_1', 'STOP_B', 2, '09:00:00', '09:00:00');

-- frequency for TR13_TRIP_1: 180 s headway → 3 min (HU19)
INSERT INTO frequencies (trip_id, start_time, end_time, headway_secs) VALUES ('TR13_TRIP_1', '06:00:00', '22:00:00', 180);

-- ── HU12 test data: MB Línea 1 route with demand and frequency ──────────────
INSERT INTO agency (agency_id, agency_name, agency_url, agency_timezone, agency_lang, agency_color)
  VALUES ('MB', 'Metrobús', 'https://www.metrobus.cdmx.gob.mx', 'America/Mexico_City', 'es', 'D40D0D');

INSERT INTO calendar (service_id, monday, tuesday, wednesday, thursday, friday, saturday, sunday, start_date, end_date)
  VALUES ('MB_SERVICE', 1, 1, 1, 1, 1, 1, 1, '2023-01-01', '2023-12-31');

INSERT INTO routes (route_id, agency_id, route_short_name, route_long_name, route_type)
  VALUES ('B_CMX0300L1', 'MB', '1', 'Indios Verdes - El Caminero', 3);

INSERT INTO trips (trip_id, route_id, service_id, shape_id, trip_headsign, trip_short_name, direction_id)
  VALUES ('MB1_TRIP_1', 'B_CMX0300L1', 'MB_SERVICE', 'MB1_SHAPE', NULL, NULL, 0);

INSERT INTO shapes (shape_id, shape_pt_lat, shape_pt_lon, shape_pt_sequence, shape_dist_traveled) VALUES ('MB1_SHAPE', 19.4950, -99.1185, 1, 0.0);
INSERT INTO shapes (shape_id, shape_pt_lat, shape_pt_lon, shape_pt_sequence, shape_dist_traveled) VALUES ('MB1_SHAPE', 19.3650, -99.1542, 2, 28.5);

-- frequency: 180 s headway → 3 min
INSERT INTO frequencies (trip_id, start_time, end_time, headway_secs) VALUES ('MB1_TRIP_1', '05:00:00', '23:00:00', 180);

-- Afluencia for linea 1: 2 weekdays (Mon/Tue) + 1 saturday + 1 sunday
-- Using small values so at least some bus models meet the required capacity
-- 2023-11-06 = Monday, 2023-11-07 = Tuesday, 2023-11-11 = Saturday, 2023-11-12 = Sunday
INSERT INTO afluencia_metrobus (fecha, mes, anio, linea, tipo_pago, afluencia) VALUES ('2023-11-06', 'Noviembre', 2023, 'linea 1', 'Prepago', 4500.0);
INSERT INTO afluencia_metrobus (fecha, mes, anio, linea, tipo_pago, afluencia) VALUES ('2023-11-06', 'Noviembre', 2023, 'linea 1', 'Gratuidad', 500.0);
INSERT INTO afluencia_metrobus (fecha, mes, anio, linea, tipo_pago, afluencia) VALUES ('2023-11-07', 'Noviembre', 2023, 'linea 1', 'Prepago', 4600.0);
INSERT INTO afluencia_metrobus (fecha, mes, anio, linea, tipo_pago, afluencia) VALUES ('2023-11-07', 'Noviembre', 2023, 'linea 1', 'Gratuidad', 460.0);
INSERT INTO afluencia_metrobus (fecha, mes, anio, linea, tipo_pago, afluencia) VALUES ('2023-11-11', 'Noviembre', 2023, 'linea 1', 'Prepago', 3000.0);
INSERT INTO afluencia_metrobus (fecha, mes, anio, linea, tipo_pago, afluencia) VALUES ('2023-11-11', 'Noviembre', 2023, 'linea 1', 'Gratuidad', 300.0);
INSERT INTO afluencia_metrobus (fecha, mes, anio, linea, tipo_pago, afluencia) VALUES ('2023-11-12', 'Noviembre', 2023, 'linea 1', 'Prepago', 2000.0);
INSERT INTO afluencia_metrobus (fecha, mes, anio, linea, tipo_pago, afluencia) VALUES ('2023-11-12', 'Noviembre', 2023, 'linea 1', 'Gratuidad', 200.0);
