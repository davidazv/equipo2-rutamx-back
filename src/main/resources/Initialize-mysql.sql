-- ============================================================================
-- RutaMx — Esquema DDL MySQL (Completo)
-- Alcance: Usuarios/Roles, Catálogo de Autobuses, GTFS (agencias, rutas,
--          viajes, paradas, frecuencias, formas), Afluencia Metrobús
-- Motor:   MySQL 8.x
-- Stack:   Quarkus + Hibernate Panache
-- ============================================================================

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
    trip_short_name VARCHAR(50)   NULL,
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
  'a8gB5KFBr1Z9czAJf6rLYE48u4k2',
  'David',
  'Zárate',
  (SELECT id FROM roles WHERE name = 'ADMIN'),
  'ACTIVE'
);