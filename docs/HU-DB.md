# HU-DB — Rutinas de Base de Datos (SPs, Functions, Triggers, Event)

## Descripcion

Esta entrega introduce un set minimo de rutinas en MySQL que mueven calculo, validacion e integridad al motor de base de datos. El proposito es: (1) colapsar llamadas multiples de los dashboards CMO y COO en un solo round-trip via stored procedures, (2) centralizar formulas matematicas que viven duplicadas en Java, (3) garantizar invariantes sobre `users` y `bus_models` con triggers, y (4) automatizar la suspension semanal de usuarios inactivos con un event scheduler. El set fue seleccionado tras una auditoria documentada en `docs/auditoria-prioridad.md`.

El set consta de:

- **2 Stored Procedures** — `sp_get_cmo_dashboard`, `sp_get_coo_dashboard`
- **2 Stored Functions** — `fn_co2_emissions`, `fn_annual_km`
- **3 Triggers** — `trg_users_email_lowercase_*`, `trg_users_audit_*`, `trg_bus_models_validate_*`
- **1 Event Scheduler** — `evt_suspend_inactive_users_weekly`
- **1 Tabla auxiliar** — `users_audit`

---

## Endpoints

### GET /api/cmo/dashboard

Dashboard consolidado del CMO. Envuelve `sp_get_cmo_dashboard` y regresa dos result-sets unificados en un solo JSON. Sustituye al endpoint deprecated `GET /api/cmo/route-stats`.

**Parametros de query:**

| Parametro | Tipo   | Requerido | Default | Validacion |
|-----------|--------|-----------|---------|------------|
| agencyId  | String | No        | null    | Si se pasa, filtra rutas por agencia |

**Respuesta 200 OK:**

```json
{
  "routes": [
    {
      "routeId": "1",
      "routeShortName": "Linea 1",
      "routeLongName": "Indios Verdes - Caminero",
      "agencyId": "METRO",
      "routeColor": "#FF0000",
      "distanciaKm": 18.5,
      "totalTrips": 1240,
      "avgDailyTrips": 177.14,
      "headwayMinutes": 4,
      "annualKm": 1196570.0,
      "co2DieselTonAnio": 1281.42,
      "co2ElectricTonAnio": 646.15
    }
  ],
  "agencies": [
    { "agencyId": "METRO", "agencyName": "Sistema de Transporte Colectivo" }
  ]
}
```

**Errores:**

| Codigo | Condicion         | Mensaje                                       |
|--------|-------------------|-----------------------------------------------|
| 500    | Error inesperado  | Error inesperado al obtener dashboard CMO     |

---

### GET /api/coo/dashboard

Dashboard consolidado del COO. Envuelve `sp_get_coo_dashboard` y regresa los cuatro result-sets en un solo JSON. Sustituye a los endpoints deprecated `GET /api/kpi/operational-summary`, `GET /api/kpi/passenger-trend`, `GET /api/kpi/hourly-stats`.

**Parametros de query:**

| Parametro | Tipo   | Requerido | Default | Validacion           |
|-----------|--------|-----------|---------|----------------------|
| start     | String | No        | null    | ISO `YYYY-MM-DD`     |
| end       | String | No        | null    | ISO `YYYY-MM-DD`     |

**Respuesta 200 OK:**

```json
{
  "operational": {
    "totalRoutes": 195,
    "totalTrips": 23120,
    "totalStops": 5380,
    "totalShapes": 197,
    "avgFrequencyMin": 7.42
  },
  "trend": [
    { "dow": 1, "avgPassengers": 412000.5 },
    { "dow": 2, "avgPassengers": 612340.8 }
  ],
  "hourly": [
    { "hour": 5, "tripCount": 200 },
    { "hour": 6, "tripCount": 1200 }
  ],
  "agencies": [
    { "agencyId": "METRO", "agencyName": "Sistema de Transporte Colectivo" }
  ]
}
```

**Errores:**

| Codigo | Condicion                       | Mensaje                                     |
|--------|---------------------------------|---------------------------------------------|
| 400    | Fecha en formato invalido       | Formato de fecha invalido. Use YYYY-MM-DD   |
| 500    | Error inesperado                | Error inesperado al obtener dashboard COO   |

---

## Logica de negocio

### Stored Functions

#### `fn_co2_emissions(annual_km, fuel_per_km, co2_factor) RETURNS DOUBLE`

Calcula los kg de CO2 emitidos al ano. Cualquier parametro nulo regresa 0.

```
kg_co2 = annual_km * fuel_per_km * co2_factor / 1000.0
```

Factores tipicos:

| Combustible | factor CO2          | Unidad                |
|-------------|---------------------|-----------------------|
| Diesel      | 2.68                | kg CO2 / litro        |
| Electrico   | 0.45                | kg CO2 / kWh (red MX) |

#### `fn_annual_km(daily_trips, route_length_km, operating_days) RETURNS DOUBLE`

Calcula los km recorridos por una ruta al ano.

```
annual_km = daily_trips * route_length_km * operating_days
```

`operating_days` se asume 365 en SP1 salvo override.

