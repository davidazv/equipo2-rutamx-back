#Backend Constitution — RutaMx

Rules and conventions for the Quarkus + Panache backend of the RutaMx platform. All decisions have room for documented exceptions — when you break a rule, leave a comment explaining why.

---

## Table of Contents

1. [Project Context](#project-context)
2. [Naming Conventions](#naming-conventions)
3. [Layer Responsibilities](#layer-responsibilities)
4. [Dependency Injection](#dependency-injection)
5. [Use Cases](#use-cases)
6. [Repositories](#repositories)
7. [DTOs](#dtos)
8. [Mappers](#mappers)
9. [Entity Relationships & Fetch Strategies](#entity-relationships--fetch-strategies)
10. [Error Handling](#error-handling)
11. [Logging](#logging)
12. [Testing Strategy](#testing-strategy)
13. [Branching Strategy](#branching-strategy)
14. [Commit & PR Conventions](#commit--pr-conventions)

---

## Project Context

RutaMx is a SaaS platform for simulating and monitoring electric bus fleet autonomy in Mexico City, built for clients SEMOVI and Yutong.

**Tech stack:** Quarkus 3.x + Maven, Hibernate ORM with Panache, MySQL 8.x, Firebase Admin SDK, JAX-RS (RESTEasy Jackson), Hibernate Validator, Java 17. H2 in-memory for the `%test` profile only.

**Base package:** `org.acme`

**Package structure:**
src/main/java/org/acme/
├── application/
│ ├── dto/
│ └── usecase/
├── domain/
│ ├── models/
│ └── repository/
├── infrastructure/
│ ├── config/ ← FirebaseConfig.java
│ ├── entities/ ← JPA entities
│ ├── firebase/ ← FirebaseUserCreator.java
│ ├── mapper/ ← static mapper classes
│ ├── repository/ ← Panache implementations
│ └── security/ ← FirebaseAuthFilter.java, AuthContext.java
└── interfaces/
└── rest/ ← JAX-RS resources

**Database:** MySQL 8.x, database name `rutamx`. Schema is managed manually — never let Hibernate recreate or drop it.

**Schema management rules:**

- `validate` → MySQL local and production. Hibernate validates against the existing schema, never touches data.
- `drop-and-create` → H2 in-memory during test runs only. H2 starts empty every time so tables must be created fresh. This never touches MySQL.

**application.properties baseline:**

```properties
quarkus.datasource.db-kind=mysql
quarkus.datasource.username=root
quarkus.datasource.password=REPLACE_ME
quarkus.datasource.jdbc.url=jdbc:mysql://localhost:3306/rutamx
quarkus.hibernate-orm.schema-management.strategy=validate
quarkus.hibernate-orm.log.sql=true
quarkus.hibernate-orm.log.format-sql=true
quarkus.http.cors=true
quarkus.http.cors.origins=*
quarkus.http.port=8080
firebase.credentials=src/main/resources/rutamx-firebase-adminsdk.json

%test.quarkus.datasource.db-kind=h2
%test.quarkus.datasource.username=sa
%test.quarkus.datasource.password=
%test.quarkus.datasource.jdbc.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE
%test.quarkus.datasource.jdbc.driver=org.h2.Driver
%test.quarkus.hibernate-orm.schema-management.strategy=drop-and-create
```

---

## Database Schema

The schema is created manually in MySQL before running the app. Never modify it without coordinating with the team.

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
    firebase_uuid VARCHAR(255)  NOT NULL,
    first_name    VARCHAR(100)  NULL,
    last_name     VARCHAR(100)  NULL,
    role_id       BIGINT        NOT NULL,
    status        ENUM('ACTIVE','SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uq_users_email    UNIQUE (email),
    CONSTRAINT uq_users_firebase UNIQUE (firebase_uuid),
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

Seed data for `import.sql` (loaded only when `drop-and-create` is active, i.e. tests):

- One ADMIN user with `firebase_uuid = 'seed-admin-placeholder'`.
- Developer replaces the placeholder with a real Firebase UID after creating the account in Firebase console.

---

## Naming Conventions

| What                      | Pattern                  | Example                                   |
| ------------------------- | ------------------------ | ----------------------------------------- |
| Domain model              | `{Entity}`               | `User`                                    |
| JPA entity                | `{Entity}Entity`         | `UserEntity`                              |
| Repository interface      | `{Entity}Repository`     | `UserRepository`                          |
| Repository implementation | `{Entity}RepositoryImpl` | `UserRepositoryImpl`                      |
| Use case                  | `{Verb}{Entity}UseCase`  | `CreateUserUseCase`, `SuspendUserUseCase` |
| Mapper                    | `{Entity}Mapper`         | `UserMapper`                              |
| Input DTO                 | `{Verb}{Entity}Dto`      | `CreateUserDto`, `UpdateUserDto`          |
| REST resource             | `{Entity}Resource`       | `UserResource`                            |

Use case verbs describe the business action: `Create`, `Update`, `Delete`, `Suspend`, `Register`. Packages mirror the layer: `domain`, `application`, `infrastructure`, `interfaces.rest`.

---

## Layer Responsibilities

interfaces/rest → HTTP only. Receives request, calls use case, returns response.
application/usecase → Business logic. Builds domain objects, calls repositories.
domain → Business model. Pure Java, zero framework dependencies.
infrastructure → Everything technical: JPA, Panache, Firebase, mappers.

Hard rules:

- **Interfaces** never access repositories directly — only use cases.
- **Application** never imports `jakarta.persistence`, Hibernate, or Firebase classes.
- **Domain** never imports anything outside standard Java.
- **Infrastructure** is the only layer allowed to know about persistence, external APIs, or framework internals.

---

## Dependency Injection

Use constructor injection for your own dependencies. Place `@Inject` on the constructor, not on fields. Fields injected through the constructor must be `private final`.

**Exception:** framework-provided infrastructure like `EntityManager` may use field injection when constructor injection is impractical inside a `PanacheRepositoryBase` subclass.

```java
@ApplicationScoped
public class CreateUserUseCase {

    private final UserRepository userRepository;
    private final FirebaseUserCreator firebaseUserCreator;

    @Inject
    public CreateUserUseCase(UserRepository userRepository, FirebaseUserCreator firebaseUserCreator) {
        this.userRepository = userRepository;
        this.firebaseUserCreator = firebaseUserCreator;
    }
}
```

Scopes:

- `@ApplicationScoped` — use cases, repositories, services.
- `@RequestScoped` — anything holding per-request state (`AuthContext`).
- JAX-RS resources do not declare a CDI scope — Quarkus manages them.

Always inject the interface, never the implementation class.

**Isolate static dependencies.** Any SDK with a static API (e.g. `FirebaseAuth.getInstance()`) must be wrapped in a thin `@ApplicationScoped` CDI bean. This makes it injectable and mockable in tests.

```java
@ApplicationScoped
public class FirebaseUserCreator {
    public UserRecord create(String email, String password) throws FirebaseAuthException {
        return FirebaseAuth.getInstance().createUser(...);
    }
}
```

---

## Use Cases

One class per business operation. One public method: `execute(...)`.

Rules:

- ID generation happens in the use case when needed.
- Business field assignment happens in the use case.
- **Exception — audit timestamps** (`createdAt`, `updatedAt`): set by the repository, not the use case.
- Use cases read `AuthContext` to obtain the authenticated user when needed.
- No HTTP objects (`Response`, `UriInfo`, etc.) inside a use case.

---

## Repositories

Each aggregate root has a port interface in `domain/repository/` and an implementation in `infrastructure/repository/`.

**Interface** — expresses intent only, no framework imports:

```java
public interface UserRepository {
    User create(User user);
    Optional<User> findById(Long id);
    Optional<User> findByFirebaseUuid(String firebaseUuid);
    List<User> findAll();
    User update(User user);
    void delete(Long id);
}
```

**Implementation** — implements the domain interface and `PanacheRepositoryBase<Entity, ID>`:

```java
@ApplicationScoped
public class UserRepositoryImpl implements UserRepository, PanacheRepositoryBase<UserEntity, Long> {

    @Override
    @Transactional
    public User create(User user) {
        UserEntity entity = UserMapper.toEntity(user);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        persist(entity);
        return UserMapper.toDomain(entity);
    }
}
```

Rules:

- `@Transactional` goes on write methods in the repository implementation, nowhere else.
- Return type is always a domain model, never an entity.
- Use Panache convenience methods for simple operations; use `EntityManager` for JPQL or query hints.

---

## DTOs

Input-only. Live in `application/dto/`. Carry only what the client sends.

Rules:

- Must have a no-arg constructor (required for JSON deserialization).
- Never include `id`, `createdAt`, `updatedAt`, `firebaseUuid`, or `status` — the use case sets those.
- No business logic inside a DTO.
- No response DTOs: domain models are returned directly and serialized by JAX-RS.

---

## Mappers

Static utility classes in `infrastructure/mapper/`. Convert between JPA entities and domain models.

Rules:

- All methods are `public static` — never instantiate a mapper.
- `toEntity` maps scalar fields only. Associations are wired by the repository after mapping.
- `toDomain` guards every lazy association with `Hibernate.isInitialized(...)` before accessing it.

---

## Entity Relationships & Fetch Strategies

All associations are `FetchType.LAZY`. Always declare it explicitly — never rely on JPA defaults.

Choose the fetch strategy based on what the query needs:

| Need                                    | Strategy                          |
| --------------------------------------- | --------------------------------- |
| Scalar fields only                      | DTO projection (`SELECT new ...`) |
| Specific associations for one query     | `LEFT JOIN FETCH` in JPQL         |
| Same associations reused across methods | `@NamedEntityGraph` + query hint  |

---

## Error Handling

Rules:

- Infrastructure exceptions (Firebase, DB) are caught in the resource layer and translated into HTTP responses.
- Return meaningful HTTP status codes: `400` bad input, `401` unauthenticated, `403` forbidden, `404` not found, `409` conflict, `500` unexpected.
- Never expose internal exception messages or stack traces to the client.
- Always call `return;` immediately after every `requestContext.abortWith(...)` in filters.

```java
@POST
public Response createUser(@Valid CreateUserDto dto) {
    try {
        return Response.status(201).entity(createUserUseCase.execute(dto)).build();
    } catch (DuplicateEmailException e) {
        return Response.status(409).entity("El correo ya está registrado").build();
    } catch (Exception e) {
        log.severe("Unexpected error: " + e.getMessage());
        return Response.serverError().entity("Error inesperado").build();
    }
}
```

---

## Logging

Use a proper logger, not `System.out.println`:

```java
private static final Logger log = Logger.getLogger(CreateUserUseCase.class.getName());
```

Rules:

- Log at entry points of meaningful operations.
- Never log passwords, tokens, or PII.
- Errors caught in the resource layer must be logged before returning the HTTP response.

---

## Testing Strategy

| Type        | Tool                | What it tests                          | Speed                      |
| ----------- | ------------------- | -------------------------------------- | -------------------------- |
| Unit        | JUnit 5 + Mockito   | One use case in isolation              | Fast — no Quarkus, no DB   |
| Integration | `@QuarkusTest` + H2 | Full HTTP → use case → repository → DB | Slower — full Quarkus boot |

### Unit tests

Instantiate the class under test directly, pass mocks via constructor. Do not start Quarkus.

```java
@BeforeEach
void setUp() {
    userRepository = mock(UserRepository.class);
    firebaseUserCreator = mock(FirebaseUserCreator.class);
    useCase = new CreateUserUseCase(userRepository, firebaseUserCreator);
}
```

Rules:

- Mock only the direct dependencies of the class under test.
- Use `ArgumentCaptor` to verify what was passed to a mock.
- Test method names follow `methodShouldBehaviorWhenCondition`.

### Integration tests

Start Quarkus with H2 using the `%test` profile. H2 starts empty — `drop-and-create` creates the tables fresh each run. MySQL is never touched.

Replace `FirebaseAuthFilter` in tests with a subclass that skips Firebase and injects a test user directly:

```java
@Mock
@Provider
@Priority(Priorities.AUTHENTICATION)
@ApplicationScoped
public class TestFirebaseAuthFilter extends FirebaseAuthFilter {

    @Inject
    AuthContext authContext;

    @Override
    public void filter(ContainerRequestContext ctx) {
        User testUser = new User();
        testUser.setId(1L);
        testUser.setRoleName("ADMIN");
        testUser.setStatus("ACTIVE");
        authContext.setUser(testUser);
    }
}
```

This class lives in `src/test/java/org/acme/interfaces/rest/`.

Rules:

- After a write operation, re-read from the DB and assert — never trust only the HTTP response.
- The only thing mocked in integration tests is external services that can't run locally (Firebase). Everything else runs real.

---

## Branching Strategy

**Every session: before writing any code, run `git branch` to confirm the current branch. If you are on `main` or `develop`, stop and create or switch to the correct branch.**

This project follows Gitflow. Branch naming uses the HU identifier:

| Branch                         | Purpose               | Created from | Merges into        |
| ------------------------------ | --------------------- | ------------ | ------------------ |
| `main`                         | Production            | —            | —                  |
| `develop`                      | Integration           | `main`       | `release/*`        |
| `feature/HU{nn}-{description}` | New functionality     | `develop`    | `develop`          |
| `bugfix/HU{nn}-{description}`  | Bug fix in develop    | `develop`    | `develop`          |
| `hotfix/{description}`         | Urgent production fix | `main`       | `main` + `develop` |

Examples: `feature/HU01-crear-cuentas-usuario`, `bugfix/HU03-delete-wrong-id`.

Rules:

- `main` and `develop` are protected — never commit directly to them.
- Delete remote and local branch after merge.
- Tag `main` after every merge: `git tag -a v1.x.x -m "Release 1.x.x"`.

---

## Commit & PR Conventions

### Commit messages — Conventional Commits

<type>(<scope>): <short description>

| Type       | When                                |
| ---------- | ----------------------------------- |
| `feat`     | New feature                         |
| `fix`      | Bug fix                             |
| `test`     | Adding or fixing tests              |
| `chore`    | Maintenance, dependency updates     |
| `refactor` | Code change with no behavior change |
| `docs`     | Documentation only                  |

Examples:
feat(user): add POST /admin/users endpoint (HU01)
fix(user): prevent admin from deleting own account (HU03)
test(user): add suspend use case unit tests (HU04)

Rules:

- Scope is the affected module in lowercase: `user`, `auth`.
- Description is imperative, lowercase, no period.
- One logical change per commit.

### Pull requests

- Title follows Conventional Commits format.
- Base branch is always `develop` (except `hotfix/*`, which targets `main`).
- Description includes: what changed, why, and the HU reference.
- Every bugfix PR must include a test that was red before the fix and green after.
