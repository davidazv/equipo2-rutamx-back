package org.acme.interfaces.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class ComparativeReportResourceTest {

    // AC1: el sistema genera el reporte al seleccionar los parámetros de ruta
    @Test
    void shouldReturn200WithValidParams() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 1)
                .queryParam("dieselModelId", 4)
                .queryParam("buses", 10)
                .when().get("/api/reports/comparative")
                .then()
                .statusCode(200)
                .body("routeId", equalTo("TR13"))
                .body("numberOfBuses", equalTo(10))
                .body("routeDistanceKm", greaterThan(0.0f));
    }

    // AC2: el reporte incluye datos comparativos entre eléctrico y diésel
    @Test
    void shouldReturnAllComparativeFields() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 1)
                .queryParam("dieselModelId", 4)
                .queryParam("buses", 10)
                .when().get("/api/reports/comparative")
                .then()
                .statusCode(200)
                .body("electricModelName", equalTo("Yutong E12PRO"))
                .body("dieselModelName", equalTo("Yutong DMT Hybrid H8"))
                .body("electricCostPerYear", greaterThan(0.0f))
                .body("electricMaintenanceCostPerYear", greaterThan(0.0f))
                .body("electricTotalCostPerYear", greaterThan(0.0f))
                .body("electricCo2TonsPerYear", notNullValue())
                .body("dieselCostPerYear", greaterThan(0.0f))
                .body("dieselMaintenanceCostPerYear", greaterThan(0.0f))
                .body("dieselTotalCostPerYear", greaterThan(0.0f))
                .body("dieselCo2TonsPerYear", greaterThan(0.0f))
                .body("annualSavingsMXN", greaterThan(0.0f))
                .body("co2AvoidedTonsPerYear", greaterThan(0.0f))
                .body("savingsPercent", greaterThan(0.0f));
    }

    @Test
    void electricShouldCostLessThanDiesel() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 1)
                .queryParam("dieselModelId", 4)
                .queryParam("buses", 10)
                .when().get("/api/reports/comparative")
                .then()
                .statusCode(200)
                .body("electricTotalCostPerYear", lessThan(
                        given()
                                .queryParam("routeId", "TR13")
                                .queryParam("electricModelId", 1)
                                .queryParam("dieselModelId", 4)
                                .queryParam("buses", 10)
                                .when().get("/api/reports/comparative")
                                .then().extract().path("dieselTotalCostPerYear")
                ));
    }

    @Test
    void shouldUseDefaultBusesWhenNotProvided() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 1)
                .queryParam("dieselModelId", 4)
                .when().get("/api/reports/comparative")
                .then()
                .statusCode(200)
                .body("numberOfBuses", equalTo(10));
    }

    @Test
    void shouldWorkWithDifferentElectricModel() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 2)
                .queryParam("dieselModelId", 4)
                .queryParam("buses", 5)
                .when().get("/api/reports/comparative")
                .then()
                .statusCode(200)
                .body("electricModelName", equalTo("Yutong ZK5120C"))
                .body("numberOfBuses", equalTo(5));
    }

    @Test
    void shouldWorkWithDifferentDieselModel() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 1)
                .queryParam("dieselModelId", 5)
                .queryParam("buses", 10)
                .when().get("/api/reports/comparative")
                .then()
                .statusCode(200)
                .body("dieselModelName", equalTo("Yutong DMT Hybrid H10"))
                .body("annualSavingsMXN", notNullValue());
    }

    @Test
    void shouldReturn404WhenRouteNotFound() {
        given()
                .queryParam("routeId", "NONEXISTENT")
                .queryParam("electricModelId", 1)
                .queryParam("dieselModelId", 4)
                .queryParam("buses", 10)
                .when().get("/api/reports/comparative")
                .then()
                .statusCode(404);
    }

    @Test
    void shouldReturn404WhenElectricModelNotFound() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 999)
                .queryParam("dieselModelId", 4)
                .queryParam("buses", 10)
                .when().get("/api/reports/comparative")
                .then()
                .statusCode(404);
    }

    @Test
    void shouldReturn404WhenDieselModelNotFound() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 1)
                .queryParam("dieselModelId", 999)
                .queryParam("buses", 10)
                .when().get("/api/reports/comparative")
                .then()
                .statusCode(404);
    }

    @Test
    void shouldReturn400WhenRouteIdMissing() {
        given()
                .queryParam("electricModelId", 1)
                .queryParam("dieselModelId", 4)
                .queryParam("buses", 10)
                .when().get("/api/reports/comparative")
                .then()
                .statusCode(400)
                .body(containsString("routeId es requerido"));
    }

    @Test
    void shouldReturn400WhenRouteIdEmpty() {
        given()
                .queryParam("routeId", "")
                .queryParam("electricModelId", 1)
                .queryParam("dieselModelId", 4)
                .queryParam("buses", 10)
                .when().get("/api/reports/comparative")
                .then()
                .statusCode(400)
                .body(containsString("routeId es requerido"));
    }

    @Test
    void shouldReturn400WhenElectricModelIdMissing() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("dieselModelId", 4)
                .queryParam("buses", 10)
                .when().get("/api/reports/comparative")
                .then()
                .statusCode(400)
                .body(containsString("electricModelId es requerido"));
    }

    @Test
    void shouldReturn400WhenDieselModelIdMissing() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 1)
                .queryParam("buses", 10)
                .when().get("/api/reports/comparative")
                .then()
                .statusCode(400)
                .body(containsString("dieselModelId es requerido"));
    }

    @Test
    void shouldReturn400WhenBusesIsZero() {
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
    void shouldReturn400WhenElectricModelIsActuallyDiesel() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 4)
                .queryParam("dieselModelId", 4)
                .queryParam("buses", 10)
                .when().get("/api/reports/comparative")
                .then()
                .statusCode(400)
                .body(containsString("ELECTRIC"));
    }

    @Test
    void shouldReturn400WhenDieselModelIsActuallyElectric() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 1)
                .queryParam("dieselModelId", 1)
                .queryParam("buses", 10)
                .when().get("/api/reports/comparative")
                .then()
                .statusCode(400)
                .body(containsString("DIESEL"));
    }

    @Test
    void savingsPercentShouldBeBetween0And100() {
        float savingsPercent = given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 1)
                .queryParam("dieselModelId", 4)
                .queryParam("buses", 10)
                .when().get("/api/reports/comparative")
                .then()
                .statusCode(200)
                .extract().path("savingsPercent");

        assert savingsPercent >= 0 && savingsPercent <= 100 :
                "savingsPercent debe estar entre 0 y 100, pero fue " + savingsPercent;
    }
}
