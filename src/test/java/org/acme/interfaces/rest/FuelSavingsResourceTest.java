package org.acme.interfaces.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class FuelSavingsResourceTest {

    @Test
    void shouldReturn200WithValidParams() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("modelId", 1)
                .queryParam("buses", 10)
                .queryParam("years", 5)
                .when().get("/api/fuel-savings")
                .then()
                .statusCode(200)
                .body("routeId", equalTo("TR13"))
                .body("busModelId", equalTo(1))
                .body("numberOfBuses", equalTo(10))
                .body("projectionYears", equalTo(5))
                .body("fuelSavingsMXN", greaterThan(0.0f))
                .body("fuelSavingsLiters", greaterThan(0.0f))
                .body("dieselReferencePriceMXN", equalTo(24.0f))
                .body("dieselConsumptionLKm", greaterThan(0.0f))
                .body("dieselCostPerYear", greaterThan(0.0f))
                .body("electricCostPerYear", notNullValue())
                .body("busModelName", notNullValue())
                .body("routeDistanceKm", greaterThan(0.0f));
    }

    @Test
    void shouldReturn400WhenRouteIdMissing() {
        given()
                .queryParam("modelId", 1)
                .queryParam("buses", 10)
                .when().get("/api/fuel-savings")
                .then()
                .statusCode(400)
                .body(containsString("routeId es requerido"));
    }

    @Test
    void shouldReturn400WhenModelIdMissing() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("buses", 10)
                .when().get("/api/fuel-savings")
                .then()
                .statusCode(400)
                .body(containsString("modelId es requerido"));
    }

    @Test
    void shouldReturn400WhenBusesIsZero() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("modelId", 1)
                .queryParam("buses", 0)
                .when().get("/api/fuel-savings")
                .then()
                .statusCode(400)
                .body(containsString("al menos 1"));
    }

    @Test
    void shouldReturn400WhenYearsOutOfRange() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("modelId", 1)
                .queryParam("buses", 10)
                .queryParam("years", 11)
                .when().get("/api/fuel-savings")
                .then()
                .statusCode(400)
                .body(containsString("entre 1 y 10"));
    }

    @Test
    void shouldReturn404WhenRouteNotFound() {
        given()
                .queryParam("routeId", "NONEXISTENT")
                .queryParam("modelId", 1)
                .queryParam("buses", 10)
                .when().get("/api/fuel-savings")
                .then()
                .statusCode(404);
    }

    @Test
    void shouldReturn404WhenModelNotFound() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("modelId", 999)
                .queryParam("buses", 10)
                .when().get("/api/fuel-savings")
                .then()
                .statusCode(404);
    }

    @Test
    void shouldReturn400WhenModelIsDiesel() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("modelId", 4)
                .queryParam("buses", 10)
                .when().get("/api/fuel-savings")
                .then()
                .statusCode(400);
    }

    @Test
    void shouldUseDefaultsWhenOptionalParamsMissing() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("modelId", 1)
                .when().get("/api/fuel-savings")
                .then()
                .statusCode(200)
                .body("numberOfBuses", equalTo(10))
                .body("projectionYears", equalTo(5));
    }
}
