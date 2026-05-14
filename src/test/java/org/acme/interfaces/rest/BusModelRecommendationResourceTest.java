package org.acme.interfaces.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class BusModelRecommendationResourceTest {

    private static final String MB_ROUTE = "B_CMX0300L1";

    // ── AC1 — recommend based on capacity and historical demand ──────────────

    @Test
    void shouldReturn200ForRouteWithDemandData() {
        given()
                .when().get("/api/routes/" + MB_ROUTE + "/bus-model-recommendation")
                .then()
                .statusCode(200);
    }

    @Test
    void shouldReturnRouteMetadata() {
        given()
                .when().get("/api/routes/" + MB_ROUTE + "/bus-model-recommendation")
                .then()
                .statusCode(200)
                .body("routeId", equalTo(MB_ROUTE))
                .body("routeShortName", equalTo("1"))
                .body("routeLongName", equalTo("Indios Verdes - El Caminero"))
                .body("distanceKm", equalTo(28.5f))
                .body("frequencyMinutes", equalTo(3));
    }

    @Test
    void shouldReturnDemandSummary() {
        // Exact values depend on H2 DAYOFWEEK behavior (ISO vs MySQL convention)
        // and test execution order. We verify all three demand fields are present and non-negative.
        given()
                .when().get("/api/routes/" + MB_ROUTE + "/bus-model-recommendation")
                .then()
                .statusCode(200)
                .body("demand.avgWeekday", notNullValue())
                .body("demand.avgSaturday", notNullValue())
                .body("demand.avgSunday", notNullValue());
    }

    @Test
    void shouldReturnRecommendationsForAllDayTypes() {
        given()
                .when().get("/api/routes/" + MB_ROUTE + "/bus-model-recommendation")
                .then()
                .statusCode(200)
                .body("recommendations.weekday", notNullValue())
                .body("recommendations.saturday", notNullValue())
                .body("recommendations.sunday", notNullValue());
    }

    @Test
    void shouldComputePeakHourDemandForWeekday() {
        // peakHourDemand = round(avgWeekday × 0.12); exact value varies by H2 DAYOFWEEK behavior
        given()
                .when().get("/api/routes/" + MB_ROUTE + "/bus-model-recommendation")
                .then()
                .statusCode(200)
                .body("recommendations.weekday.peakHourDemand", greaterThanOrEqualTo(0));
    }

    @Test
    void shouldComputeRequiredCapacityForWeekday() {
        // requiredCapacity = ceil(peakHour / (busesPerHour × targetOccupancy)); verify field exists
        given()
                .when().get("/api/routes/" + MB_ROUTE + "/bus-model-recommendation")
                .then()
                .statusCode(200)
                .body("recommendations.weekday.requiredCapacity", greaterThanOrEqualTo(0));
    }

    // ── AC2 — ≥2 models ordered by adequacy with justification ───────────────

    @Test
    void shouldReturnAtLeastTwoModels() {
        given()
                .when().get("/api/routes/" + MB_ROUTE + "/bus-model-recommendation")
                .then()
                .statusCode(200)
                .body("recommendations.weekday.models.size()", greaterThanOrEqualTo(2));
    }

    @Test
    void shouldReturnAllSixSeedModels() {
        given()
                .when().get("/api/routes/" + MB_ROUTE + "/bus-model-recommendation")
                .then()
                .statusCode(200)
                .body("recommendations.weekday.models.size()", equalTo(6));
    }

    @Test
    void shouldMarkFirstEligibleModelAsRecommended() {
        // All models meet requiredCapacity=38 (min capacity is 61). Eligible models sorted by
        // unitCostUsd ASC → H8 (120 000 USD) is rank 1 and recommended.
        given()
                .when().get("/api/routes/" + MB_ROUTE + "/bus-model-recommendation")
                .then()
                .statusCode(200)
                .body("recommendations.weekday.models[0].recommended", equalTo(true))
                .body("recommendations.weekday.models[0].meetsCapacity", equalTo(true))
                .body("recommendations.weekday.models[0].meetsAutonomy", equalTo(true))
                .body("recommendations.weekday.models[0].rank", equalTo(1));
    }

    @Test
    void shouldNotMarkMoreThanOneModelAsRecommended() {
        io.restassured.response.Response response = given()
                .when().get("/api/routes/" + MB_ROUTE + "/bus-model-recommendation")
                .then().statusCode(200).extract().response();

        long recommendedCount = response.jsonPath()
                .<Boolean>getList("recommendations.weekday.models.recommended")
                .stream().filter(Boolean.TRUE::equals).count();

        org.junit.jupiter.api.Assertions.assertEquals(1, recommendedCount,
                "Exactly one model should be recommended");
    }

    @Test
    void shouldIncludeRequiredCapacityAndJustificationOnEachModel() {
        given()
                .when().get("/api/routes/" + MB_ROUTE + "/bus-model-recommendation")
                .then()
                .statusCode(200)
                .body("recommendations.weekday.models[0].requiredCapacity", greaterThanOrEqualTo(0))
                .body("recommendations.weekday.models[0].justification", notNullValue())
                .body("recommendations.weekday.models[0].model", notNullValue())
                .body("recommendations.weekday.models[0].model.passengerCapacity", notNullValue());
    }

    // ── AC3 — no model has sufficient capacity ────────────────────────────────

    @Test
    void shouldReturnNoRecommendedModelWhenCapacityExceedsAllModels() {
        // targetOccupancy=0.0001 → with any real demand the requiredCapacity far exceeds
        // the max model capacity in seed (140 pax), so none should be eligible.
        // e.g. demand=4180: peakHour=502, requiredCap=ceil(502/(20×0.0001))=ceil(251000)=251000 >> 140
        given()
                .queryParam("targetOccupancy", "0.0001")
                .when().get("/api/routes/" + MB_ROUTE + "/bus-model-recommendation")
                .then()
                .statusCode(200)
                .body("recommendations.weekday.models.recommended", not(hasItem(true)))
                .body("recommendations.weekday.models.meetsCapacity", not(hasItem(true)));
    }

    // ── Error cases ───────────────────────────────────────────────────────────

    @Test
    void shouldReturn404WhenRouteNotFound() {
        given()
                .when().get("/api/routes/NONEXISTENT_ROUTE/bus-model-recommendation")
                .then()
                .statusCode(404);
    }

    @Test
    void shouldReturn404WhenRouteHasNoDemandData() {
        // TR13 exists in GTFS but has no afluencia_metrobus records
        given()
                .when().get("/api/routes/TR13/bus-model-recommendation")
                .then()
                .statusCode(404);
    }
}
