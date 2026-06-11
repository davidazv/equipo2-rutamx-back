#!/usr/bin/env python3
"""
faker_afluencia.py — Genera proyección sintética de afluencia Metrobús
para años futuros (sin datos reales aún).

Útil para:
- Probar índices `idx_afluencia_fecha/linea/anio` con más volumen.
- Validar agregaciones `GROUP BY DAYOFWEEK(fecha)` del dashboard COO/CMO
  bajo dataset 2× más grande.
- Stress test del SP `sp_get_coo_dashboard` que filtra por rango de fechas.

Modela:
- Estacionalidad semanal (L-V > sábado > domingo).
- Variación anual: 100% base con ruido gaussiano ±15%.
- Líneas y tipos de pago tomados de los reales en BD (no inventa).

Uso:
    pip install -r scripts/requirements.txt
    python3 scripts/faker_afluencia.py --start 2027-01-01 --end 2027-12-31 \\
        --password NewPassword

Opciones:
    --start       fecha inicio inclusiva (YYYY-MM-DD)
    --end         fecha fin inclusiva (YYYY-MM-DD)
    --password    password root MySQL (default vacío)
    --host        host (default 127.0.0.1)
    --port        puerto (default 3306)
    --user        usuario MySQL (default root)
    --batch       tamaño batch (default 1000)
    --seed        semilla (default 42)
    --truncate    elimina filas sintéticas previas (anio >= 2027)

Tag de sintético: `tipo_pago` recibe sufijo `_SIM` para distinguir de reales.
"""
import argparse
import random
import sys
from datetime import date, datetime, timedelta

from faker import Faker
import mysql.connector


SPANISH_MONTHS = [
    "ENERO", "FEBRERO", "MARZO", "ABRIL", "MAYO", "JUNIO",
    "JULIO", "AGOSTO", "SEPTIEMBRE", "OCTUBRE", "NOVIEMBRE", "DICIEMBRE",
]

# Factor base por día de semana (lun=0 ... dom=6)
DOW_FACTOR = [1.00, 1.02, 1.00, 1.01, 0.95, 0.70, 0.55]


def fetch_lineas_tipos(cur):
    cur.execute(
        "SELECT DISTINCT linea FROM afluencia_metrobus "
        "WHERE tipo_pago NOT LIKE '%\\_SIM' ORDER BY linea"
    )
    lineas = [row[0] for row in cur.fetchall()]
    cur.execute(
        "SELECT DISTINCT tipo_pago FROM afluencia_metrobus "
        "WHERE tipo_pago NOT LIKE '%\\_SIM' ORDER BY tipo_pago"
    )
    tipos = [row[0] for row in cur.fetchall()]
    return lineas, tipos


def fetch_baseline(cur, lineas, tipos):
    """Promedio histórico de afluencia por (linea, tipo_pago) para anclar
       la proyección. Usa años anteriores como referencia."""
    base = {}
    for linea in lineas:
        for tipo in tipos:
            cur.execute(
                "SELECT AVG(afluencia) FROM afluencia_metrobus "
                "WHERE linea=%s AND tipo_pago=%s",
                (linea, tipo),
            )
            avg = cur.fetchone()[0]
            base[(linea, tipo)] = float(avg) if avg else 1000.0
    return base


def truncate_synthetic(cur, start_year):
    cur.execute(
        "DELETE FROM afluencia_metrobus "
        "WHERE anio >= %s AND tipo_pago LIKE %s",
        (start_year, "%\\_SIM"),
    )
    return cur.rowcount


def daterange(start, end):
    cur = start
    while cur <= end:
        yield cur
        cur += timedelta(days=1)


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--start", default="2027-01-01")
    p.add_argument("--end", default="2027-12-31")
    p.add_argument("--password", default="")
    p.add_argument("--host", default="127.0.0.1")
    p.add_argument("--port", type=int, default=3306)
    p.add_argument("--user", default="root")
    p.add_argument("--batch", type=int, default=1000)
    p.add_argument("--seed", type=int, default=42)
    p.add_argument("--truncate", action="store_true")
    args = p.parse_args()

    random.seed(args.seed)
    Faker.seed(args.seed)

    start = datetime.strptime(args.start, "%Y-%m-%d").date()
    end = datetime.strptime(args.end, "%Y-%m-%d").date()
    if end < start:
        print("ERROR: --end < --start")
        sys.exit(1)

    print(f"[faker_afluencia] Conectando a {args.user}@{args.host}:{args.port}/rutamx")
    conn = mysql.connector.connect(
        host=args.host,
        port=args.port,
        user=args.user,
        password=args.password,
        database="rutamx",
        autocommit=False,
    )
    cur = conn.cursor()

    lineas, tipos = fetch_lineas_tipos(cur)
    if not lineas or not tipos:
        print("ERROR: tabla afluencia_metrobus vacía — corre seed-data.sh primero")
        sys.exit(1)
    print(f"  Líneas: {len(lineas)}, tipos de pago: {len(tipos)}")
    base = fetch_baseline(cur, lineas, tipos)

    if args.truncate:
        deleted = truncate_synthetic(cur, start.year)
        conn.commit()
        print(f"  Eliminadas {deleted} filas sintéticas previas (anio >= {start.year})")

    sql = (
        "INSERT INTO afluencia_metrobus "
        "(fecha, mes, anio, linea, tipo_pago, afluencia) "
        "VALUES (%s, %s, %s, %s, %s, %s)"
    )

    days = (end - start).days + 1
    estimated = days * len(lineas) * len(tipos)
    print(f"[faker_afluencia] Generando {estimated} filas "
          f"({days} días × {len(lineas)} líneas × {len(tipos)} tipos)")

    buffer = []
    inserted = 0
    for d in daterange(start, end):
        dow_f = DOW_FACTOR[d.weekday()]
        mes_str = SPANISH_MONTHS[d.month - 1]
        anio = d.year
        for linea in lineas:
            for tipo in tipos:
                avg = base[(linea, tipo)]
                noise = random.gauss(1.0, 0.15)
                afluencia = round(avg * dow_f * noise, 2)
                if afluencia < 0:
                    afluencia = 0
                buffer.append((
                    d, mes_str, anio, linea, f"{tipo}_SIM", afluencia
                ))
                if len(buffer) >= args.batch:
                    cur.executemany(sql, buffer)
                    conn.commit()
                    inserted += len(buffer)
                    if inserted % 10000 == 0:
                        print(f"  {inserted}/{estimated}")
                    buffer = []
    if buffer:
        cur.executemany(sql, buffer)
        conn.commit()
        inserted += len(buffer)

    cur.execute(
        "SELECT COUNT(*) FROM afluencia_metrobus WHERE tipo_pago LIKE %s",
        ("%\\_SIM",),
    )
    total = cur.fetchone()[0]
    print(f"\n[faker_afluencia] Total filas sintéticas en BD: {total}")
    cur.close()
    conn.close()
    print("[faker_afluencia] OK")


if __name__ == "__main__":
    main()
