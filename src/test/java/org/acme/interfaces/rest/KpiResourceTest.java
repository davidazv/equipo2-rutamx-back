package org.acme.interfaces.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class KpiResourceTest {

    @Test
    void summaryShouldReturn200WithDefaultBuses() {
        given()
                .when().get("/api/kpi/summary")
                .then()
                .statusCode(200)
                .body("totalCo2AvoidedTons", notNullValue())
                .body("totalFuelSavingsMXN", notNullValue())
                .body("totalInvestmentMXN", notNullValue())
                .body("totalElectricCostMXN", notNullValue())
                .body("totalDieselCostMXN", notNullValue())
                .body("routesAnalyzed", greaterThanOrEqualTo(0));
    }

    @Test
    void summaryShouldReturn200WithExplicitBuses() {
        given()
                .queryParam("busesPerRoute", 5)
                .when().get("/api/kpi/summary")
                .then()
                .statusCode(200)
                .body("totalCo2AvoidedTons", notNullValue())
                .body("routesAnalyzed", greaterThanOrEqualTo(0));
    }

    @Test
    void summaryShouldReturn400WhenBusesIsZero() {
        given()
                .queryParam("busesPerRoute", 0)
                .when().get("/api/kpi/summary")
                .then()
                .statusCode(400);
    }

    @Test
    void summaryShouldReturn400WhenBusesIsNegative() {
        given()
                .queryParam("busesPerRoute", -1)
                .when().get("/api/kpi/summary")
                .then()
                .statusCode(400);
    }

    @Test
    void summaryShouldReturnCo2GreaterThanZeroWithSeedData() {
        given()
                .queryParam("busesPerRoute", 10)
                .when().get("/api/kpi/summary")
                .then()
                .statusCode(200)
                .body("totalCo2AvoidedTons", greaterThan(0.0f));
    }

    @Test
    void summaryShouldReturnFuelSavingsGreaterThanZero() {
        given()
                .queryParam("busesPerRoute", 10)
                .when().get("/api/kpi/summary")
                .then()
                .statusCode(200)
                .body("totalFuelSavingsMXN", greaterThan(0.0f));
    }

    @Test
    void summaryShouldReturnExpectedRoutesAnalyzedCount() {
        given()
                .when().get("/api/kpi/summary")
                .then()
                .statusCode(200)
                .body("routesAnalyzed", equalTo(2));
    }

    @Test
    void summaryShouldReturnDieselCostGreaterThanElectricCost() {
        io.restassured.response.Response response = given()
                .queryParam("busesPerRoute", 10)
                .when().get("/api/kpi/summary")
                .then()
                .statusCode(200)
                .extract().response();

        float dieselCost = response.jsonPath().getFloat("totalDieselCostMXN");
        float electricCost = response.jsonPath().getFloat("totalElectricCostMXN");
        assertTrue(dieselCost > electricCost,
                "Diesel cost (" + dieselCost + ") should exceed electric cost (" + electricCost + ")");
    }
}
