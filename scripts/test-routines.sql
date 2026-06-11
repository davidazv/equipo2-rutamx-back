-- ============================================================================
-- test-routines.sql — Evidencia de pruebas para SPs, SFs y Triggers
-- Uso: mysql -uroot -pNewPassword -t < scripts/test-routines.sql
-- ============================================================================
USE rutamx;

-- ════════════════════════════════════════════════════════════════════
-- A. STORED FUNCTIONS — pruebas con casos representativos
-- ════════════════════════════════════════════════════════════════════

SELECT '═══ A1. fn_co2_emissions(annual_km, fuel_per_km, co2_factor) ═══' AS test;

SELECT
    fn_co2_emissions(50000, 0.4, 2.68) AS diesel_50k_km_kg_co2,
    fn_co2_emissions(50000, 1.2, 0.45) AS electric_50k_km_kg_co2,
    fn_co2_emissions(NULL, 0.4, 2.68) AS retorna_cero_si_null,
    fn_co2_emissions(0, 0.4, 2.68) AS retorna_cero_si_cero;

SELECT '═══ A2. fn_annual_km(daily_trips, route_length_km, operating_days) ═══' AS test;

SELECT
    fn_annual_km(20, 25, 365) AS bus_20viajes_25km_365dias,
    fn_annual_km(10, 15, 250) AS bus_10viajes_15km_250dias,
    fn_annual_km(NULL, 25, 365) AS retorna_cero_si_null;

-- ════════════════════════════════════════════════════════════════════
-- B. STORED PROCEDURES — ejecución y verificación
-- ════════════════════════════════════════════════════════════════════

SELECT '═══ B1. sp_get_cmo_dashboard(NULL) — toda agencia ═══' AS test;

-- Solo muestra primeras 5 filas para evidencia
CALL sp_get_cmo_dashboard(NULL);

SELECT '═══ B2. sp_get_cmo_dashboard("MB") — filtro por Metrobús ═══' AS test;
CALL sp_get_cmo_dashboard('MB');

SELECT '═══ B3. sp_get_coo_dashboard("2024-01-01","2026-12-31") ═══' AS test;
CALL sp_get_coo_dashboard('2024-01-01','2026-12-31');

-- ════════════════════════════════════════════════════════════════════
-- C. TRIGGERS — pruebas funcionales
-- ════════════════════════════════════════════════════════════════════

SELECT '═══ C1. Trigger trg_users_email_lowercase_ins ═══' AS test;

-- Cleanup previo
DELETE FROM users WHERE email = 'test.trigger@rutamx.com';

-- Insertar con email en MAYÚSCULAS y espacios
INSERT INTO users (email, firebase_UUID, first_name, last_name, role_id, status)
VALUES ('  TEST.TRIGGER@RUTAMX.COM  ', 'test-uuid-trigger-001', 'Test', 'Trigger',
        (SELECT id FROM roles WHERE name='ADMIN'), 'ACTIVE');

SELECT
    email AS email_resultante,
    CASE WHEN email = 'test.trigger@rutamx.com'
         THEN '✅ PASS — normalizado a lowercase + trim'
         ELSE '❌ FAIL'
    END AS resultado
FROM users WHERE firebase_UUID = 'test-uuid-trigger-001';

SELECT '═══ C2. Trigger trg_users_audit_ins ═══' AS test;

-- El INSERT anterior debió disparar el audit trigger
SELECT
    action,
    new_email,
    new_status,
    CASE WHEN action = 'INSERT' AND new_email = 'test.trigger@rutamx.com'
         THEN '✅ PASS — INSERT registrado en users_audit'
         ELSE '❌ FAIL'
    END AS resultado
FROM users_audit
WHERE user_id = (SELECT id FROM users WHERE firebase_UUID = 'test-uuid-trigger-001')
ORDER BY changed_at DESC LIMIT 1;

SELECT '═══ C3. Trigger trg_users_audit_upd (cambio de status) ═══' AS test;

-- Forzar UPDATE (con delay para asegurar timestamp distinto)
UPDATE users
   SET status = 'SUSPENDED'
 WHERE firebase_UUID = 'test-uuid-trigger-001';

-- Buscar específicamente la entrada UPDATE
SELECT
    action,
    old_status,
    new_status,
    CASE WHEN action = 'UPDATE'
              AND old_status = 'ACTIVE'
              AND new_status = 'SUSPENDED'
         THEN '✅ PASS — UPDATE registrado con delta old→new'
         ELSE '❌ FAIL'
    END AS resultado
FROM users_audit
WHERE user_id = (SELECT id FROM users WHERE firebase_UUID = 'test-uuid-trigger-001')
  AND action = 'UPDATE'
ORDER BY id DESC LIMIT 1;

SELECT '═══ C4. Trigger trg_bus_models_validate_ins (rechazo de negativos) ═══' AS test;
SELECT 'Ejecuta el siguiente INSERT por separado para evidencia:' AS instruccion;
SELECT 'INSERT INTO bus_models (name, fuel_type, autonomy_km, unit_cost_usd) VALUES (''TEST_NEG'', ''ELECTRIC'', -100, 50000);' AS comando_a_correr;
SELECT 'Resultado esperado: ERROR 1644 (45000): autonomy_km no puede ser negativo' AS resultado_esperado;
SELECT '✅ PASS si MySQL devuelve ese error exacto' AS verificacion;

SELECT '═══ C5. Trigger trg_bus_models_validate_ins (caso válido) ═══' AS test;

DELETE FROM bus_models WHERE name = 'TEST_VALIDO';

INSERT INTO bus_models
    (name, fuel_type, autonomy_km, unit_cost_usd, passenger_capacity)
VALUES
    ('TEST_VALIDO', 'ELECTRIC', 300, 420000, 85);

SELECT
    name, fuel_type, autonomy_km,
    CASE WHEN autonomy_km > 0 THEN '✅ PASS — INSERT con valores válidos aceptado'
         ELSE '❌ FAIL' END AS resultado
FROM bus_models WHERE name = 'TEST_VALIDO';

-- Cleanup
DELETE FROM users WHERE firebase_UUID = 'test-uuid-trigger-001';
DELETE FROM bus_models WHERE name IN ('TEST_VALIDO', 'TEST_NEGATIVO_AUTONOMIA');

-- ════════════════════════════════════════════════════════════════════
-- D. EVENT SCHEDULER — verificación de configuración
-- ════════════════════════════════════════════════════════════════════

SELECT '═══ D1. Event evt_suspend_inactive_users_weekly registrado ═══' AS test;

SELECT
    EVENT_NAME,
    STATUS,
    EVENT_DEFINITION AS accion,
    INTERVAL_VALUE,
    INTERVAL_FIELD AS unidad,
    LAST_EXECUTED
FROM information_schema.EVENTS
WHERE EVENT_SCHEMA = 'rutamx'
  AND EVENT_NAME = 'evt_suspend_inactive_users_weekly';

SELECT '═══ D2. event_scheduler global está ON ═══' AS test;

SHOW VARIABLES LIKE 'event_scheduler';
