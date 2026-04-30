package org.acme.interfaces.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class BusModelResourceTest {

    @Test
    void getShouldReturnAllBusModels() {
        given()
                .when().get("/api/bus-models")
                .then()
                .statusCode(200)
                .body("$.size()", greaterThanOrEqualTo(6))
                .body("[0].name", notNullValue())
                .body("[0].manufacturer", equalTo("Yutong"))
                .body("[0].fuelType", notNullValue());
    }

    @Test
    void getShouldReturnSingleBusModel() {
        given()
                .when().get("/api/bus-models/1")
                .then()
                .statusCode(200)
                .body("name", equalTo("Yutong E12PRO"))
                .body("fuelType", equalTo("ELECTRIC"))
                .body("unitCostUsd", notNullValue());
    }

    @Test
    void getShouldReturn404WhenBusModelNotFound() {
        given()
                .when().get("/api/bus-models/999")
                .then()
                .statusCode(404);
    }

    @Test
    void getShouldReturnDieselModelWithCorrectFuelType() {
        given()
                .when().get("/api/bus-models/4")
                .then()
                .statusCode(200)
                .body("name", equalTo("Yutong DMT Hybrid H8"))
                .body("fuelType", equalTo("DIESEL"));
    }

    @Test
    void getShouldReturnAllFieldsForBusModel() {
        given()
                .when().get("/api/bus-models/1")
                .then()
                .statusCode(200)
                .body("name", notNullValue())
                .body("manufacturer", notNullValue())
                .body("fuelType", notNullValue())
                .body("autonomyKm", notNullValue())
                .body("passengerCapacity", notNullValue())
                .body("unitCostUsd", notNullValue())
                .body("batteryCapacityKwh", notNullValue())
                .body("energyConsumptionKwhKm", notNullValue())
                .body("maintenanceCostPerKm", notNullValue());
    }

    @Test
    void listShouldReturnModelsOrderedById() {
        int firstId = given()
                .when().get("/api/bus-models")
                .then()
                .statusCode(200)
                .extract().jsonPath().getInt("[0].id");
        int lastId = given()
                .when().get("/api/bus-models")
                .then()
                .extract().jsonPath().getInt("[5].id");
        assertTrue(firstId < lastId, "Models should be ordered by id ascending");
    }

    @Test
    void listShouldIncludeElectricAndDieselModels() {
        given()
                .when().get("/api/bus-models")
                .then()
                .statusCode(200)
                .body("fuelType", hasItems("ELECTRIC", "DIESEL"));
    }

    @Test
    void getShouldReturnFinancialFieldsForModel() {
        given()
                .when().get("/api/bus-models/1")
                .then()
                .statusCode(200)
                .body("unitCostUsd", is(notNullValue()))
                .body("maintenanceCostPerKm", is(notNullValue()))
                .body("co2EmissionsGKm", is(notNullValue()));
    }

    @Test
    void getShouldReturnTechnicalFieldsForElectricModel() {
        given()
                .when().get("/api/bus-models/1")
                .then()
                .statusCode(200)
                .body("fuelType", equalTo("ELECTRIC"))
                .body("autonomyKm", is(notNullValue()))
                .body("passengerCapacity", greaterThan(0))
                .body("batteryCapacityKwh", is(notNullValue()))
                .body("energyConsumptionKwhKm", is(notNullValue()));
    }

    @Test
    void getShouldReturnZeroBatteryFieldsForDieselModel() {
        given()
                .when().get("/api/bus-models/4")
                .then()
                .statusCode(200)
                .body("fuelType", equalTo("DIESEL"))
                .body("batteryCapacityKwh", comparesEqualTo(0.0f))
                .body("energyConsumptionKwhKm", comparesEqualTo(0.0f))
                .body("fuelConsumptionLKm", greaterThan(0.0f));
    }

    // ── Prueba 1 — AC1: modificar campos de un modelo registrado ─────────────
    @Test
    void putShouldPersistChangesToNameAutonomyCapacityAndCost() {
        String body = """
                {
                  "name": "Yutong E12PRO Editado",
                  "autonomyKm": 350.00,
                  "passengerCapacity": 90,
                  "unitCostUsd": 450000.00
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().put("/api/bus-models/1")
                .then()
                .statusCode(200)
                .body("model.name", equalTo("Yutong E12PRO Editado"))
                .body("model.autonomyKm", comparesEqualTo(350.00f))
                .body("model.passengerCapacity", equalTo(90))
                .body("model.unitCostUsd", comparesEqualTo(450000.00f));

        // Restaurar para no romper otros tests
        String restore = """
                {
                  "name": "Yutong E12PRO",
                  "autonomyKm": 300.00,
                  "passengerCapacity": 85,
                  "unitCostUsd": 420000.00
                }
                """;
        given().contentType(ContentType.JSON).body(restore).when().put("/api/bus-models/1");
    }

    // ── Prueba 2 — AC2: cambios reflejados inmediatamente en GET ─────────────
    @Test
    void putShouldReflectChangesImmediatelyInGetEndpoints() {
        String update = """
                {
                  "name": "Yutong E12PRO Temp",
                  "autonomyKm": 280.00,
                  "passengerCapacity": 80,
                  "unitCostUsd": 400000.00
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(update)
                .when().put("/api/bus-models/2")
                .then()
                .statusCode(200);

        // GET /{id} refleja el cambio
        given()
                .when().get("/api/bus-models/2")
                .then()
                .statusCode(200)
                .body("name", equalTo("Yutong E12PRO Temp"))
                .body("autonomyKm", comparesEqualTo(280.00f));

        // GET / (listado general) también refleja el cambio
        given()
                .when().get("/api/bus-models")
                .then()
                .statusCode(200)
                .body("name", hasItem("Yutong E12PRO Temp"));

        // Restaurar
        given().contentType(ContentType.JSON)
                .body("{\"name\":\"Yutong ZK5120C\",\"autonomyKm\":130.00,\"passengerCapacity\":85,\"unitCostUsd\":300000.00}")
                .when().put("/api/bus-models/2");
    }

    // ── Prueba 3 — AC3: mensaje de confirmación al guardar correctamente ──────
    @Test
    void putShouldReturnConfirmationMessageOnSuccess() {
        String body = """
                {
                  "name": "Yutong E12PRO Confirm",
                  "autonomyKm": 310.00
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().put("/api/bus-models/3")
                .then()
                .statusCode(200)
                .body("message", equalTo("Modelo actualizado correctamente"))
                .body("model", notNullValue())
                .body("model.id", equalTo(3));

        // Restaurar
        given().contentType(ContentType.JSON)
                .body("{\"name\":\"Yutong ZK5180C\",\"autonomyKm\":120.00}")
                .when().put("/api/bus-models/3");
    }

    @Test
    void putShouldReturn404WhenModelDoesNotExist() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"name\": \"Fantasma\"}")
                .when().put("/api/bus-models/99999")
                .then()
                .statusCode(404);
    }

    // ── HU13 — POST: agregar modelo al catálogo ───────────────────────────────

    // AC1: el administrador puede agregar un registro al catálogo
    @Test
    void postShouldCreateNewBusModel() {
        String body = """
                {
                  "name": "BYD K9M Test",
                  "manufacturer": "BYD",
                  "fuelType": "ELECTRIC",
                  "autonomyKm": 250.00,
                  "passengerCapacity": 75,
                  "unitCostUsd": 380000.00,
                  "batteryCapacityKwh": 324.00,
                  "energyConsumptionKwhKm": 1.20,
                  "fuelConsumptionLKm": 0.00,
                  "maintenanceCostPerKm": 0.10,
                  "co2EmissionsGKm": 0.00
                }
                """;

        int newId = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/api/bus-models")
                .then()
                .statusCode(201)
                .body("message", equalTo("Modelo agregado correctamente"))
                .body("model.name", equalTo("BYD K9M Test"))
                .body("model.manufacturer", equalTo("BYD"))
                .body("model.fuelType", equalTo("ELECTRIC"))
                .body("model.id", notNullValue())
                .extract().jsonPath().getInt("model.id");

        // Limpiar
        given().when().delete("/api/bus-models/" + newId);
    }

    // AC2: el modelo incluye nombre, capacidad, autonomía y costo
    @Test
    void postShouldPersistAllRequiredFields() {
        String body = """
                {
                  "name": "Volvo 7900E Test",
                  "manufacturer": "Volvo",
                  "fuelType": "ELECTRIC",
                  "autonomyKm": 200.00,
                  "passengerCapacity": 68,
                  "unitCostUsd": 410000.00
                }
                """;

        int newId = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/api/bus-models")
                .then()
                .statusCode(201)
                .body("model.autonomyKm", comparesEqualTo(200.00f))
                .body("model.passengerCapacity", equalTo(68))
                .body("model.unitCostUsd", comparesEqualTo(410000.00f))
                .extract().jsonPath().getInt("model.id");

        // Limpiar
        given().when().delete("/api/bus-models/" + newId);
    }

    // AC3: los cambios se reflejan de inmediato en las vistas del CEO (GET list)
    @Test
    void postShouldAppearImmediatelyInList() {
        String body = """
                {
                  "name": "Higer KLQ6129GEV Test",
                  "manufacturer": "Higer",
                  "fuelType": "ELECTRIC",
                  "autonomyKm": 270.00,
                  "passengerCapacity": 80,
                  "unitCostUsd": 360000.00
                }
                """;

        int newId = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/api/bus-models")
                .then()
                .statusCode(201)
                .extract().jsonPath().getInt("model.id");

        given()
                .when().get("/api/bus-models")
                .then()
                .statusCode(200)
                .body("name", hasItem("Higer KLQ6129GEV Test"));

        // Limpiar
        given().when().delete("/api/bus-models/" + newId);
    }

    @Test
    void postShouldReturn409WhenNameAlreadyExists() {
        String body = """
                {
                  "name": "Yutong E12PRO",
                  "manufacturer": "Yutong",
                  "fuelType": "ELECTRIC",
                  "autonomyKm": 300.00,
                  "passengerCapacity": 85,
                  "unitCostUsd": 420000.00
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/api/bus-models")
                .then()
                .statusCode(409);
    }

    @Test
    void postShouldReturn400WhenFuelTypeIsInvalid() {
        String body = """
                {
                  "name": "Modelo Invalido",
                  "manufacturer": "X",
                  "fuelType": "GASOLINA",
                  "autonomyKm": 100.00,
                  "passengerCapacity": 50,
                  "unitCostUsd": 100000.00
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/api/bus-models")
                .then()
                .statusCode(400);
    }

    // ── HU15 — DELETE: eliminar modelo del catálogo ───────────────────────────

    // AC1: el administrador puede eliminar un modelo del catálogo
    @Test
    void deleteShouldRemoveModel() {
        // Crear un modelo temporal para eliminar
        String body = """
                {
                  "name": "Modelo Para Eliminar",
                  "manufacturer": "Test",
                  "fuelType": "DIESEL",
                  "autonomyKm": 400.00,
                  "passengerCapacity": 60,
                  "unitCostUsd": 100000.00
                }
                """;

        int newId = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/api/bus-models")
                .then()
                .statusCode(201)
                .extract().jsonPath().getInt("model.id");

        // AC1: eliminar el modelo
        given()
                .when().delete("/api/bus-models/" + newId)
                .then()
                .statusCode(200)
                .body("message", equalTo("Modelo eliminado correctamente"));
    }

    // AC3: el modelo eliminado deja de aparecer en todas las vistas
    @Test
    void deleteShouldDisappearImmediatelyFromList() {
        String body = """
                {
                  "name": "Modelo Para Eliminar 2",
                  "manufacturer": "Test",
                  "fuelType": "DIESEL",
                  "autonomyKm": 400.00,
                  "passengerCapacity": 60,
                  "unitCostUsd": 100000.00
                }
                """;

        int newId = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/api/bus-models")
                .then()
                .statusCode(201)
                .extract().jsonPath().getInt("model.id");

        given().when().delete("/api/bus-models/" + newId).then().statusCode(200);

        // Ya no aparece en el listado
        given()
                .when().get("/api/bus-models")
                .then()
                .statusCode(200)
                .body("name", not(hasItem("Modelo Para Eliminar 2")));

        // Ya no aparece en GET /{id}
        given()
                .when().get("/api/bus-models/" + newId)
                .then()
                .statusCode(404);
    }

    @Test
    void deleteShouldReturn404WhenModelDoesNotExist() {
        given()
                .when().delete("/api/bus-models/99999")
                .then()
                .statusCode(404);
    }
}
