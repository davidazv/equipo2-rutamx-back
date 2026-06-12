-- ============================================================================
-- verify-faker.sql — Verificación post-Faker
-- Uso: mysql -uroot -pNewPassword -t < scripts/verify-faker.sql
-- ============================================================================
USE rutamx;

-- ─────────────────────────────────────────────
-- 1. Volumen sintético vs real
-- ─────────────────────────────────────────────
SELECT '=== 1. Volumen total ===' AS reporte;
SELECT
    'users' AS tabla,
    (SELECT COUNT(*) FROM users WHERE email NOT LIKE '%@faker.rutamx.com') AS reales,
    (SELECT COUNT(*) FROM users WHERE email LIKE '%@faker.rutamx.com') AS sinteticos,
    (SELECT COUNT(*) FROM users) AS total
UNION ALL
SELECT
    'afluencia_metrobus',
    (SELECT COUNT(*) FROM afluencia_metrobus WHERE tipo_pago NOT LIKE '%\_SIM'),
    (SELECT COUNT(*) FROM afluencia_metrobus WHERE tipo_pago LIKE '%\_SIM'),
    (SELECT COUNT(*) FROM afluencia_metrobus);

-- ─────────────────────────────────────────────
-- 2. Distribución de roles en users sintéticos
-- ─────────────────────────────────────────────
SELECT '=== 2. Distribución de roles (esperado: 80/10/5/5) ===' AS reporte;
SELECT
    r.name AS rol,
    COUNT(*) AS users,
    ROUND(100.0 * COUNT(*) / (SELECT COUNT(*) FROM users WHERE email LIKE '%@faker.rutamx.com'), 1) AS porcentaje
FROM users u
JOIN roles r ON r.id = u.role_id
WHERE u.email LIKE '%@faker.rutamx.com'
GROUP BY r.name
ORDER BY users DESC;

-- ─────────────────────────────────────────────
-- 3. Distribución de status en users sintéticos
-- ─────────────────────────────────────────────
SELECT '=== 3. Distribución de status (esperado: 90/10) ===' AS reporte;
SELECT
    status,
    COUNT(*) AS users,
    ROUND(100.0 * COUNT(*) / (SELECT COUNT(*) FROM users WHERE email LIKE '%@faker.rutamx.com'), 1) AS porcentaje
FROM users
WHERE email LIKE '%@faker.rutamx.com'
GROUP BY status;

-- ─────────────────────────────────────────────
-- 4. Spread temporal de users (alimenta event scheduler)
-- ─────────────────────────────────────────────
SELECT '=== 4. Spread temporal users sintéticos ===' AS reporte;
SELECT
    CASE
        WHEN updated_at < NOW() - INTERVAL 180 DAY THEN 'a. Candidatos a suspender (>180 días inactivos)'
        WHEN updated_at < NOW() - INTERVAL 90 DAY THEN 'b. 90-180 días'
        ELSE 'c. Recientes (<90 días)'
    END AS antiguedad,
    COUNT(*) AS users
FROM users
WHERE email LIKE '%@faker.rutamx.com'
GROUP BY antiguedad
ORDER BY antiguedad;

-- ─────────────────────────────────────────────
-- 5. Patrón estacional afluencia sintética (esperado: L-V > S > D)
-- ─────────────────────────────────────────────
SELECT '=== 5. Patrón estacional afluencia sintética 2027 ===' AS reporte;
SELECT
    dow,
    CASE dow
        WHEN 1 THEN 'Domingo'
        WHEN 2 THEN 'Lunes'
        WHEN 3 THEN 'Martes'
        WHEN 4 THEN 'Miercoles'
        WHEN 5 THEN 'Jueves'
        WHEN 6 THEN 'Viernes'
        WHEN 7 THEN 'Sabado'
    END AS dia,
    filas,
    ROUND(avg_afluencia, 2) AS avg_afluencia
FROM (
    SELECT
        DAYOFWEEK(fecha) AS dow,
        COUNT(*) AS filas,
        AVG(afluencia) AS avg_afluencia
    FROM afluencia_metrobus
    WHERE tipo_pago LIKE '%\_SIM'
    GROUP BY DAYOFWEEK(fecha)
) t
ORDER BY dow;

