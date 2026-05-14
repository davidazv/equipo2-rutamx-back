package org.acme.interfaces.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class FleetResourceTest {

    // ── bus-count tests ──────────────────────────────────────────────────────

    @Test
    void busCountShouldReturn200WithValidWeekdayParams() {
        given()
                .queryParam("linea", "linea 1")
                .queryParam("dayType", "weekday")
                .queryParam("occupancy", 80)
                .when().get("/api/fleet/bus-count")
                .then()
                .statusCode(200)
                .body("linea", equalTo("linea 1"))
                .body("dayType", equalTo("weekday"))
                .body("avgDailyDemand", greaterThan(0.0f))
                .body("peakHourDemand", greaterThan(0.0f))
                .body("recommendedBuses", greaterThan(0))
                .body("targetOccupancy", greaterThan(0.0f));
    }

    @Test
    void busCountShouldReturn200WithSaturday() {
        given()
                .queryParam("linea", "linea 1")
                .queryParam("dayType", "saturday")
                .queryParam("occupancy", 80)
                .when().get("/api/fleet/bus-count")
                .then()
                .statusCode(200)
                .body("dayType", equalTo("saturday"))
                .body("avgDailyDemand", greaterThan(0.0f));
    }

    @Test
    void busCountShouldReturn200WithSunday() {
        given()
                .queryParam("linea", "linea 1")
                .queryParam("dayType", "sunday")
                .queryParam("occupancy", 80)
                .when().get("/api/fleet/bus-count")
                .then()
                .statusCode(200)
                .body("dayType", equalTo("sunday"))
                .body("avgDailyDemand", greaterThan(0.0f));
    }

    @Test
    void busCountShouldReturn200WithCustomOccupancy() {
        given()
                .queryParam("linea", "linea 1")
                .queryParam("dayType", "weekday")
                .queryParam("occupancy", 95)
                .when().get("/api/fleet/bus-count")
                .then()
                .statusCode(200)
                .body("targetOccupancy", greaterThan(0.0f));
    }

    @Test
    void busCountShouldReturn400WhenLineaMissing() {
        given()
                .queryParam("dayType", "weekday")
                .queryParam("occupancy", 80)
                .when().get("/api/fleet/bus-count")
                .then()
                .statusCode(400);
    }

    @Test
    void busCountShouldReturn400WhenLineaEmpty() {
        given()
                .queryParam("linea", "")
                .queryParam("dayType", "weekday")
                .queryParam("occupancy", 80)
                .when().get("/api/fleet/bus-count")
                .then()
                .statusCode(400);
    }

    @Test
    void busCountShouldReturn400WhenOccupancyBelowRange() {
        given()
                .queryParam("linea", "linea 1")
                .queryParam("dayType", "weekday")
                .queryParam("occupancy", 59)
                .when().get("/api/fleet/bus-count")
                .then()
                .statusCode(400);
    }

    @Test
    void busCountShouldReturn400WhenOccupancyAboveRange() {
        given()
                .queryParam("linea", "linea 1")
                .queryParam("dayType", "weekday")
                .queryParam("occupancy", 96)
                .when().get("/api/fleet/bus-count")
                .then()
                .statusCode(400);
    }

    @Test
    void busCountShouldReturn400WhenDayTypeInvalid() {
        given()
                .queryParam("linea", "linea 1")
                .queryParam("dayType", "holiday")
                .queryParam("occupancy", 80)
                .when().get("/api/fleet/bus-count")
                .then()
                .statusCode(400);
    }

    @Test
    void busCountShouldReturn404WhenNoDataForLinea() {
        given()
                .queryParam("linea", "linea 99")
                .queryParam("dayType", "weekday")
                .queryParam("occupancy", 80)
                .when().get("/api/fleet/bus-count")
                .then()
                .statusCode(404);
    }

    // ── model-recommendation tests ───────────────────────────────────────────

    @Test
    void modelRecommendationShouldReturn200WithValidParams() {
        given()
                .queryParam("routeId", "B_CMX0300L1")
                .queryParam("occupancy", 80)
                .when().get("/api/fleet/model-recommendation")
                .then()
                .statusCode(200)
                .body("recommendations.weekday.requiredCapacity", greaterThanOrEqualTo(0))
                .body("recommendations.weekday.models", not(empty()));
    }

    @Test
    void modelRecommendationShouldMarkOneAsRecommended() {
        given()
                .queryParam("routeId", "B_CMX0300L1")
                .queryParam("occupancy", 80)
                .when().get("/api/fleet/model-recommendation")
                .then()
                .statusCode(200)
                .body("recommendations.weekday.models.findAll { it.recommended }.size()", equalTo(1));
    }

    @Test
    void modelRecommendationShouldReturn400WhenRouteIdMissing() {
        given()
                .queryParam("occupancy", 80)
                .when().get("/api/fleet/model-recommendation")
                .then()
                .statusCode(400);
    }

    @Test
    void modelRecommendationShouldReturn404WhenNoDemandData() {
        given()
                .queryParam("routeId", "TR13")
                .queryParam("occupancy", 80)
                .when().get("/api/fleet/model-recommendation")
                .then()
                .statusCode(404);
    }

    @Test
    void modelRecommendationShouldReturnAllFields() {
        given()
                .queryParam("routeId", "B_CMX0300L1")
                .queryParam("occupancy", 80)
                .when().get("/api/fleet/model-recommendation")
                .then()
                .statusCode(200)
                .body("routeId", equalTo("B_CMX0300L1"))
                .body("recommendations.weekday.models[0].rank", notNullValue())
                .body("recommendations.weekday.models[0].model", notNullValue())
                .body("recommendations.weekday.models[0].model.passengerCapacity", notNullValue())
                .body("recommendations.weekday.models[0].meetsCapacity", notNullValue())
                .body("recommendations.weekday.models[0].recommended", notNullValue());
    }

    @Test
    void modelRecommendationShouldReturn200WithCustomOccupancy() {
        given()
                .queryParam("routeId", "B_CMX0300L1")
                .queryParam("occupancy", 60)
                .when().get("/api/fleet/model-recommendation")
                .then()
                .statusCode(200);
    }
}
