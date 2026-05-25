package org.acme.interfaces.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class CooKpiResourceTest {

    // ── GET /api/kpi/operational-summary ────────────────────────────────────

    @Test
    void operationalSummaryShouldReturn200() {
        given()
                .when().get("/api/kpi/operational-summary")
                .then()
                .statusCode(200)
                .body("totalRoutes", greaterThanOrEqualTo(0))
                .body("avgDailyPassengers", greaterThanOrEqualTo(0.0f))
                .body("peakHour", notNullValue());
    }

    @Test
    void operationalSummaryShouldReturnExpectedRouteCount() {
        given()
                .when().get("/api/kpi/operational-summary")
                .then()
                .statusCode(200)
                .body("totalRoutes", equalTo(3));
    }

    @Test
    void operationalSummaryShouldReturnPositivePassengers() {
        given()
                .when().get("/api/kpi/operational-summary")
                .then()
                .statusCode(200)
                .body("avgDailyPassengers", greaterThan(0.0f));
    }

    @Test
    void operationalSummaryShouldReturnValidPeakHourFormat() {
        String peakHour = given()
                .when().get("/api/kpi/operational-summary")
                .then()
                .statusCode(200)
                .extract().path("peakHour");

        // Expected format: HH:00
        assert peakHour != null && peakHour.matches("\\d{2}:00")
                : "peakHour should match HH:00 format but was: " + peakHour;
    }

    // ── GET /api/kpi/passenger-trend ─────────────────────────────────────────

    @Test
    void passengerTrendShouldReturn200WithSevenPoints() {
        given()
                .when().get("/api/kpi/passenger-trend")
                .then()
                .statusCode(200)
                .body("size()", equalTo(7))
                .body("[0].day", notNullValue())
                .body("[0].avgPassengers", notNullValue());
    }

    @Test
    void passengerTrendShouldStartWithMonday() {
        given()
                .when().get("/api/kpi/passenger-trend")
                .then()
                .statusCode(200)
                .body("[0].day", equalTo("Lun"));
    }

    @Test
    void passengerTrendShouldEndWithSunday() {
        given()
                .when().get("/api/kpi/passenger-trend")
                .then()
                .statusCode(200)
                .body("[6].day", equalTo("Dom"));
    }

    @Test
    void passengerTrendShouldHaveNonNegativeValues() {
        given()
                .when().get("/api/kpi/passenger-trend")
                .then()
                .statusCode(200)
                .body("avgPassengers", everyItem(greaterThanOrEqualTo(0.0f)));
    }

    // ── GET /api/kpi/hourly-stats ────────────────────────────────────────────

    @Test
    void hourlyStatsShouldReturn200WithBothLists() {
        given()
                .when().get("/api/kpi/hourly-stats")
                .then()
                .statusCode(200)
                .body("occupancyByHour", notNullValue())
                .body("busDemand", notNullValue());
    }

    @Test
    void hourlyStatsShouldHaveNonEmptyLists() {
        given()
                .when().get("/api/kpi/hourly-stats")
                .then()
                .statusCode(200)
                .body("occupancyByHour.size()", greaterThan(0))
                .body("busDemand.size()", greaterThan(0));
    }

    @Test
    void hourlyStatsOccupancyShouldBeWithinValidRange() {
        given()
                .when().get("/api/kpi/hourly-stats")
                .then()
                .statusCode(200)
                .body("occupancyByHour.occupancyPct", everyItem(allOf(
                        greaterThanOrEqualTo(0.0f),
                        lessThanOrEqualTo(100.0f)
                )));
    }

    @Test
    void hourlyStatsBusesShouldBePositive() {
        given()
                .when().get("/api/kpi/hourly-stats")
                .then()
                .statusCode(200)
                .body("busDemand.busesRequired", everyItem(greaterThan(0)));
    }

    @Test
    void hourlyStatsHoursShouldBeValid() {
        given()
                .when().get("/api/kpi/hourly-stats")
                .then()
                .statusCode(200)
                .body("busDemand.hour", everyItem(allOf(
                        greaterThanOrEqualTo(0),
                        lessThan(24)
                )));
    }
}
