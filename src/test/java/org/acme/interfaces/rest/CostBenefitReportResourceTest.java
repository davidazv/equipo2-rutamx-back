package org.acme.interfaces.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class CostBenefitReportResourceTest {

    // AC1: el sistema genera la gráfica de líneas con evolución de costos acumulados
    @Test
    void shouldReturn200WithValidParams() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 1)
                .queryParam("dieselModelId", 4)
                .queryParam("buses", 10)
                .queryParam("years", 10)
                .when().get("/api/reports/cost-benefit")
                .then()
                .statusCode(200)
                .body("routeId", equalTo("TR13"))
                .body("numberOfBuses", equalTo(10))
                .body("routeDistanceKm", greaterThan(0.0f))
                .body("totalInvestmentMXN", greaterThan(0.0f))
                .body("paybackYears", greaterThan(0.0f));
    }

    // AC2: el eje horizontal representa el tiempo (años)
    @Test
    void shouldReturnCorrectNumberOfPoints() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 1)
                .queryParam("dieselModelId", 4)
                .queryParam("buses", 10)
                .queryParam("years", 10)
                .when().get("/api/reports/cost-benefit")
                .then()
                .statusCode(200)
                .body("points.size()", equalTo(10))
                .body("points[0].year", equalTo(1))
                .body("points[9].year", equalTo(10));
    }

    // AC2: el eje vertical representa el costo acumulado en pesos
    @Test
    void shouldReturnCumulativeCostsPerPoint() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 1)
                .queryParam("dieselModelId", 4)
                .queryParam("buses", 10)
                .queryParam("years", 10)
                .when().get("/api/reports/cost-benefit")
                .then()
                .statusCode(200)
                .body("points[0].electricCumulativeMXN", greaterThan(0.0f))
                .body("points[0].dieselCumulativeMXN", greaterThan(0.0f))
                .body("points[0].electricCumulativeMXN", greaterThan(
                        given()
                                .queryParam("routeId", "TR13")
                                .queryParam("electricModelId", 1)
                                .queryParam("dieselModelId", 4)
                                .queryParam("buses", 10)
                                .queryParam("years", 10)
                                .when().get("/api/reports/cost-benefit")
                                .then().extract().path("points[0].dieselCumulativeMXN")
                ));
    }

    // AC3: el punto de cruce se resalta visualmente
    @Test
    void shouldMarkBreakEvenPoint() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 1)
                .queryParam("dieselModelId", 4)
                .queryParam("buses", 10)
                .queryParam("years", 30)
                .when().get("/api/reports/cost-benefit")
                .then()
                .statusCode(200)
                .body("points.findAll { it.breakEvenYear == true }.size()", equalTo(1));
    }

    @Test
    void shouldReturnAllModelNames() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 1)
                .queryParam("dieselModelId", 4)
                .when().get("/api/reports/cost-benefit")
                .then()
                .statusCode(200)
                .body("electricModelName", equalTo("Yutong E12PRO"))
                .body("dieselModelName", equalTo("Yutong DMT Hybrid H8"));
    }

    @Test
    void shouldUseDefaultYearsWhenNotProvided() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 1)
                .queryParam("dieselModelId", 4)
                .when().get("/api/reports/cost-benefit")
                .then()
                .statusCode(200)
                .body("points.size()", equalTo(10));
    }

    @Test
    void shouldUseDefaultBusesWhenNotProvided() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 1)
                .queryParam("dieselModelId", 4)
                .when().get("/api/reports/cost-benefit")
                .then()
                .statusCode(200)
                .body("numberOfBuses", equalTo(10));
    }

    @Test
    void shouldWorkWithDifferentModels() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 2)
                .queryParam("dieselModelId", 5)
                .queryParam("buses", 5)
                .queryParam("years", 15)
                .when().get("/api/reports/cost-benefit")
                .then()
                .statusCode(200)
                .body("numberOfBuses", equalTo(5))
                .body("points.size()", equalTo(15));
    }

    @Test
    void shouldReturn404WhenRouteNotFound() {
        given()
                .queryParam("routeId", "NONEXISTENT")
                .queryParam("electricModelId", 1)
                .queryParam("dieselModelId", 4)
                .when().get("/api/reports/cost-benefit")
                .then()
                .statusCode(404);
    }

    @Test
    void shouldReturn404WhenElectricModelNotFound() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 999)
                .queryParam("dieselModelId", 4)
                .when().get("/api/reports/cost-benefit")
                .then()
                .statusCode(404);
    }

    @Test
    void shouldReturn404WhenDieselModelNotFound() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 1)
                .queryParam("dieselModelId", 999)
                .when().get("/api/reports/cost-benefit")
                .then()
                .statusCode(404);
    }

    @Test
    void shouldReturn400WhenRouteIdMissing() {
        given()
                .queryParam("electricModelId", 1)
                .queryParam("dieselModelId", 4)
                .when().get("/api/reports/cost-benefit")
                .then()
                .statusCode(400)
                .body(containsString("routeId es requerido"));
    }

    @Test
    void shouldReturn400WhenElectricModelIdMissing() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("dieselModelId", 4)
                .when().get("/api/reports/cost-benefit")
                .then()
                .statusCode(400)
                .body(containsString("electricModelId es requerido"));
    }

    @Test
    void shouldReturn400WhenDieselModelIdMissing() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 1)
                .when().get("/api/reports/cost-benefit")
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
                .when().get("/api/reports/cost-benefit")
                .then()
                .statusCode(400)
                .body(containsString("al menos 1"));
    }

    @Test
    void shouldReturn400WhenYearsExceeds30() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 1)
                .queryParam("dieselModelId", 4)
                .queryParam("years", 31)
                .when().get("/api/reports/cost-benefit")
                .then()
                .statusCode(400)
                .body(containsString("1 y 30"));
    }

    @Test
    void shouldReturn400WhenElectricModelIsActuallyDiesel() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("electricModelId", 4)
                .queryParam("dieselModelId", 4)
                .when().get("/api/reports/cost-benefit")
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
                .when().get("/api/reports/cost-benefit")
                .then()
                .statusCode(400)
                .body(containsString("DIESEL"));
    }
}
