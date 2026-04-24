#!/usr/bin/env bash
# ============================================================================
# seed-data.sh — Inicializa la base de datos rutamx desde cero
#
# 1. Ejecuta equipo2-rutamx-back/src/main/resources/Initialize-mysql.sql (crea DB, tablas, roles, admin user)
# 2. Carga todos los CSV de data/ en sus tablas correspondientes
#
# Uso:
#   ./scripts/seed-data.sh                          # usa root sin password
#   ./scripts/seed-data.sh -p mypass                # con password
#   ./scripts/seed-data.sh -u myuser -p mypass      # credenciales custom
#   ./scripts/seed-data.sh -H 192.168.1.5 -P 3307  # host y puerto custom
#
# Prerequisitos:
#   1. MySQL 8.x corriendo
#   2. El servidor MySQL debe tener local_infile habilitado:
#      SET GLOBAL local_infile = 1;
# ============================================================================

set -euo pipefail

# ── Defaults ────────────────────────────────────────────────────────────────
DB_USER="root"
DB_PASS=""
DB_HOST="localhost"
DB_PORT="3306"

# ── Parse args ──────────────────────────────────────────────────────────────
while getopts "u:p:H:P:" opt; do
  case $opt in
    u) DB_USER="$OPTARG" ;;
    p) DB_PASS="$OPTARG" ;;
    H) DB_HOST="$OPTARG" ;;
    P) DB_PORT="$OPTARG" ;;
    *) echo "Uso: $0 [-u user] [-p pass] [-H host] [-P port]" && exit 1 ;;
  esac
done

# ── Resolve paths ───────────────────────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$SCRIPT_DIR/.."
DATA_DIR="$PROJECT_DIR/data"
INIT_SQL="$PROJECT_DIR/src/main/resources/Initialize-mysql.sql"

if [ ! -f "$INIT_SQL" ]; then
  echo "ERROR: No se encontro $INIT_SQL"
  exit 1
fi

if [ ! -d "$DATA_DIR" ]; then
  echo "ERROR: No se encontro el directorio data/ en $DATA_DIR"
  exit 1
fi

# ── MySQL command builder ───────────────────────────────────────────────────
MYSQL_BASE="mysql --local-infile=1 -u${DB_USER} -h${DB_HOST} -P${DB_PORT}"
if [ -n "$DB_PASS" ]; then
  MYSQL_BASE="mysql --local-infile=1 -u${DB_USER} -p${DB_PASS} -h${DB_HOST} -P${DB_PORT}"
fi

run_sql() {
  echo "$1" | $MYSQL_BASE rutamx
}

load_csv() {
  local table="$1"
  local file="$2"
  local columns="$3"

  if [ ! -f "$DATA_DIR/$file" ]; then
    echo "  SKIP: $file no encontrado"
    return
  fi

  local row_count
  row_count=$(wc -l < "$DATA_DIR/$file" | tr -d ' ')
  row_count=$((row_count - 1))

  echo "  Cargando $file → $table ($row_count filas)..."

  if [ -z "$columns" ]; then
    run_sql "LOAD DATA LOCAL INFILE '${DATA_DIR}/${file}'
      INTO TABLE ${table}
      FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"'
      LINES TERMINATED BY '\n'
      IGNORE 1 LINES;"
  else
    run_sql "LOAD DATA LOCAL INFILE '${DATA_DIR}/${file}'
      INTO TABLE ${table}
      FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"'
      LINES TERMINATED BY '\n'
      IGNORE 1 LINES
      (${columns});"
  fi
}

# ── Step 1: Initialize schema ───────────────────────────────────────────────
echo "=== RutaMx — Inicializacion completa ==="
echo "    Servidor: ${DB_USER}@${DB_HOST}:${DB_PORT}"
echo ""
echo "[Paso 1] Creando base de datos y tablas..."
$MYSQL_BASE < "$INIT_SQL"
echo "  OK: Esquema creado (Initialize-mysql.sql)"

# ── Step 2: Load CSV data ───────────────────────────────────────────────────
echo ""
echo "[Paso 2] Cargando datos CSV desde data/..."

# 1. bus_models (no FK, auto-increment id)
echo "  [1/10] bus_models"
load_csv "bus_models" "bus_models.csv" \
  "name, manufacturer, fuel_type, autonomy_km, passenger_capacity, unit_cost_usd, battery_capacity_kwh, energy_consumption_kwh_km, fuel_consumption_l_km, maintenance_cost_per_km, co2_emissions_g_km"

# 2. agency (no FK, natural PK)
echo "  [2/10] agency"
load_csv "agency" "agency.csv" ""

# 3. calendar (no FK, natural PK)
echo "  [3/10] calendar"
load_csv "calendar" "calendar.csv" ""

# 4. stops (no FK, natural PK)
echo "  [4/10] stops"
load_csv "stops" "stops.csv" ""

# 5. routes (FK → agency)
echo "  [5/10] routes"
load_csv "routes" "routes.csv" ""

# 6. shapes (no FK, auto-increment id — 127k rows)
echo "  [6/10] shapes (esto puede tomar unos segundos...)"
load_csv "shapes" "shapes.csv" \
  "shape_id, shape_pt_lat, shape_pt_lon, shape_pt_sequence, shape_dist_traveled"

# 7. trips (FK → routes, calendar)
echo "  [7/10] trips"
load_csv "trips" "trips.csv" \
  "route_id, service_id, trip_id, shape_id, trip_headsign, trip_short_name, direction_id"

# 8. stop_times (FK → trips, stops; CSV column order differs from DDL)
echo "  [8/10] stop_times"
load_csv "stop_times" "stop_times.csv" \
  "trip_id, timepoint, stop_id, stop_sequence, arrival_time, departure_time"

# 9. frequencies (FK → trips, auto-increment id)
echo "  [9/10] frequencies"
load_csv "frequencies" "frequencies.csv" \
  "trip_id, start_time, end_time, headway_secs, exact_times"

# 10. afluencia_metrobus (no FK, auto-increment id)
echo "  [10/10] afluencia_metrobus"
load_csv "afluencia_metrobus" "afluenciamb.csv" \
  "fecha, mes, anio, linea, tipo_pago, afluencia"

# ── Verification ────────────────────────────────────────────────────────────
echo ""
echo "=== Inicializacion completada ==="
echo ""
echo "Conteo de filas por tabla:"
for tbl in roles users bus_models agency calendar stops routes shapes trips stop_times frequencies afluencia_metrobus; do
  count=$(run_sql "SELECT COUNT(*) FROM ${tbl};" 2>/dev/null | tail -1)
  printf "  %-25s %s\n" "$tbl" "$count"
done
