-- ============================================================================
-- RutaMx — Esquema DDL MySQL (Completo)
-- Alcance: Usuarios/Roles, Catálogo de Autobuses, GTFS (agencias, rutas,
--          viajes, paradas, frecuencias, formas), Afluencia Metrobús
-- Motor:   MySQL 8.x
-- Stack:   Quarkus + Hibernate Panache
-- ============================================================================

SET NAMES utf8mb4;

DROP DATABASE IF EXISTS rutamx;
CREATE DATABASE IF NOT EXISTS rutamx
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE rutamx;

-- ────────────────────────────────────────────────────────────────────────────
-- 1. ROLES
--    Control de acceso basado en roles (RNF-SEG-03, RNF-SEG-04)
--    Roles previstos: ADMIN, CEO, COO, CMO
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE roles (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50)  NOT NULL,
    description VARCHAR(255) NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uq_roles_name UNIQUE (name)
) ENGINE=InnoDB;

-- ────────────────────────────────────────────────────────────────────────────
-- 2. USERS
--    Gestión de cuentas (HU01-HU04, HU24-HU25)
--    Autenticación por correo/contraseña (RNF-SEG-01)
--    Contraseñas con hash seguro (RNF-SEG-02)
--    Estado ACTIVE/SUSPENDED para suspensión inmediata (HU04)
--    Sesión expira a los 15 min de inactividad (RNF-SEG-05, app-level)
--    Política de contraseña: ≥8 chars, 1 mayúscula, 1 número (RNF-SEG-06, app-level)
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE users (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    email         VARCHAR(255)  NOT NULL,
    firebase_UUID VARCHAR(255)  NOT NULL,
    first_name    VARCHAR(100)  NULL,
    last_name     VARCHAR(100)  NULL,
    role_id       BIGINT        NOT NULL,
    status        ENUM('ACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT fk_users_role  FOREIGN KEY (role_id) REFERENCES roles (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE INDEX idx_users_status ON users (status);

-- ────────────────────────────────────────────────────────────────────────────
-- 3. BUS_MODELS
--    Catálogo de modelos de autobús eléctrico y diésel
--    CRUD eléctricos (HU13-HU15), consulta de especificaciones (HU08-HU09)
--    Comparativa eléctrico vs diésel (HU22-HU23)
--    Simulación de consumo de batería (HU16-HU17)
--    Cálculo de emisiones CO₂ (HU20)
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE bus_models (
    id                        BIGINT         NOT NULL AUTO_INCREMENT,
    name                      VARCHAR(150)   NOT NULL,
    manufacturer              VARCHAR(150)   NULL,
    fuel_type                 ENUM('ELECTRIC', 'DIESEL') NOT NULL,
    autonomy_km               DECIMAL(8,2)   NULL,
    passenger_capacity        INT            NULL,
    unit_cost_usd             DECIMAL(12,2)  NULL,
    battery_capacity_kwh      DECIMAL(8,2)   NULL,
    energy_consumption_kwh_km DECIMAL(6,4)   NULL,
    fuel_consumption_l_km     DECIMAL(6,4)   NULL,
    maintenance_cost_per_km   DECIMAL(8,4)   NULL,
    co2_emissions_g_km        DECIMAL(8,2)   NULL,
    created_at                TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uq_bus_models_name UNIQUE (name)
) ENGINE=InnoDB;

CREATE INDEX idx_bus_models_fuel_type ON bus_models (fuel_type);

-- ────────────────────────────────────────────────────────────────────────────
-- 4. AGENCY  (GTFS: agency.txt)
--    Agencias de transporte público (HU18)
--    agency_id es la PK natural del estándar GTFS
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE agency (
    agency_id       VARCHAR(50)   NOT NULL,
    agency_name     VARCHAR(150)  NOT NULL,
    agency_url      VARCHAR(255)  NULL,
    agency_timezone VARCHAR(50)   NOT NULL,
    agency_lang     VARCHAR(10)   NULL,
    agency_color    VARCHAR(10)   NULL,

    PRIMARY KEY (agency_id)
) ENGINE=InnoDB;

-- ────────────────────────────────────────────────────────────────────────────
-- 5. CALENDAR  (GTFS: calendar.txt)
--    Disponibilidad de servicio por día de semana (HU21)
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE calendar (
    service_id  VARCHAR(50)  NOT NULL,
    monday      TINYINT      NOT NULL DEFAULT 0,
    tuesday     TINYINT      NOT NULL DEFAULT 0,
    wednesday   TINYINT      NOT NULL DEFAULT 0,
    thursday    TINYINT      NOT NULL DEFAULT 0,
    friday      TINYINT      NOT NULL DEFAULT 0,
    saturday    TINYINT      NOT NULL DEFAULT 0,
    sunday      TINYINT      NOT NULL DEFAULT 0,
    start_date  DATE         NOT NULL,
    end_date    DATE         NOT NULL,

    PRIMARY KEY (service_id)
) ENGINE=InnoDB;

-- ────────────────────────────────────────────────────────────────────────────
-- 6. ROUTES  (GTFS: routes.txt)
--    Rutas de transporte público (HU18, HU20, HU21)
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE routes (
    route_id         VARCHAR(50)   NOT NULL,
    agency_id        VARCHAR(50)   NOT NULL,
    route_short_name VARCHAR(20)   NULL,
    route_long_name  VARCHAR(255)  NULL,
    route_type       INT           NOT NULL,
    route_color      VARCHAR(10)   NULL,
    route_text_color VARCHAR(10)   NULL,

    PRIMARY KEY (route_id),
    CONSTRAINT fk_routes_agency FOREIGN KEY (agency_id) REFERENCES agency (agency_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE INDEX idx_routes_agency ON routes (agency_id);

-- ────────────────────────────────────────────────────────────────────────────
-- 7. SHAPES  (GTFS: shapes.txt)
--    Trazado geográfico de rutas (HU19, visualización de mapas)
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE shapes (
    id                  BIGINT         NOT NULL AUTO_INCREMENT,
    shape_id            VARCHAR(50)    NOT NULL,
    shape_pt_lat        DECIMAL(10,7)  NOT NULL,
    shape_pt_lon        DECIMAL(10,7)  NOT NULL,
    shape_pt_sequence   INT            NOT NULL,
    shape_dist_traveled DECIMAL(10,4)  NULL,

    PRIMARY KEY (id),
    CONSTRAINT uq_shapes_seq UNIQUE (shape_id, shape_pt_sequence)
) ENGINE=InnoDB;

CREATE INDEX idx_shapes_shape_id ON shapes (shape_id);

-- ────────────────────────────────────────────────────────────────────────────
-- 8. TRIPS  (GTFS: trips.txt)
--    Viajes programados (HU19, HU21)
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE trips (
    trip_id         VARCHAR(100)  NOT NULL,
    route_id        VARCHAR(50)   NOT NULL,
    service_id      VARCHAR(50)   NOT NULL,
    shape_id        VARCHAR(50)   NULL,
    trip_headsign   VARCHAR(150)  NULL,
    trip_short_name VARCHAR(150)  NULL,
    direction_id    TINYINT       NULL,

    PRIMARY KEY (trip_id),
    CONSTRAINT fk_trips_route    FOREIGN KEY (route_id)   REFERENCES routes   (route_id)   ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_trips_calendar FOREIGN KEY (service_id) REFERENCES calendar (service_id) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE INDEX idx_trips_route     ON trips (route_id);
CREATE INDEX idx_trips_service   ON trips (service_id);
CREATE INDEX idx_trips_shape     ON trips (shape_id);

-- ────────────────────────────────────────────────────────────────────────────
-- 9. STOPS  (GTFS: stops.txt)
--    Paradas del sistema de transporte (HU11, HU12, HU19)
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE stops (
    stop_id             VARCHAR(100)   NOT NULL,
    stop_name           VARCHAR(255)   NOT NULL,
    stop_lat            DECIMAL(10,7)  NOT NULL,
    stop_lon            DECIMAL(10,7)  NOT NULL,
    zone_id             VARCHAR(50)    NULL,
    wheelchair_boarding TINYINT        NULL DEFAULT 0,

    PRIMARY KEY (stop_id)
) ENGINE=InnoDB;

-- ────────────────────────────────────────────────────────────────────────────
-- 10. STOP_TIMES  (GTFS: stop_times.txt)
--     Horarios por parada por viaje (HU10, HU11, HU19)
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE stop_times (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    trip_id        VARCHAR(100) NOT NULL,
    stop_id        VARCHAR(100) NOT NULL,
    stop_sequence  INT          NOT NULL,
    arrival_time   VARCHAR(8)   NULL,
    departure_time VARCHAR(8)   NULL,
    timepoint      TINYINT      NULL,

    PRIMARY KEY (id),
    CONSTRAINT uq_stop_times  UNIQUE (trip_id, stop_sequence),
    CONSTRAINT fk_stop_times_trip FOREIGN KEY (trip_id) REFERENCES trips (trip_id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_stop_times_stop FOREIGN KEY (stop_id) REFERENCES stops (stop_id) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE INDEX idx_stop_times_trip ON stop_times (trip_id);
CREATE INDEX idx_stop_times_stop ON stop_times (stop_id);

-- ────────────────────────────────────────────────────────────────────────────
-- 11. FREQUENCIES  (GTFS: frequencies.txt)
--     Frecuencias de servicio por viaje (HU10, HU19, HU21)
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE frequencies (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    trip_id      VARCHAR(100) NOT NULL,
    start_time   VARCHAR(8)   NOT NULL,
    end_time     VARCHAR(8)   NOT NULL,
    headway_secs INT          NOT NULL,
    exact_times  TINYINT      NULL DEFAULT 0,

    PRIMARY KEY (id),
    CONSTRAINT fk_frequencies_trip FOREIGN KEY (trip_id) REFERENCES trips (trip_id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_frequencies_trip ON frequencies (trip_id);

-- ────────────────────────────────────────────────────────────────────────────
-- 12. AFLUENCIA_METROBUS  (SEMOVI: afluencia_mb.csv)
--     Afluencia diaria de pasajeros por línea de Metrobús (HU10, HU11, HU18, HU20)
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE afluencia_metrobus (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    fecha      DATE          NOT NULL,
    mes        VARCHAR(20)   NOT NULL,
    anio       SMALLINT      NOT NULL,
    linea      VARCHAR(30)   NOT NULL,
    tipo_pago  VARCHAR(30)   NOT NULL,
    afluencia  DECIMAL(12,2) NOT NULL,

    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE INDEX idx_afluencia_fecha ON afluencia_metrobus (fecha);
CREATE INDEX idx_afluencia_linea ON afluencia_metrobus (linea);
CREATE INDEX idx_afluencia_anio  ON afluencia_metrobus (anio);



-- ────────────────────────────────────────────────────────────────────────────
-- 13. UPLOAD_METADATA
--     Tracks last CSV upload timestamp per table
-- ────────────────────────────────────────────────────────────────────────────
CREATE TABLE upload_metadata (
    table_name  VARCHAR(50) NOT NULL,
    uploaded_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (table_name)
) ENGINE=InnoDB;

-- Roles
INSERT INTO roles (name, description) VALUES
  ('ADMIN', 'Administrador del sistema'),
  ('CEO',   'Chief Executive Officer'),
  ('COO',   'Chief Operating Officer'),
  ('CMO',   'Chief Marketing Officer');

-- Usuario administrador por defecto
INSERT INTO users (email, firebase_UUID, first_name, last_name, role_id, status)
VALUES (
  'admin@rutamx.com',
  'R6OJ0u5lHVRWmSLazE0MlkweyMD3',
  'David',
  'Zárate',
  (SELECT id FROM roles WHERE name = 'ADMIN'),
  'ACTIVE'
);

-- ============================================================================
-- AUDITORIA + RUTINAS DE BASE DE DATOS
-- Set: 2 Functions, 2 Procedures, 3 Triggers, 1 Event
-- Documentacion: docs/mejoras-base-de-datos.md, docs/auditoria-prioridad.md
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Tabla de auditoria de usuarios (consumida por TR2 y E1)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users_audit (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    action      ENUM('INSERT','UPDATE','DELETE','SUSPEND_INACTIVE') NOT NULL,
    old_email   VARCHAR(255) NULL,
    new_email   VARCHAR(255) NULL,
    old_role    BIGINT       NULL,
    new_role    BIGINT       NULL,
    old_status  ENUM('ACTIVE','SUSPENDED') NULL,
    new_status  ENUM('ACTIVE','SUSPENDED') NULL,
    changed_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_users_audit_user (user_id),
    KEY idx_users_audit_action (action)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- STORED FUNCTIONS
-- ----------------------------------------------------------------------------

DROP FUNCTION IF EXISTS fn_co2_emissions;
DROP FUNCTION IF EXISTS fn_annual_km;

DELIMITER $$

-- FN1: kg de CO2 anuales = km_anual * consumo_por_km * factor_co2 / 1000
CREATE FUNCTION fn_co2_emissions(
    annual_km    DOUBLE,
    fuel_per_km  DOUBLE,
    co2_factor   DOUBLE
) RETURNS DOUBLE
DETERMINISTIC
BEGIN
    IF annual_km IS NULL OR fuel_per_km IS NULL OR co2_factor IS NULL THEN
        RETURN 0;
    END IF;
    RETURN annual_km * fuel_per_km * co2_factor / 1000.0;
END$$

-- FN2: km recorridos al ano = viajes_diarios * largo_ruta_km * dias_operativos
CREATE FUNCTION fn_annual_km(
    daily_trips     DOUBLE,
    route_length_km DOUBLE,
    operating_days  INT
) RETURNS DOUBLE
DETERMINISTIC
BEGIN
    IF daily_trips IS NULL OR route_length_km IS NULL OR operating_days IS NULL THEN
        RETURN 0;
    END IF;
    RETURN daily_trips * route_length_km * operating_days;
END$$

DELIMITER ;

-- ----------------------------------------------------------------------------
-- STORED PROCEDURES
-- ----------------------------------------------------------------------------

DROP PROCEDURE IF EXISTS sp_get_cmo_dashboard;
DROP PROCEDURE IF EXISTS sp_get_coo_dashboard;

DELIMITER $$

-- SP1: dashboard CMO. Result-set 1: rutas con metricas. Result-set 2: agencias.
CREATE PROCEDURE sp_get_cmo_dashboard(IN p_agency_id VARCHAR(50))
BEGIN
    DECLARE v_avg_diesel_l_km DOUBLE;
    DECLARE v_avg_kwh_km      DOUBLE;
    DECLARE v_diesel_co2      DOUBLE DEFAULT 2.68;   -- kg CO2 / litro diesel
    DECLARE v_electric_co2    DOUBLE DEFAULT 0.45;   -- kg CO2 / kWh red
    DECLARE v_operating_days  INT    DEFAULT 365;

    SELECT IFNULL(AVG(fuel_consumption_l_km), 0.4)
      INTO v_avg_diesel_l_km
      FROM bus_models
     WHERE fuel_type = 'DIESEL' AND fuel_consumption_l_km > 0;

    SELECT IFNULL(AVG(energy_consumption_kwh_km), 1.2)
      INTO v_avg_kwh_km
      FROM bus_models
     WHERE fuel_type = 'ELECTRIC' AND energy_consumption_kwh_km > 0;

    -- Result-set 1: rutas con metricas
    SELECT
        r.route_id,
        r.route_short_name,
        r.route_long_name,
        r.agency_id,
        r.route_color,
        IFNULL(rd.distancia_km, 0)                                AS distancia_km,
        IFNULL(tc.total_trips, 0)                                 AS total_trips,
        IFNULL(tc.total_trips / 7.0, 0)                           AS avg_daily_trips,
        IFNULL(ROUND(hw.avg_headway_secs / 60), 0)                AS headway_minutes,
        fn_annual_km(IFNULL(tc.total_trips / 7.0, 0),
                     IFNULL(rd.distancia_km, 0),
                     v_operating_days)                            AS annual_km,
        ROUND(fn_co2_emissions(
            fn_annual_km(IFNULL(tc.total_trips / 7.0, 0),
                         IFNULL(rd.distancia_km, 0),
                         v_operating_days),
            v_avg_diesel_l_km,
            v_diesel_co2) / 1000.0, 2)                            AS co2_diesel_ton_anio,
        ROUND(fn_co2_emissions(
            fn_annual_km(IFNULL(tc.total_trips / 7.0, 0),
                         IFNULL(rd.distancia_km, 0),
                         v_operating_days),
            v_avg_kwh_km,
            v_electric_co2) / 1000.0, 2)                          AS co2_electric_ton_anio
    FROM routes r
    LEFT JOIN (
        SELECT t.route_id, MAX(s.shape_dist_traveled) / 1000.0 AS distancia_km
          FROM trips t
          JOIN shapes s ON s.shape_id = t.shape_id
         GROUP BY t.route_id
    ) rd ON rd.route_id = r.route_id
    LEFT JOIN (
        SELECT route_id, COUNT(*) AS total_trips
          FROM trips
         GROUP BY route_id
    ) tc ON tc.route_id = r.route_id
    LEFT JOIN (
        SELECT t.route_id, AVG(f.headway_secs) AS avg_headway_secs
          FROM frequencies f
          JOIN trips t ON t.trip_id = f.trip_id
         GROUP BY t.route_id
    ) hw ON hw.route_id = r.route_id
    WHERE p_agency_id IS NULL OR r.agency_id = p_agency_id;

    -- Result-set 2: agencias
    SELECT agency_id, agency_name FROM agency;
END$$

-- SP2: dashboard COO. 4 result-sets:
--   1) contadores operativos
--   2) tendencia de pasajeros por dia de semana
--   3) viajes por hora
--   4) agencias
CREATE PROCEDURE sp_get_coo_dashboard(IN p_start DATE, IN p_end DATE)
BEGIN
    -- Result-set 1: contadores operativos
    SELECT
        (SELECT COUNT(*) FROM routes)                              AS total_routes,
        (SELECT COUNT(*) FROM trips)                               AS total_trips,
        (SELECT COUNT(*) FROM stops)                               AS total_stops,
        (SELECT COUNT(DISTINCT shape_id) FROM shapes)              AS total_shapes,
        IFNULL((SELECT AVG(headway_secs) / 60.0 FROM frequencies),
               0)                                                  AS avg_frequency_min;

    -- Result-set 2: tendencia por dia-de-semana (1=Dom ... 7=Sab)
    SELECT
        DAYOFWEEK(fecha) AS dow,
        AVG(afluencia)   AS avg_passengers
      FROM afluencia_metrobus
     WHERE (p_start IS NULL OR fecha >= p_start)
       AND (p_end   IS NULL OR fecha <= p_end)
     GROUP BY DAYOFWEEK(fecha)
     ORDER BY dow;

    -- Result-set 3: viajes por hora (0-23)
    SELECT
        LEAST(CAST(SUBSTRING(departure_time, 1, 2) AS UNSIGNED), 23) AS hr,
        COUNT(*)                                                     AS trip_count
      FROM stop_times
     WHERE departure_time IS NOT NULL AND CHAR_LENGTH(departure_time) >= 2
     GROUP BY hr
     ORDER BY hr;

    -- Result-set 4: agencias
    SELECT agency_id, agency_name FROM agency;
END$$

DELIMITER ;

-- ----------------------------------------------------------------------------
-- TRIGGERS
-- ----------------------------------------------------------------------------

DROP TRIGGER IF EXISTS trg_users_email_lowercase_ins;
DROP TRIGGER IF EXISTS trg_users_email_lowercase_upd;
DROP TRIGGER IF EXISTS trg_users_audit_ins;
DROP TRIGGER IF EXISTS trg_users_audit_upd;
DROP TRIGGER IF EXISTS trg_users_audit_del;
DROP TRIGGER IF EXISTS trg_bus_models_validate_ins;
DROP TRIGGER IF EXISTS trg_bus_models_validate_upd;

DELIMITER $$

-- TR1: normalizar email (lowercase + trim) en INSERT y UPDATE
CREATE TRIGGER trg_users_email_lowercase_ins
BEFORE INSERT ON users
FOR EACH ROW
BEGIN
    SET NEW.email = LOWER(TRIM(NEW.email));
END$$

CREATE TRIGGER trg_users_email_lowercase_upd
BEFORE UPDATE ON users
FOR EACH ROW
BEGIN
    SET NEW.email = LOWER(TRIM(NEW.email));
END$$

-- TR2: auditoria de cambios sobre users (cubre INSERT, UPDATE y DELETE)
CREATE TRIGGER trg_users_audit_ins
AFTER INSERT ON users
FOR EACH ROW
BEGIN
    INSERT INTO users_audit(user_id, action, new_email, new_role, new_status)
    VALUES (NEW.id, 'INSERT', NEW.email, NEW.role_id, NEW.status);
END$$

CREATE TRIGGER trg_users_audit_upd
AFTER UPDATE ON users
FOR EACH ROW
BEGIN
    INSERT INTO users_audit(
        user_id, action,
        old_email, new_email,
        old_role,  new_role,
        old_status, new_status
    )
    VALUES (
        NEW.id,
        IF(OLD.status = 'ACTIVE' AND NEW.status = 'SUSPENDED'
           AND OLD.updated_at < DATE_SUB(NOW(), INTERVAL 180 DAY),
           'SUSPEND_INACTIVE',
           'UPDATE'),
        OLD.email,   NEW.email,
        OLD.role_id, NEW.role_id,
        OLD.status,  NEW.status
    );
END$$

CREATE TRIGGER trg_users_audit_del
AFTER DELETE ON users
FOR EACH ROW
BEGIN
    INSERT INTO users_audit(user_id, action, old_email, old_role, old_status)
    VALUES (OLD.id, 'DELETE', OLD.email, OLD.role_id, OLD.status);
END$$

-- TR3: validar campos numericos de bus_models (rechazo de negativos)
CREATE TRIGGER trg_bus_models_validate_ins
BEFORE INSERT ON bus_models
FOR EACH ROW
BEGIN
    IF NEW.unit_cost_usd IS NOT NULL AND NEW.unit_cost_usd < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'unit_cost_usd no puede ser negativo';
    END IF;
    IF NEW.fuel_consumption_l_km IS NOT NULL AND NEW.fuel_consumption_l_km < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'fuel_consumption_l_km no puede ser negativo';
    END IF;
    IF NEW.energy_consumption_kwh_km IS NOT NULL AND NEW.energy_consumption_kwh_km < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'energy_consumption_kwh_km no puede ser negativo';
    END IF;
    IF NEW.autonomy_km IS NOT NULL AND NEW.autonomy_km < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'autonomy_km no puede ser negativo';
    END IF;
END$$

CREATE TRIGGER trg_bus_models_validate_upd
BEFORE UPDATE ON bus_models
FOR EACH ROW
BEGIN
    IF NEW.unit_cost_usd IS NOT NULL AND NEW.unit_cost_usd < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'unit_cost_usd no puede ser negativo';
    END IF;
    IF NEW.fuel_consumption_l_km IS NOT NULL AND NEW.fuel_consumption_l_km < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'fuel_consumption_l_km no puede ser negativo';
    END IF;
    IF NEW.energy_consumption_kwh_km IS NOT NULL AND NEW.energy_consumption_kwh_km < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'energy_consumption_kwh_km no puede ser negativo';
    END IF;
    IF NEW.autonomy_km IS NOT NULL AND NEW.autonomy_km < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'autonomy_km no puede ser negativo';
    END IF;
END$$

DELIMITER ;

-- ----------------------------------------------------------------------------
-- EVENT SCHEDULER
-- Requiere: SET GLOBAL event_scheduler = ON; (ya en docker-compose.yml)
-- ----------------------------------------------------------------------------

DROP EVENT IF EXISTS evt_suspend_inactive_users_weekly;

DELIMITER $$

CREATE EVENT evt_suspend_inactive_users_weekly
ON SCHEDULE EVERY 1 WEEK
  STARTS (TIMESTAMP(CURRENT_DATE + INTERVAL (8 - DAYOFWEEK(CURRENT_DATE)) DAY) + INTERVAL 4 HOUR)
ON COMPLETION PRESERVE
ENABLE
DO
BEGIN
    UPDATE users
       SET status = 'SUSPENDED'
     WHERE status = 'ACTIVE'
       AND updated_at < DATE_SUB(NOW(), INTERVAL 180 DAY);
    -- Nota: TR2 (trg_users_audit_upd) detecta la transicion ACTIVE->SUSPENDED
    -- con updated_at > 180 dias y registra action='SUSPEND_INACTIVE' en users_audit.
END$$

DELIMITER ;