# RutaMx — Data Pipeline Guide

## Overview

RutaMx uses GTFS (General Transit Feed Specification) data along with bus model catalog data and Metrobús ridership data. All source data lives as CSV files in `data/` and is loaded into MySQL via the seed script.

## Quick Start

### 1. Initialize the database

```bash
# From the backend root directory
./scripts/seed-data.sh              # root without password
./scripts/seed-data.sh -p secret    # root with password
./scripts/seed-data.sh -u admin -p secret -H 192.168.1.5 -P 3307
```

This script:
1. Runs `docs/Initialize-mysql.sql` — creates the `rutamx` database, all tables, default roles, and admin user
2. Loads all CSV files from `data/` into their corresponding tables using `LOAD DATA LOCAL INFILE`

### 2. Prerequisites

- MySQL 8.x running locally
- `local_infile` enabled on the MySQL server:
  ```sql
  SET GLOBAL local_infile = 1;
  ```
- Connect with `--local-infile=1` (the script handles this automatically)

## Data Sources

| CSV File | Table | Rows | Description |
|----------|-------|------|-------------|
| `bus_models.csv` | `bus_models` | 6 | Yutong electric & diesel bus specifications |
| `agency.csv` | `agency` | 10 | GTFS transport agencies |
| `calendar.csv` | `calendar` | 13 | GTFS service schedules by day of week |
| `routes.csv` | `routes` | 301 | GTFS transit routes |
| `shapes.csv` | `shapes` | 127,135 | GTFS route geographic traces (lat/lon + distance) |
| `trips.csv` | `trips` | 1,205 | GTFS scheduled trips per route |
| `stops.csv` | `stops` | 11,362 | GTFS stop locations |
| `stop_times.csv` | `stop_times` | 42,789 | GTFS arrival/departure times per stop per trip |
| `frequencies.csv` | `frequencies` | 1,584 | GTFS headway frequencies |
| `afluenciamb.csv` | `afluencia_metrobus` | 26,390 | SEMOVI Metrobús daily ridership by line |

## Load Order (FK Dependencies)

Tables must be loaded in this order:

```
bus_models ─────────┐
agency ─────────────┤
calendar ───────────┤
stops ──────────────┤
routes ← agency     │
shapes ─────────────┤
trips ← routes, calendar
stop_times ← trips, stops
frequencies ← trips │
afluencia_metrobus ─┘
```

## Route Distance Computation

The `routes` table does not have a `distance_km` column. Route distance is computed dynamically by joining `routes` → `trips` → `shapes`:

```sql
SELECT r.route_id, MAX(s.shape_dist_traveled) AS distance_km
FROM routes r
JOIN trips t ON t.route_id = r.route_id
JOIN shapes s ON s.shape_id = t.shape_id
GROUP BY r.route_id
HAVING MAX(s.shape_dist_traveled) > 0;
```

The `shapes` table has cumulative `shape_dist_traveled` values. The MAX value per shape gives the one-way distance for that route.

## Creating New Endpoints

Follow the Constitution.md architecture. Example: adding a new entity/endpoint.

### 1. JPA Entity (`infrastructure/entities/`)

```java
@Entity
@Table(name = "your_table")
public class YourEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // fields with @Column annotations...
}
```

### 2. Domain Model (`domain/models/`)

Pure Java, no framework imports:

```java
public class YourModel {
    private Long id;
    // getters/setters...
}
```

### 3. Mapper (`infrastructure/mapper/`)

Static utility class:

```java
public class YourMapper {
    public static YourModel toDomain(YourEntity entity) { ... }
    public static YourEntity toEntity(YourModel model) { ... }
}
```

### 4. Repository Interface (`domain/repository/`)

```java
public interface YourRepository {
    List<YourModel> findAll();
    Optional<YourModel> findById(Long id);
}
```

### 5. Repository Implementation (`infrastructure/repository/`)

```java
@ApplicationScoped
public class YourRepositoryImpl implements YourRepository {
    @Inject EntityManager entityManager;
    // implement methods, return domain models...
}
```

### 6. Use Case (`application/usecase/`)

One class, one public `execute()` method:

```java
@ApplicationScoped
public class DoSomethingUseCase {
    @Inject
    public DoSomethingUseCase(YourRepository repo) { ... }
    public Result execute(params) { ... }
}
```

### 7. REST Resource (`interfaces/rest/`)

```java
@Path("/api/your-resource")
@Produces(MediaType.APPLICATION_JSON)
public class YourResource {
    @Inject
    public YourResource(YourUseCase useCase) { ... }

    @GET
    public Response list() { ... }
}
```

### 8. Test Data (`import.sql`)

Add INSERT statements for H2 test database. Tables with `created_at`/`updated_at` NOT NULL columns must include explicit `CURRENT_TIMESTAMP` values.
