#!/usr/bin/env bash
# Load test: /api/reports/comparative — empieza en 1k concurrentes, meta: 1M requests
set -euo pipefail

# ── Config ────────────────────────────────────────────────────────────────────
HOST="${HOST:-http://127.0.0.1:8080}"
ROUTE_ID="${ROUTE_ID:-B_CMX0800L1}"
ELECTRIC_ID="${ELECTRIC_ID:-1}"
DIESEL_ID="${DIESEL_ID:-4}"
FIREBASE_KEY="AIzaSyCxvE3ZXO6EQfIgU2okDq1qkvE_0laOzo0"
EMAIL="emiliano@rutamx.com"
PASS="Emiliano2004"

URL="${HOST}/api/reports/comparative?routeId=${ROUTE_ID}&electricModelId=${ELECTRIC_ID}&dieselModelId=${DIESEL_ID}&buses=10&years=10"
TOTAL_REQUESTS=0
START_TIME=$SECONDS
REPORT_FILE="/tmp/load-test-$(date +%s).txt"
JVM_PID=""

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; CYAN='\033[0;36m'; BOLD='\033[1m'; NC='\033[0m'

log()  { echo -e "${CYAN}[$(date +%H:%M:%S)]${NC} $*"; }
ok()   { echo -e "${GREEN}[OK]${NC} $*"; }
warn() { echo -e "${YELLOW}[WARN]${NC} $*"; }
die()  { echo -e "${RED}${BOLD}[TRONADO]${NC} $*"; }
hdr()  { echo -e "${BOLD}${YELLOW}$*${NC}"; }

# ── Token Firebase ────────────────────────────────────────────────────────────
get_token() {
  local resp
  resp=$(curl -sf --max-time 10 -X POST \
    "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=${FIREBASE_KEY}" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"${EMAIL}\",\"password\":\"${PASS}\",\"returnSecureToken\":true}" 2>/dev/null) || true
  [[ -z "$resp" ]] && { warn "Sin token Firebase"; echo ""; return; }
  echo "$resp" | python3 -c "import json,sys; d=json.load(sys.stdin); print(d.get('idToken',''))" 2>/dev/null || echo ""
}

# ── Server alive ──────────────────────────────────────────────────────────────
server_alive() {
  local code
  code=$(curl -s --max-time 4 -o /dev/null -w "%{http_code}" "${HOST}/q/dev" 2>/dev/null) || true
  [[ "$code" =~ ^[0-9]+$ && "$code" != "000" ]]
}

# ── Memoria JVM ───────────────────────────────────────────────────────────────
jvm_mem() {
  local pid
  pid=$(pgrep -f "quarkus:dev\|quarkus.*runner\|equipo2-rutamx" 2>/dev/null | head -1) || true
  [[ -z "$pid" ]] && { echo "n/a"; return; }
  local mem
  mem=$(ps -o rss= -p "$pid" 2>/dev/null | awk '{printf "%.0f MB", $1/1024}') || mem="n/a"
  echo "$mem"
}

# ── Una fase: N instancias ab en paralelo ──────────────────────────────────────
# Cada instancia corre REQS_EACH requests a CONC_EACH concurrentes
# Total efectivo = AB_INSTANCES × REQS_EACH   /   concurrencia real = AB_INSTANCES × CONC_EACH
run_phase() {
  local label=$1 reqs_each=$2 conc_each=$3 ab_instances=$4 token=$5
  local total_phase=$((reqs_each * ab_instances))
  local total_conc=$((conc_each * ab_instances))

  log "FASE ${label}: ${total_phase} requests — ${ab_instances}×ab @ ${conc_each} c/u = ${total_conc} concurrentes totales  |  JVM: $(jvm_mem)"

  # Archivo temporal por instancia
  local tmpdir
  tmpdir=$(mktemp -d)

  for i in $(seq 1 "$ab_instances"); do
    {
      if [[ -n "$token" ]]; then
        ab -n "$reqs_each" -c "$conc_each" -r \
          -H "Authorization: Bearer ${token}" \
          "${URL}" > "${tmpdir}/ab_${i}.txt" 2>&1
      else
        ab -n "$reqs_each" -c "$conc_each" -r \
          "${URL}" > "${tmpdir}/ab_${i}.txt" 2>&1
      fi
    } &
  done
  wait

  TOTAL_REQUESTS=$((TOTAL_REQUESTS + total_phase))

  # Agregar métricas de todas las instancias
  local total_rps=0 total_failed=0 p50_sum=0 p99_sum=0 count=0
  for f in "${tmpdir}"/ab_*.txt; do
    local rps failed p50 p99
    rps=$(grep  "Requests per second" "$f" 2>/dev/null | awk '{print $4}') || rps="0"
    failed=$(grep "Failed requests"   "$f" 2>/dev/null | awk '{print $3}') || failed="0"
    p50=$(grep  "^ *50%"              "$f" 2>/dev/null | awk '{print $2}') || p50="0"
    p99=$(grep  "^ *99%"              "$f" 2>/dev/null | awk '{print $2}') || p99="0"
    total_rps=$(python3 -c "print(${total_rps}+${rps:-0})" 2>/dev/null)   || true
    total_failed=$(python3 -c "print(${total_failed}+${failed:-0})" 2>/dev/null) || true
    p50_sum=$(python3   -c "print(${p50_sum}+${p50:-0})"   2>/dev/null)   || true
    p99_sum=$(python3   -c "print(${p99_sum}+${p99:-0})"   2>/dev/null)   || true
    count=$((count+1))
  done
  local avg_p50=0 avg_p99=0
  [[ $count -gt 0 ]] && avg_p50=$(python3 -c "print(round(${p50_sum}/${count}))") || true
  [[ $count -gt 0 ]] && avg_p99=$(python3 -c "print(round(${p99_sum}/${count}))") || true

  echo "  req/s total: ${total_rps}  |  fallos: ${total_failed}  |  p50: ${avg_p50}ms  |  p99: ${avg_p99}ms  |  acum: ${TOTAL_REQUESTS}"

  # Log al reporte
  {
    echo "FASE ${label}: req/s=${total_rps} fallos=${total_failed} p50=${avg_p50}ms p99=${avg_p99}ms total_acum=${TOTAL_REQUESTS}"
    for f in "${tmpdir}"/ab_*.txt; do echo "--- $(basename "$f") ---"; cat "$f"; echo; done
  } >> "$REPORT_FILE"

  rm -rf "$tmpdir"
}

