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

### 3. Esquema y datos iniciales

Antes de arrancar la app habilita `local_infile` en MySQL (solo se hace una vez):

```sql
SET GLOBAL local_infile = 1;
```

Luego ejecuta el script de inicialización desde la raíz del proyecto:

```bash
./scripts/seed-data.sh              # root sin contraseña
./scripts/seed-data.sh -p secret    # root con contraseña
```

Este script:
1. Crea la base de datos `rutamx` y todas las tablas (`docs/Initialize-mysql.sql`)
2. Carga los 10 CSVs de `data/` con datos GTFS, modelos de autobús y afluencia Metrobús
3. Inserta los roles y el usuario admin inicial

> Sin este paso los KPIs y funcionalidades que dependen de datos GTFS o modelos de autobús no funcionarán.

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
