-- H2 test seed data — runs after drop-and-create
-- Tables with created_at/updated_at NOT NULL need explicit timestamps

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
INSERT INTO agency (agency_id, agency_name, agency_url, agency_timezone, agency_lang) VALUES ('SEMOVI', 'SEMOVI', 'https://semovi.cdmx.gob.mx/', 'America/Mexico_City', 'es');
INSERT INTO agency (agency_id, agency_name, agency_url, agency_timezone, agency_lang) VALUES ('TROLE', 'Servicio de Transportes Electricos', 'https://www.ste.cdmx.gob.mx/', 'America/Mexico_City', 'es');

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
