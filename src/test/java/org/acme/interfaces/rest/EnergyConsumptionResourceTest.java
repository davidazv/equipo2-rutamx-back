package org.acme.interfaces.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class EnergyConsumptionResourceTest {

    @Test
    void shouldReturn200WithValidParams() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("busModelId", 1)
                .queryParam("occupancyPercent", 50)
                .when().get("/api/energy-consumption")
                .then()
                .statusCode(200)
                .body("routeId", equalTo("TR13"))
                .body("busModelId", equalTo(1))
                .body("occupancyPercent", equalTo(50))
                .body("estimatedConsumptionKwh", greaterThan(0.0f))
                .body("batteryPercentAfter", greaterThanOrEqualTo(0.0f))
                .body("remainingRangeKm", greaterThanOrEqualTo(0.0f))
                .body("canCompleteRoute", notNullValue())
                .body("routeDistanceKm", greaterThan(0.0f))
                .body("busModelName", notNullValue());
    }

    @Test
    void shouldUseDefaultOccupancyWhenNotProvided() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("busModelId", 1)
                .when().get("/api/energy-consumption")
                .then()
                .statusCode(200)
                .body("occupancyPercent", equalTo(50));
    }

    @Test
    void shouldReturn404WhenRouteNotFound() {
        given()
                .queryParam("routeId", "NONEXISTENT")
                .queryParam("busModelId", 1)
                .queryParam("occupancyPercent", 50)
                .when().get("/api/energy-consumption")
                .then()
                .statusCode(404);
    }

    @Test
    void shouldReturn404WhenBusModelNotFound() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("busModelId", 999)
                .queryParam("occupancyPercent", 50)
                .when().get("/api/energy-consumption")
                .then()
                .statusCode(404);
    }

    @Test
    void shouldReturn400WhenRouteIdMissing() {
        given()
                .queryParam("busModelId", 1)
                .queryParam("occupancyPercent", 50)
                .when().get("/api/energy-consumption")
                .then()
                .statusCode(400)
                .body(containsString("routeId es requerido"));
    }

    @Test
    void shouldReturn400WhenRouteIdEmpty() {
        given()
                .queryParam("routeId", "")
                .queryParam("busModelId", 1)
                .queryParam("occupancyPercent", 50)
                .when().get("/api/energy-consumption")
                .then()
                .statusCode(400)
                .body(containsString("routeId es requerido"));
    }

    @Test
    void shouldReturn400WhenBusModelIdMissing() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("occupancyPercent", 50)
                .when().get("/api/energy-consumption")
                .then()
                .statusCode(400)
                .body(containsString("busModelId es requerido"));
    }

    @Test
    void shouldReturn400WhenOccupancyNegative() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("busModelId", 1)
                .queryParam("occupancyPercent", -1)
                .when().get("/api/energy-consumption")
                .then()
                .statusCode(400)
                .body(containsString("entre 0 y 100"));
    }

    @Test
    void shouldReturn400WhenOccupancyOver100() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("busModelId", 1)
                .queryParam("occupancyPercent", 101)
                .when().get("/api/energy-consumption")
                .then()
                .statusCode(400)
                .body(containsString("entre 0 y 100"));
    }

    @Test
    void shouldReturn400WhenModelIsDiesel() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("busModelId", 4)
                .queryParam("occupancyPercent", 50)
                .when().get("/api/energy-consumption")
                .then()
                .statusCode(400)
                .body(containsString("eléctrico"));
    }

    @Test
    void shouldIncreaseConsumptionWithHigherOccupancy() {
        float lowOccConsumption = given()
                .queryParam("routeId", "TR13")
                .queryParam("busModelId", 1)
                .queryParam("occupancyPercent", 0)
                .when().get("/api/energy-consumption")
                .then().statusCode(200)
                .extract().path("estimatedConsumptionKwh");

        float highOccConsumption = given()
                .queryParam("routeId", "TR13")
                .queryParam("busModelId", 1)
                .queryParam("occupancyPercent", 100)
                .when().get("/api/energy-consumption")
                .then().statusCode(200)
                .extract().path("estimatedConsumptionKwh");

        assertTrue(highOccConsumption > lowOccConsumption,
                "Consumption at 100% occupancy should exceed 0% occupancy");
    }
}
