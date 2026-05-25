package org.acme.interfaces.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class CmoResourceTest {

    // ── GET /api/cmo/route-stats ─────────────────────────────────────────────

    @Test
    void routeStatsShouldReturn200() {
        given()
                .when().get("/api/cmo/route-stats")
                .then()
                .statusCode(200);
    }

    @Test
    void routeStatsShouldReturnNonEmptyList() {
        given()
                .when().get("/api/cmo/route-stats")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    void routeStatsShouldContainRequiredFields() {
        given()
                .when().get("/api/cmo/route-stats")
                .then()
                .statusCode(200)
                .body("[0].routeId", notNullValue())
                .body("[0].routeName", notNullValue())
                .body("[0].agencyId", notNullValue())
                .body("[0].distanciaKm", notNullValue())
                .body("[0].avgDailyPassengers", notNullValue())
                .body("[0].co2AhorradoTonAnio", notNullValue())
                .body("[0].headwayMinutes", notNullValue());
    }

    @Test
    void routeStatsShouldHavePositiveDistances() {
        given()
                .when().get("/api/cmo/route-stats")
                .then()
                .statusCode(200)
                .body("distanciaKm", everyItem(greaterThan(0.0f)));
    }

    @Test
    void routeStatsShouldHaveNonNegativePassengers() {
        given()
                .when().get("/api/cmo/route-stats")
                .then()
                .statusCode(200)
                .body("avgDailyPassengers", everyItem(greaterThanOrEqualTo(0.0f)));
    }

    @Test
    void routeStatsCo2SavingsShouldBePositive() {
        given()
                .when().get("/api/cmo/route-stats")
                .then()
                .statusCode(200)
                .body("co2AhorradoTonAnio", everyItem(greaterThanOrEqualTo(0.0f)));
    }

    @Test
    void routeStatsCo2DieselShouldBeGreaterThanElectric() {
        given()
                .when().get("/api/cmo/route-stats")
                .then()
                .statusCode(200)
                .body("[0].co2DieselTonAnio", greaterThanOrEqualTo(0.0f))
                .body("[0].co2ElectricoTonAnio", greaterThanOrEqualTo(0.0f));
    }

    @Test
    void routeStatsHeadwayShouldBeNonNegative() {
        given()
                .when().get("/api/cmo/route-stats")
                .then()
                .statusCode(200)
                .body("headwayMinutes", everyItem(greaterThanOrEqualTo(0)));
    }

    @Test
    void routeStatsShouldMatchSeedRouteCount() {
        given()
                .when().get("/api/cmo/route-stats")
                .then()
                .statusCode(200)
                .body("size()", equalTo(3));
    }
}
