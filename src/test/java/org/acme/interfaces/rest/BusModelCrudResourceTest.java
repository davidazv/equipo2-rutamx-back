package org.acme.interfaces.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.*;

/**
 * Verifica el ciclo CRUD completo de modelos de autobús (HU13, HU14, HU15):
 * crear (POST), editar (PUT) y eliminar (DELETE), incluyendo los casos de
 * error que el frontend espera (409 duplicado, 404 no encontrado).
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BusModelCrudResourceTest {

    private static final String UNIQUE_NAME = "Test CRUD E2E Model";

    private String newModelBody(String name) {
        return """
            {
              "name": "%s",
              "manufacturer": "TestMaker",
              "fuelType": "ELECTRIC",
              "autonomyKm": 320,
              "passengerCapacity": 90,
              "unitCostUsd": 410000,
              "batteryCapacityKwh": 350.5,
              "energyConsumptionKwhKm": 1.2,
              "fuelConsumptionLKm": null,
              "maintenanceCostPerKm": 0.15,
              "co2EmissionsGKm": 0
            }
            """.formatted(name);
    }

    @Test
    @Order(1)
    void postShouldCreateBusModel() {
        Integer id = given()
                .contentType(JSON)
                .body(newModelBody(UNIQUE_NAME))
                .when().post("/api/bus-models")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo(UNIQUE_NAME))
                .body("manufacturer", equalTo("TestMaker"))
                .body("fuelType", equalTo("ELECTRIC"))
                .body("passengerCapacity", equalTo(90))
                .extract().jsonPath().getInt("id");

        // El modelo recién creado debe poder leerse de vuelta.
        given()
                .when().get("/api/bus-models/" + id)
                .then()
                .statusCode(200)
                .body("name", equalTo(UNIQUE_NAME));
    }

    @Test
    @Order(2)
    void postDuplicateNameShouldReturn409() {
        // El nombre es UNIQUE: reusar el del seed dispara conflicto.
        given()
                .contentType(JSON)
                .body(newModelBody("Yutong E12PRO"))
                .when().post("/api/bus-models")
                .then()
                .statusCode(409)
                .body(containsString("MODEL_ALREADY_EXISTS"));
    }

    @Test
    @Order(3)
    void putShouldUpdateBusModel() {
        int id = given()
                .when().get("/api/bus-models")
                .then().statusCode(200)
                .extract().jsonPath()
                .getInt("find { it.name == '%s' }.id".formatted(UNIQUE_NAME));

        String updated = """
            {
              "name": "%s",
              "manufacturer": "TestMaker Updated",
              "fuelType": "ELECTRIC",
              "autonomyKm": 999,
              "passengerCapacity": 120,
              "unitCostUsd": 500000,
              "batteryCapacityKwh": 400,
              "energyConsumptionKwhKm": 1.1,
              "fuelConsumptionLKm": null,
              "maintenanceCostPerKm": 0.2,
              "co2EmissionsGKm": 0
            }
            """.formatted(UNIQUE_NAME);

        given()
                .contentType(JSON)
                .body(updated)
                .when().put("/api/bus-models/" + id)
                .then()
                .statusCode(200)
                .body("manufacturer", equalTo("TestMaker Updated"))
                .body("autonomyKm", notNullValue())
                .body("autonomyKm.toString()", anyOf(is("999"), is("999.0"), is("999.00")))
                .body("passengerCapacity", equalTo(120));
    }

    @Test
    @Order(4)
    void putNonExistentShouldReturn404() {
        given()
                .contentType(JSON)
                .body(newModelBody("Ghost Model"))
                .when().put("/api/bus-models/99999")
                .then()
                .statusCode(404)
                .body(containsString("MODEL_NOT_FOUND"));
    }

    @Test
    @Order(5)
    void deleteShouldRemoveBusModel() {
        int id = given()
                .when().get("/api/bus-models")
                .then().statusCode(200)
                .extract().jsonPath()
                .getInt("find { it.name == '%s' }.id".formatted(UNIQUE_NAME));

        given()
                .when().delete("/api/bus-models/" + id)
                .then()
                .statusCode(204);

        // Ya no debe existir.
        given()
                .when().get("/api/bus-models/" + id)
                .then()
                .statusCode(404);
    }

    @Test
    @Order(6)
    void deleteNonExistentShouldReturn404() {
        given()
                .when().delete("/api/bus-models/99999")
                .then()
                .statusCode(404)
                .body(containsString("MODEL_NOT_FOUND"));
    }
}