### Stored Procedures

#### `sp_get_cmo_dashboard(p_agency_id)`

1. Resuelve `v_avg_diesel_l_km` y `v_avg_kwh_km` con `AVG(...)` sobre `bus_models` filtrando por `fuel_type`.
2. Result-set 1: JOIN de `routes` con (`trips` agregados por ruta), (`shapes` para distancia con `MAX(shape_dist_traveled)`), (`frequencies` para headway promedio). Calcula `annualKm`, `co2DieselTonAnio` y `co2ElectricTonAnio` usando `fn_annual_km` + `fn_co2_emissions`.
3. Result-set 2: catalogo de agencias.

#### `sp_get_coo_dashboard(p_start, p_end)`

1. Result-set 1: contadores `COUNT(*)` sobre routes/trips/stops/shapes mas `AVG(headway_secs)/60.0`.
2. Result-set 2: tendencia de pasajeros — `AVG(afluencia) GROUP BY DAYOFWEEK(fecha)` filtrado por rango.
3. Result-set 3: distribucion horaria — `COUNT(*) FROM stop_times GROUP BY HOUR(departure_time)`.
4. Result-set 4: catalogo de agencias.

### Triggers

| Trigger                              | Tabla        | Evento(s)                         | Efecto |
|--------------------------------------|--------------|-----------------------------------|--------|
| `trg_users_email_lowercase_ins/upd`  | `users`      | `BEFORE INSERT`, `BEFORE UPDATE` | `NEW.email = LOWER(TRIM(NEW.email))` |
| `trg_users_audit_ins/upd/del`        | `users`      | `AFTER INSERT/UPDATE/DELETE`      | Inserta fila en `users_audit`. UPDATE detecta transicion `ACTIVE -> SUSPENDED` con `updated_at > 180 dias` y registra `action = 'SUSPEND_INACTIVE'`; en otros casos `action = 'UPDATE'`. |
| `trg_bus_models_validate_ins/upd`    | `bus_models` | `BEFORE INSERT`, `BEFORE UPDATE` | `SIGNAL '45000'` si `unit_cost_usd`, `fuel_consumption_l_km`, `energy_consumption_kwh_km` o `autonomy_km` < 0. |

### Event Scheduler

`evt_suspend_inactive_users_weekly` corre cada lunes a las 04:00. Ejecuta:

```sql
UPDATE users SET status = 'SUSPENDED'
WHERE status = 'ACTIVE'
  AND updated_at < DATE_SUB(NOW(), INTERVAL 180 DAY);
```

El `UPDATE` dispara `trg_users_audit_upd`, que registra cada fila como `action = 'SUSPEND_INACTIVE'` en `users_audit`. No se requiere tabla de log adicional.

Pre-requisito: `--event-scheduler=ON` en MySQL (ya configurado en `docker-compose.yml`).

---

## Arquitectura

```
[Frontend]
  └── lib/api/cmo.ts::getCmoDashboard()      ──┐
  └── lib/api/coo.ts::getCooDashboard()      ──┤
                                                │
[Interfaces / REST]                             │
  └── CmoResource @ /api/cmo/dashboard       ──┤
  └── CooResource @ /api/coo/dashboard       ──┤
                                                │
[Application / Use case]                        │
  └── GetCmoDashboardUseCase                 ──┤
  └── GetCooDashboardUseCase                 ──┤
                                                │
[Domain / Repository]                           │
  └── DashboardRepository (interface)        ──┤
                                                │
[Infrastructure / Repository]                   │
  └── DashboardRepositoryImpl                ──┘
        EntityManager.createStoredProcedureQuery(...)
                                                │
[MySQL]                                         │
  ├── sp_get_cmo_dashboard                  <──┘
  ├── sp_get_coo_dashboard
  ├── fn_co2_emissions, fn_annual_km
  ├── trg_users_email_lowercase_*
  ├── trg_users_audit_*           ───────────> users_audit
  ├── trg_bus_models_validate_*
  └── evt_suspend_inactive_users_weekly ─────> dispara trg_users_audit_upd
```

### Notas de implementacion

- El SQL del set vive al final de `src/main/resources/Initialize-mysql.sql` y solo aplica al perfil MySQL. El perfil `%test` usa H2 con `drop-and-create` desde las entidades JPA — los SPs/triggers/event no existen en H2, asi que los tests del nuevo `DashboardRepositoryImpl` deben mockear el repositorio en lugar de hacer integration.
- El frontend solo expone los nuevos servicios (`getCmoDashboard`, `getCooDashboard`). Los componentes `cmo-dashboard.tsx` y `coo-dashboard.tsx` siguen usando los endpoints viejos porque el shape de datos del SP1 no coincide con `RouteStatsResponse` (sin `avgDailyPassengers` ni `co2AhorradoTonAnio` calculado). Migrar las graficas es trabajo posterior.
- El endpoint deprecated `GET /api/cmo/route-stats` queda en pie por compatibilidad con el dashboard actual.
- La tabla `users_audit` NO tiene entidad JPA correspondiente — Hibernate en modo `validate` la ignora.
