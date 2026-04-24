package org.acme.interfaces.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class RoiResourceTest {

    @Test
    void estimateShouldReturn200WithValidParams() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("modelId", 1)
                .queryParam("buses", 10)
                .when().get("/api/roi/estimate")
                .then()
                .statusCode(200)
                .body("roiPercent", notNullValue())
                .body("paybackYears", greaterThan(0.0f))
                .body("netAnnualReturn", greaterThan(0.0f))
                .body("totalInvestmentMXN", greaterThan(0.0f))
                .body("co2AvoidedTons", greaterThan(0.0f))
                .body("electricCostPerYear", notNullValue())
                .body("dieselCostPerYear", notNullValue());
    }

    @Test
    void estimateShouldReturn404WhenRouteNotFound() {
        given()
                .queryParam("routeId", "NONEXISTENT")
                .queryParam("modelId", 1)
                .queryParam("buses", 10)
                .when().get("/api/roi/estimate")
                .then()
                .statusCode(404);
    }

    @Test
    void estimateShouldReturn404WhenModelNotFound() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("modelId", 999)
                .queryParam("buses", 10)
                .when().get("/api/roi/estimate")
                .then()
                .statusCode(404);
    }

    @Test
    void estimateShouldReturn400WhenRouteIdMissing() {
        given()
                .queryParam("modelId", 1)
                .queryParam("buses", 10)
                .when().get("/api/roi/estimate")
                .then()
                .statusCode(400);
    }

    @Test
    void estimateShouldReturn400WhenBusesIsZero() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("modelId", 1)
                .queryParam("buses", 0)
                .when().get("/api/roi/estimate")
                .then()
                .statusCode(400);
    }

    @Test
    void estimateShouldReturn400WhenModelIsDiesel() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("modelId", 4)
                .queryParam("buses", 10)
                .when().get("/api/roi/estimate")
                .then()
                .statusCode(400);
    }
}
