# Postman Guide — RutaMx Admin API

Base URL: `http://localhost:8080`

All endpoints require `Authorization: Bearer <token>` unless noted otherwise.

---

## How to get a Firebase ID Token

Send this request to get a valid Bearer token:

```
POST https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=YOUR_FIREBASE_WEB_API_KEY
Content-Type: application/json

{
  "email": "admin@rutamx.com",
  "password": "YourPassword",
  "returnSecureToken": true
}
```

The response contains `idToken`. Use that value as the Bearer token in subsequent requests.

> **Important:** The seed ADMIN user's `firebase_uuid` in `import.sql` is `seed-admin-placeholder`. After creating the admin account in the Firebase console (with email `admin@rutamx.com`), replace `seed-admin-placeholder` in the `users` table with the real Firebase UID assigned by Firebase. Until then, the auth filter will not resolve the admin user.

---

## Endpoints

### GET /admin/users — List all users

| Field        | Value                              |
|--------------|------------------------------------|
| Method       | `GET`                              |
| URL          | `http://localhost:8080/admin/users` |
| Auth         | `Authorization: Bearer <token>`    |
| Request body | none                               |

**Expected response — 200 OK:**
```json
[
  {
    "id": 1,
    "email": "admin@rutamx.com",
    "firebaseUuid": "...",
    "firstName": "Admin",
    "lastName": "RutaMx",
    "roleId": 1,
    "roleName": "ADMIN",
    "status": "ACTIVE",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  }
]
```

---

### GET /admin/users/{id} — Get one user

| Field        | Value                                  |
|--------------|----------------------------------------|
| Method       | `GET`                                  |
| URL          | `http://localhost:8080/admin/users/1`  |
| Auth         | `Authorization: Bearer <token>`        |
| Request body | none                                   |

**Expected response — 200 OK:** single user object (same structure as above)

**Expected response — 404 Not Found:**
```
Usuario no encontrado
```

---

### POST /admin/users — Create user (HU01)

| Field        | Value                               |
|--------------|-------------------------------------|
| Method       | `POST`                              |
| URL          | `http://localhost:8080/admin/users` |
| Auth         | `Authorization: Bearer <token>`     |
| Content-Type | `application/json`                  |

**Request body:**
```json
{
  "firstName": "Luis",
  "lastName": "García",
  "email": "luis.garcia@rutamx.com",
  "password": "Passw0rd123",
  "roleId": 2
}
```

Password rules: min 8 chars, max 64 chars, at least one uppercase letter, at least one digit.

`roleId` values available after seed:
- `1` = ADMIN
- `2` = CEO
- `3` = COO
- `4` = CMO

**Expected response — 201 Created:**
```json
{
  "id": 3,
  "email": "luis.garcia@rutamx.com",
  "firebaseUuid": "firebase-generated-uid",
  "firstName": "Luis",
  "lastName": "García",
  "roleId": 2,
  "roleName": "CEO",
  "status": "ACTIVE",
  "createdAt": "2024-06-01T10:00:00",
  "updatedAt": "2024-06-01T10:00:00"
}
```

**Expected response — 409 Conflict (duplicate email):**
```
El correo ya está registrado
```

**Expected response — 400 Bad Request (validation failure):**
```json
{
  "title": "Constraint Violation",
  "violations": [...]
}
```

---

### PUT /admin/users/{id} — Update user (HU02)

| Field        | Value                                    |
|--------------|------------------------------------------|
| Method       | `PUT`                                    |
| URL          | `http://localhost:8080/admin/users/3`    |
| Auth         | `Authorization: Bearer <token>`          |
| Content-Type | `application/json`                       |

All fields are optional. Only non-null fields are updated. Email and password cannot be changed here.

**Request body:**
```json
{
  "firstName": "Luis Updated",
  "lastName": "García Updated",
  "roleId": 3
}
```

**Expected response — 200 OK:** updated user object

**Expected response — 404 Not Found:**
```
Usuario no encontrado
```

---

### DELETE /admin/users/{id} — Delete user (HU03)

| Field        | Value                                    |
|--------------|------------------------------------------|
| Method       | `DELETE`                                 |
| URL          | `http://localhost:8080/admin/users/3`    |
| Auth         | `Authorization: Bearer <token>`          |
| Request body | none                                     |

**Expected response — 204 No Content**

**Expected response — 404 Not Found:**
```
Usuario no encontrado
```

**Expected response — 400 Bad Request (self-delete attempt):**
```
No puedes eliminar tu propia cuenta
```

---

### PATCH /admin/users/{id}/activate — Activate user

| Field        | Value                                            |
|--------------|--------------------------------------------------|
| Method       | `PATCH`                                          |
| URL          | `http://localhost:8080/admin/users/3/activate`   |
| Auth         | `Authorization: Bearer <token>`                  |
| Request body | none                                             |

**Expected response — 200 OK:** updated user object with `"status": "ACTIVE"`

**Expected response — 404 Not Found:**
```
Usuario no encontrado
```

**Expected response — 409 Conflict (already active):**
```
El usuario ya está activo
```

---

### PATCH /admin/users/{id}/suspend — Suspend user (HU04)

| Field        | Value                                            |
|--------------|--------------------------------------------------|
| Method       | `PATCH`                                          |
| URL          | `http://localhost:8080/admin/users/3/suspend`    |
| Auth         | `Authorization: Bearer <token>`                  |
| Request body | none                                             |

**Expected response — 200 OK:** updated user object with `"status": "SUSPENDED"`

**Expected response — 404 Not Found:**
```
Usuario no encontrado
```

**Expected response — 409 Conflict (already suspended):**
```
El usuario ya está suspendido
```

**Expected response — 400 Bad Request (self-suspend attempt):**
```
No puedes suspender tu propia cuenta
```

---

## Authentication Errors

| Status | Meaning                                    |
|--------|--------------------------------------------|
| 401    | Missing or invalid Bearer token            |
| 403    | Token valid but user is not ACTIVE or ADMIN |
