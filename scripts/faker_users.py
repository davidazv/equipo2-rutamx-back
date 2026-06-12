#!/usr/bin/env python3
"""
faker_users.py — Genera N usuarios sintéticos en MySQL `rutamx`.

Útil para pruebas de carga: simula universo de 10K cuentas, mezcla roles
ADMIN/CEO/COO/CMO y estados ACTIVE/SUSPENDED para validar índices, login
bajo concurrencia y políticas RBAC.

Uso:
    pip install -r scripts/requirements.txt
    python3 scripts/faker_users.py --count 10000 --password NewPassword

Opciones:
    --count       N usuarios (default 10000)
    --password    password root MySQL (default vacío)
    --host        host (default 127.0.0.1)
    --port        puerto (default 3306)
    --user        usuario MySQL (default root)
    --batch       tamaño batch para executemany (default 1000)
    --truncate    elimina users sintéticos previos (email termina en
                  @faker.rutamx.com) antes de insertar
    --seed        semilla Faker para reproducibilidad (default 42)

Restricciones:
    - email único (uq_users_email) → Faker.unique
    - firebase_UUID único → uuid4 truncado a 28 chars (formato Firebase)
    - role_id FK a roles.id (debe existir admin/ceo/coo/cmo)
    - status ENUM('ACTIVE','SUSPENDED')

Distribución generada:
    Roles:   80% CMO, 10% COO, 5% CEO, 5% ADMIN
    Status:  90% ACTIVE, 10% SUSPENDED
"""
import argparse
import random
import sys
import uuid
from datetime import datetime, timedelta

from faker import Faker
import mysql.connector


ROLE_DIST = [
    ("CMO", 0.80),
    ("COO", 0.10),
    ("CEO", 0.05),
    ("ADMIN", 0.05),
]
STATUS_DIST = [("ACTIVE", 0.90), ("SUSPENDED", 0.10)]
EMAIL_DOMAIN = "faker.rutamx.com"


def weighted_choice(dist):
    r = random.random()
    acc = 0.0
    for value, weight in dist:
        acc += weight
        if r <= acc:
            return value
    return dist[-1][0]


def fetch_role_map(cur):
    cur.execute("SELECT name, id FROM roles")
    return {row[0]: row[1] for row in cur.fetchall()}


def truncate_synthetic(cur):
    cur.execute(
        "DELETE FROM users WHERE email LIKE %s",
        (f"%@{EMAIL_DOMAIN}",),
    )
    return cur.rowcount


def generate_user_row(fake, role_map):
    first = fake.first_name()
    last = fake.last_name()
    # Email único — Faker.unique evita duplicados dentro del batch.
    # Sufijo numérico controla colisiones entre runs distintos.
    suffix = random.randint(1000, 9999)
    email = (
        f"{first.lower()}.{last.lower()}.{suffix}@{EMAIL_DOMAIN}"
    )
    firebase_uuid = uuid.uuid4().hex[:28]  # 28 chars como tokens Firebase reales
    role_name = weighted_choice(ROLE_DIST)
    role_id = role_map[role_name]
    status = weighted_choice(STATUS_DIST)
    # Spread de fechas en últimos 2 años para que el event scheduler tenga
    # algo que suspender (registros > 180 días).
    days_ago = random.randint(0, 730)
    created = datetime.now() - timedelta(days=days_ago)
    updated = created + timedelta(days=random.randint(0, min(days_ago, 200)))
    return (
        email,
        firebase_uuid,
        first[:100],
        last[:100],
        role_id,
        status,
        created,
        updated,
    )


def main():
    p = argparse.ArgumentParser(description="Generador Faker para users RutaMx")
    p.add_argument("--count", type=int, default=10000)
    p.add_argument("--password", default="")
    p.add_argument("--host", default="127.0.0.1")
    p.add_argument("--port", type=int, default=3306)
    p.add_argument("--user", default="root")
    p.add_argument("--batch", type=int, default=1000)
    p.add_argument("--truncate", action="store_true")
    p.add_argument("--seed", type=int, default=42)
    args = p.parse_args()

    random.seed(args.seed)
    fake = Faker("es_MX")
    Faker.seed(args.seed)

    print(f"[faker_users] Conectando a {args.user}@{args.host}:{args.port}/rutamx")
    conn = mysql.connector.connect(
        host=args.host,
        port=args.port,
        user=args.user,
        password=args.password,
        database="rutamx",
        autocommit=False,
    )
    cur = conn.cursor()

    role_map = fetch_role_map(cur)
    missing = {"ADMIN", "CEO", "COO", "CMO"} - set(role_map.keys())
    if missing:
        print(f"ERROR: roles faltantes en BD: {missing}")
        sys.exit(1)
    print(f"  Roles disponibles: {role_map}")

    if args.truncate:
        deleted = truncate_synthetic(cur)
        conn.commit()
        print(f"  Eliminados {deleted} users sintéticos previos")

    sql = (
        "INSERT INTO users "
        "(email, firebase_UUID, first_name, last_name, role_id, status, "
        " created_at, updated_at) "
        "VALUES (%s, %s, %s, %s, %s, %s, %s, %s)"
    )

    print(f"[faker_users] Insertando {args.count} filas en batches de {args.batch}")
    inserted = 0
    while inserted < args.count:
        remain = args.count - inserted
        chunk = min(args.batch, remain)
        rows = [generate_user_row(fake, role_map) for _ in range(chunk)]
        try:
            cur.executemany(sql, rows)
            conn.commit()
            inserted += chunk
            print(f"  {inserted}/{args.count}")
        except mysql.connector.IntegrityError as e:
            conn.rollback()
            print(f"  WARN colisión: {e}. Reintentando con offset…")
            # nuevo sufijo y reintento individual de las filas
            for row in rows:
                try:
                    cur.execute(sql, row)
                    conn.commit()
                    inserted += 1
                except mysql.connector.IntegrityError:
                    pass

    # Resumen
    cur.execute("SELECT COUNT(*) FROM users WHERE email LIKE %s",
                (f"%@{EMAIL_DOMAIN}",))
    total = cur.fetchone()[0]
    cur.execute(
        "SELECT r.name, COUNT(*) FROM users u "
        "JOIN roles r ON r.id = u.role_id "
        "WHERE u.email LIKE %s GROUP BY r.name",
        (f"%@{EMAIL_DOMAIN}",),
    )
    print(f"\n[faker_users] Total users sintéticos en BD: {total}")
    for name, n in cur.fetchall():
        print(f"  {name:<6} {n}")

    cur.close()
    conn.close()
    print("[faker_users] OK")


if __name__ == "__main__":
    main()
