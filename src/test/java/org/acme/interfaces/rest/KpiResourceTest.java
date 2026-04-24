package org.acme.interfaces.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

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
}