# ── MAIN ──────────────────────────────────────────────────────────────────────
clear
echo -e "${RED}╔══════════════════════════════════════════════════════╗${NC}"
echo -e "${RED}║   LOAD TEST: /api/reports/comparative                ║${NC}"
echo -e "${RED}║   META: 1,000,000 requests — empezando en 1k conc   ║${NC}"
echo -e "${RED}╚══════════════════════════════════════════════════════╝${NC}"
echo ""
log "Target : ${URL}"
log "Reporte: ${REPORT_FILE}"
echo ""

log "Verificando servidor..."
if ! server_alive; then
  die "Servidor no responde en ${HOST}"; exit 1
fi
ok "Servidor vivo  |  JVM: $(jvm_mem)"

log "Obteniendo token Firebase (CMO)..."
TOKEN=$(get_token)
[[ -n "$TOKEN" ]] && ok "Token OK (${#TOKEN} chars)" || warn "Sin token — requests = 401"
echo ""

echo "Inicio: $(date)" > "$REPORT_FILE"
echo "URL: ${URL}"     >> "$REPORT_FILE"
echo ""               >> "$REPORT_FILE"

# ── Fases ─────────────────────────────────────────────────────────────────────
# label | reqs_por_ab | conc_por_ab | instancias_ab
# total_reqs = reqs_por_ab × instancias_ab
# concurrencia = conc_por_ab × instancias_ab
PHASES=(
  "1 :  10000 : 500 :  2"   #   20k req   1000 conc
  "2 :  25000 : 500 :  4"   #  100k req   2000 conc
  "3 :  50000 : 500 :  6"   #  300k req   3000 conc
  "4 : 100000 : 500 :  8"   #  800k req   4000 conc  — supera 1M acumulado
)

DEAD=0
for phase in "${PHASES[@]}"; do
  IFS=':' read -r label reqs_each conc_each instances <<< "$phase"
  label=$(echo "$label" | tr -d ' ')
  reqs_each=$(echo "$reqs_each" | tr -d ' ')
  conc_each=$(echo "$conc_each" | tr -d ' ')
  instances=$(echo "$instances" | tr -d ' ')

  if ! server_alive; then
    die "¡SERVIDOR CAÍDO antes de FASE ${label}! — ${TOTAL_REQUESTS} requests enviadas"
    echo "SERVIDOR CAÍDO — total: ${TOTAL_REQUESTS}" >> "$REPORT_FILE"
    DEAD=1; break
  fi

  # Refrescar token cada fase (las fases son largas)
  log "Refrescando token..."
  NEW_TOKEN=$(get_token)
  [[ -n "$NEW_TOKEN" ]] && TOKEN="$NEW_TOKEN"

  run_phase "$label" "$reqs_each" "$conc_each" "$instances" "$TOKEN"
  echo ""

  # Si ya llegamos al millón, paramos
  if [[ $TOTAL_REQUESTS -ge 1000000 ]]; then
    ok "¡Millón de requests alcanzado!"
    break
  fi

  sleep 2
done

# ── Resultado ─────────────────────────────────────────────────────────────────
ELAPSED=$(( SECONDS - START_TIME ))
echo ""
echo -e "${RED}══════════════════════════════════════════════════════${NC}"
hdr "RESUMEN FINAL"
echo -e "${RED}══════════════════════════════════════════════════════${NC}"
printf "  Total requests enviadas : %'d\n" "$TOTAL_REQUESTS"
echo   "  Tiempo total            : ${ELAPSED}s"
if [[ $DEAD -eq 0 ]] && server_alive; then
  echo -e "  Estado final del server  : ${GREEN}VIVO${NC} — Quarkus es un toro"
else
  echo -e "  Estado final del server  : ${RED}TRONADO${NC}"
fi
echo   "  JVM al final            : $(jvm_mem)"
echo   "  Reporte completo        : ${REPORT_FILE}"
echo ""