-- ─────────────────────────────────────────────
-- 6. Comparativa baseline real vs sintético por línea
-- ─────────────────────────────────────────────
SELECT '=== 6. Comparativa real vs sintético por línea ===' AS reporte;
SELECT
    real_data.linea,
    ROUND(real_data.avg_real, 2) AS avg_real,
    ROUND(sim_data.avg_sim, 2) AS avg_sim,
    ROUND(100.0 * (sim_data.avg_sim - real_data.avg_real) / real_data.avg_real, 1) AS delta_pct
FROM
    (SELECT linea, AVG(afluencia) AS avg_real
     FROM afluencia_metrobus
     WHERE tipo_pago NOT LIKE '%\_SIM'
     GROUP BY linea) real_data
JOIN
    (SELECT linea, AVG(afluencia) AS avg_sim
     FROM afluencia_metrobus
     WHERE tipo_pago LIKE '%\_SIM'
     GROUP BY linea) sim_data ON sim_data.linea = real_data.linea
ORDER BY real_data.linea;

-- ─────────────────────────────────────────────
-- 7. Crecimiento físico en disco
-- ─────────────────────────────────────────────
SELECT '=== 7. Tamaño físico en disco ===' AS reporte;
SELECT
    table_name AS tabla,
    table_rows AS filas_aprox,
    ROUND(data_length/1024/1024, 2) AS datos_mb,
    ROUND(index_length/1024/1024, 2) AS indices_mb,
    ROUND((data_length + index_length)/1024/1024, 2) AS total_mb
FROM information_schema.tables
WHERE table_schema = 'rutamx'
  AND table_name IN ('users', 'afluencia_metrobus')
ORDER BY table_name;

-- ─────────────────────────────────────────────
-- 8. EXPLAIN ANALYZE — login por email (debe usar uq_users_email)
-- ─────────────────────────────────────────────
SELECT '=== 8. EXPLAIN login por email sintético ===' AS reporte;
EXPLAIN ANALYZE
SELECT u.id, u.email, r.name
FROM users u
LEFT JOIN roles r ON r.id = u.role_id
WHERE u.email = (
    SELECT email FROM users WHERE email LIKE '%@faker.rutamx.com' LIMIT 1
);

-- ─────────────────────────────────────────────
-- 9. EXPLAIN ANALYZE — agregación afluencia DOW (idx_afluencia_fecha)
-- ─────────────────────────────────────────────
SELECT '=== 9. EXPLAIN agregación DOW con dataset ampliado ===' AS reporte;
EXPLAIN ANALYZE
SELECT DAYOFWEEK(fecha) AS dow, AVG(afluencia) AS avg_pasajeros
FROM afluencia_metrobus
WHERE fecha BETWEEN '2024-01-01' AND '2027-12-31'
GROUP BY DAYOFWEEK(fecha)
ORDER BY dow;

-- ─────────────────────────────────────────────
-- 10. Resumen final
-- ─────────────────────────────────────────────
SELECT '=== 10. Resumen ===' AS reporte;
SELECT
    'Total users en BD' AS metrica,
    CAST((SELECT COUNT(*) FROM users) AS CHAR) AS valor
UNION ALL
SELECT 'Users sintéticos',
    CAST((SELECT COUNT(*) FROM users WHERE email LIKE '%@faker.rutamx.com') AS CHAR)
UNION ALL
SELECT 'Total afluencia en BD',
    CAST((SELECT COUNT(*) FROM afluencia_metrobus) AS CHAR)
UNION ALL
SELECT 'Afluencia sintética',
    CAST((SELECT COUNT(*) FROM afluencia_metrobus WHERE tipo_pago LIKE '%\_SIM') AS CHAR);

mysql -uroot -pNewPassword -t < /Users/aliosha/Proyects/Learning/TecSemestre6/Proyecto-Equipo2-RutaMx/equipo2-rutamx-back/scripts/verify-faker.sql