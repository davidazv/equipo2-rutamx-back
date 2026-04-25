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
    void shouldReturnCorrectCalculationValuesAt50Percent() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("busModelId", 1)
                .queryParam("occupancyPercent", 50)
                .when().get("/api/energy-consumption")
                .then()
                .statusCode(200)
                // TR13 = 20km, model 1 = 1.0 kWh/km, 352.08 kWh, 85 passengers
                // occupancyFactor = 1 + (50/100) * 0.003 * 85 = 1.1275
                // totalFactor = 1.1275 * 1.15 * 1.1 = 1.426225
                // consumption = 20 * 1.0 * 1.426225 = 28.5245 → 28.5
                .body("estimatedConsumptionKwh", equalTo(28.5f))
                .body("canCompleteRoute", equalTo(true))
                .body("routeDistanceKm", equalTo(20.0f))
                .body("busModelName", equalTo("Yutong E12PRO"));
    }

    @Test
    void shouldReturnAllResponseFields() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("busModelId", 1)
                .queryParam("occupancyPercent", 50)
                .when().get("/api/energy-consumption")
                .then()
                .statusCode(200)
                .body("routeId", notNullValue())
                .body("routeDistanceKm", notNullValue())
                .body("busModelId", notNullValue())
                .body("busModelName", notNullValue())
                .body("occupancyPercent", notNullValue())
                .body("estimatedConsumptionKwh", notNullValue())
                .body("batteryPercentAfter", notNullValue())
                .body("remainingRangeKm", notNullValue())
                .body("canCompleteRoute", notNullValue());
    }

    @Test
    void shouldWorkWithDifferentElectricBusModel() {
        // busModelId=2: Yutong ZK5120C, 127.51 kWh, 1.0 kWh/km, 85 passengers
        given()
                .queryParam("routeId", "TR13")
                .queryParam("busModelId", 2)
                .queryParam("occupancyPercent", 50)
                .when().get("/api/energy-consumption")
                .then()
                .statusCode(200)
                .body("busModelId", equalTo(2))
                .body("busModelName", equalTo("Yutong ZK5120C"))
                .body("estimatedConsumptionKwh", greaterThan(0.0f))
                .body("canCompleteRoute", notNullValue());
    }

    @Test
    void shouldWorkWithHighCapacityBusModel() {
        // busModelId=3: Yutong ZK5180C, 155.33 kWh, 1.3 kWh/km, 140 passengers
        given()
                .queryParam("routeId", "TR13")
                .queryParam("busModelId", 3)
                .queryParam("occupancyPercent", 75)
                .when().get("/api/energy-consumption")
                .then()
                .statusCode(200)
                .body("busModelId", equalTo(3))
                .body("estimatedConsumptionKwh", greaterThan(0.0f));
    }

    @Test
    void shouldReturnCanCompleteAtZeroOccupancy() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("busModelId", 1)
                .queryParam("occupancyPercent", 0)
                .when().get("/api/energy-consumption")
                .then()
                .statusCode(200)
                .body("occupancyPercent", equalTo(0))
                .body("canCompleteRoute", equalTo(true));
    }

    @Test
    void shouldReturnCanCompleteAt100Occupancy() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("busModelId", 1)
                .queryParam("occupancyPercent", 100)
                .when().get("/api/energy-consumption")
                .then()
                .statusCode(200)
                .body("occupancyPercent", equalTo(100))
                .body("canCompleteRoute", equalTo(true));
    }

    @Test
    void shouldWorkWithTestRoute() {
        // TEST_ROUTE = 15km
        given()
                .queryParam("routeId", "TEST_ROUTE")
                .queryParam("busModelId", 1)
                .queryParam("occupancyPercent", 50)
                .when().get("/api/energy-consumption")
                .then()
                .statusCode(200)
                .body("routeId", equalTo("TEST_ROUTE"))
                .body("routeDistanceKm", equalTo(15.0f))
                .body("estimatedConsumptionKwh", greaterThan(0.0f));
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
