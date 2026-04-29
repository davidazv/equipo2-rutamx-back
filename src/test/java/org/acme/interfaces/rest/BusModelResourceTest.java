package org.acme.interfaces.rest;

import io.quarkus.test.junit.QuarkusTest;
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
}
