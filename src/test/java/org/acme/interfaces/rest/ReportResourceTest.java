package org.acme.interfaces.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class ReportResourceTest {

    @Test
    void comparativeShouldReturn200WithValidParams() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 1)
                .queryParam("dieselModelId", 4)
                .queryParam("buses", 10)
                .queryParam("years", 10)
                .when().get("/api/reports/comparative")
                .then()
                .statusCode(200)
                .body("routeId", equalTo("TR13"))
                .body("numberOfBuses", equalTo(10))
                .body("projectionYears", equalTo(10))
                .body("electricCostPerYear", greaterThan(0.0f))
                .body("dieselCostPerYear", greaterThan(0.0f))
                .body("co2AvoidedTonsPerYear", greaterThan(0.0f))
                .body("totalInvestmentMXN", greaterThan(0.0f))
                .body("netAnnualSavings", notNullValue())
                .body("tcoProjection", hasSize(10))
                .body("tcoProjection[0].year", equalTo(1))
                .body("electricModelName", notNullValue())
                .body("dieselModelName", notNullValue());
    }

    @Test
    void comparativeShouldReturn200WithDefaultBusesAndYears() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 1)
                .queryParam("dieselModelId", 4)
                .when().get("/api/reports/comparative")
                .then()
                .statusCode(200)
                .body("numberOfBuses", equalTo(10))
                .body("projectionYears", equalTo(10));
    }

    @Test
    void comparativeShouldReturn404WhenRouteNotFound() {
        given()
                .queryParam("routeId", "NONEXISTENT")
                .queryParam("electricModelId", 1)
                .queryParam("dieselModelId", 4)
                .when().get("/api/reports/comparative")
                .then()
                .statusCode(404);
    }

    @Test
    void comparativeShouldReturn404WhenElectricModelNotFound() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 999)
                .queryParam("dieselModelId", 4)
                .when().get("/api/reports/comparative")
                .then()
                .statusCode(404);
    }

    @Test
    void comparativeShouldReturn400WhenRouteIdMissing() {
        given()
                .queryParam("electricModelId", 1)
                .queryParam("dieselModelId", 4)
                .when().get("/api/reports/comparative")
                .then()
                .statusCode(400)
                .body(containsString("routeId es requerido"));
    }

    @Test
    void comparativeShouldReturn400WhenElectricModelIdMissing() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("dieselModelId", 4)
                .when().get("/api/reports/comparative")
                .then()
                .statusCode(400)
                .body(containsString("electricModelId es requerido"));
    }

    @Test
    void comparativeShouldReturn400WhenDieselModelIdMissing() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 1)
                .when().get("/api/reports/comparative")
                .then()
                .statusCode(400)
                .body(containsString("dieselModelId es requerido"));
    }

    @Test
    void comparativeShouldReturn400WhenBusesIsZero() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 1)
                .queryParam("dieselModelId", 4)
                .queryParam("buses", 0)
                .when().get("/api/reports/comparative")
                .then()
                .statusCode(400)
                .body(containsString("al menos 1"));
    }

    @Test
    void comparativeShouldReturn400WhenYearsOutOfRange() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 1)
                .queryParam("dieselModelId", 4)
                .queryParam("years", 0)
                .when().get("/api/reports/comparative")
                .then()
                .statusCode(400);

        given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 1)
                .queryParam("dieselModelId", 4)
                .queryParam("years", 31)
                .when().get("/api/reports/comparative")
                .then()
                .statusCode(400);
    }

    @Test
    void comparativeShouldReturn400WhenElectricModelIsDiesel() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 4)
                .queryParam("dieselModelId", 4)
                .when().get("/api/reports/comparative")
                .then()
                .statusCode(400)
                .body(containsString("ELECTRIC"));
    }

    @Test
    void comparativeShouldReturn400WhenDieselModelIsElectric() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 1)
                .queryParam("dieselModelId", 1)
                .when().get("/api/reports/comparative")
                .then()
                .statusCode(400)
                .body(containsString("DIESEL"));
    }

    @Test
    void comparativeTcoProjectionShouldHaveCorrectYears() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 1)
                .queryParam("dieselModelId", 4)
                .queryParam("years", 5)
                .when().get("/api/reports/comparative")
                .then()
                .statusCode(200)
                .body("tcoProjection", hasSize(5))
                .body("tcoProjection[4].year", equalTo(5));
    }

    @Test
    void comparativeShouldReturn200BecauseTestFilterInjectsCmo() {
        // TestFirebaseAuthFilter inyecta CMO para rutas /api/reports/*
        given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 1)
                .queryParam("dieselModelId", 4)
                .when().get("/api/reports/comparative")
                .then()
                .statusCode(200);
    }
}
