# equipo2-rutamx-back

Backend del sistema RutaMx — Quarkus 3.8 + Hibernate ORM Panache + MySQL 8 + Firebase Admin SDK.

## Requisitos previos

| Herramienta | Versión mínima | Instalación (macOS)                    |
| ----------- | -------------- | -------------------------------------- |
| Java (JDK)  | 17             | `brew install openjdk@17`              |
| Maven       | 3.9            | `brew install maven`                   |
| MySQL       | 8.x            | `brew install mysql` o MySQL Workbench |

> Si no tienes Homebrew: `/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"`

## Configuración inicial (una sola vez)

### 1. Credenciales de Firebase

Descarga el JSON de service account desde Firebase Console:
`Configuración del proyecto → Cuentas de servicio → Generar nueva clave privada`

Colócalo en:

```
src/main/resources/rutamx-firebase-adminsdk.json
```

> Este archivo está en `.gitignore` — nunca se sube al repositorio.

### 2. Contraseña de MySQL

Edita `src/main/resources/application.properties` y reemplaza el valor de:

```properties
quarkus.datasource.password=TU_PASSWORD_AQUI
```

### 3. Esquema de base de datos

La base de datos `rutamx` y las tablas deben existir antes de arrancar la app.
Ejecuta este SQL en MySQL (Workbench, DBeaver o terminal):

```sql
CREATE DATABASE IF NOT EXISTS rutamx CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE rutamx;

CREATE TABLE roles (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50)  NOT NULL,
    description VARCHAR(255) NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uq_roles_name UNIQUE (name)
) ENGINE=InnoDB;

CREATE TABLE users (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    email         VARCHAR(255)  NOT NULL,
    firebase_UUID VARCHAR(255)  NOT NULL,
    first_name    VARCHAR(100)  NULL,
    last_name     VARCHAR(100)  NULL,
    role_id       BIGINT        NOT NULL,
    status        ENUM('ACTIVE','SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uq_users_email    UNIQUE (email),
    CONSTRAINT uq_users_firebase UNIQUE (firebase_UUID),
    CONSTRAINT fk_users_role     FOREIGN KEY (role_id) REFERENCES roles(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE INDEX idx_users_status ON users (status);

INSERT INTO roles (name, description) VALUES
  ('ADMIN', 'Administrador del sistema'),
  ('CEO',   'Chief Executive Officer'),
  ('COO',   'Chief Operating Officer'),
  ('CMO',   'Chief Marketing Officer');
```

### 4. Usuario ADMIN inicial

Crea el usuario administrador en Firebase Console (Authentication → Add user) con el email y contraseña que quieras.
Firebase te asignará un UID. Luego inserta el usuario en MySQL con ese UID:

```sql
INSERT INTO users (email, firebase_UUID, first_name, last_name, role_id, status)
VALUES ('tu-email@dominio.com', 'UID_REAL_DE_FIREBASE', 'Nombre', 'Apellido', 1, 'ACTIVE');
```

## Arrancar la aplicación

```bash
mvn quarkus:dev
```

La app queda disponible en `http://localhost:8080`.
La primera vez descarga dependencias — puede tardar 2-5 minutos.

> **Java 22 o superior:** el repo incluye `.mvn/jvm.config` con `-Dnet.bytebuddy.experimental=true` para que funcione sin configuración adicional.

## Ejecutar tests

```bash
mvn test
```

Los tests usan H2 en memoria — no tocan MySQL.

## Usar la API

Ver [`docs/POSTMAN_GUIDE.md`](docs/POSTMAN_GUIDE.md) para la lista completa de endpoints, cuerpos JSON y ejemplos.

### Obtener token de Firebase (para Postman)

La API Key del proyecto Firebase se usa **solo en el cliente / Postman** para hacer login y obtener un `idToken`.
El backend no la usa — él se autentica con el JSON de service account.

```
POST https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=APIKEY
Content-Type: application/json

{
  "email": "tu-email@dominio.com",
  "password": "tu-contraseña",
  "returnSecureToken": true
}
```

Copia el `idToken` de la respuesta y úsalo como header en todas las llamadas:

```
Authorization: Bearer <idToken>
```

## Estructura del proyecto

```
src/main/java/org/acme/
├── application/
│   ├── dto/          ← CreateUserDto, UpdateUserDto
│   ├── exception/    ← DuplicateEmailException, UserNotFoundException, ...
│   └── usecase/      ← CreateUserUseCase, UpdateUserUseCase, DeleteUserUseCase, SuspendUserUseCase
├── domain/
│   ├── models/       ← User, Role, UserStatus
│   └── repository/   ← UserRepository, RoleRepository (interfaces)
├── infrastructure/
│   ├── config/       ← FirebaseConfig
│   ├── entities/     ← UserEntity, RoleEntity
│   ├── firebase/     ← FirebaseUserCreator
│   ├── mapper/       ← UserMapper, RoleMapper
│   ├── repository/   ← UserRepositoryImpl, RoleRepositoryImpl
│   └── security/     ← FirebaseAuthFilter, AuthContext
└── interfaces/
    └── rest/         ← UserResource
```
