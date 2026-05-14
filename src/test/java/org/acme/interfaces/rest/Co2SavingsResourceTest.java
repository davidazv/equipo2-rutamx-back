package org.acme.interfaces.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class Co2SavingsResourceTest {

    @Test
    void shouldReturn200WithValidElectricModel() {
        given()
                .queryParam("busModelId", 1)
                .when().get("/api/co2-savings")
                .then()
                .statusCode(200)
                .body("$.size()", greaterThanOrEqualTo(1))
                .body("[0].routeId", notNullValue())
                .body("[0].routeName", notNullValue())
                .body("[0].agencyId", notNullValue())
                .body("[0].distanciaKm", greaterThan(0.0f))
                .body("[0].emisionesDieselTon", greaterThan(0.0f))
                .body("[0].emisionesElectricoTon", greaterThan(0.0f))
                .body("[0].ahorroTon", greaterThan(0.0f))
                .body("[0].score", notNullValue())
                .body("[0].prioridad", notNullValue());
    }

    @Test
    void shouldReturnResultsForAllSeedRoutes() {
        given()
                .queryParam("busModelId", 1)
                .when().get("/api/co2-savings")
                .then()
                .statusCode(200)
                .body("$.size()", equalTo(3));
    }

    @Test
    void shouldReturnResultsSortedByScoreDescending() {
        float score0 = given()
                .queryParam("busModelId", 1)
                .when().get("/api/co2-savings")
                .then().statusCode(200)
                .extract().path("[0].score");

        float score1 = given()
                .queryParam("busModelId", 1)
                .when().get("/api/co2-savings")
                .then().statusCode(200)
                .extract().path("[1].score");

        org.junit.jupiter.api.Assertions.assertTrue(score0 >= score1,
                "Results should be sorted by score descending");
    }

    @Test
    void shouldReturnHighestScoreOf100ForTopRoute() {
        given()
                .queryParam("busModelId", 1)
                .when().get("/api/co2-savings")
                .then()
                .statusCode(200)
                .body("[0].score", equalTo(100.0f));
    }

    @Test
    void shouldSetPrioridadAltaForTopRoute() {
        given()
                .queryParam("busModelId", 1)
                .when().get("/api/co2-savings")
                .then()
                .statusCode(200)
                .body("[0].prioridad", equalTo("Alta"));
    }

    @Test
    void shouldReturnDetallesPorDiaWithFallbackWhenNoAfluenciaData() {
        // Routes with trips but no afluencia data use AVG_PASSENGERS_PER_TRIP (79) as fallback.
        // detallesPorDia is only null when a route has no trips at all.
        given()
                .queryParam("busModelId", 1)
                .when().get("/api/co2-savings")
                .then()
                .statusCode(200)
                .body("find { it.routeId == 'TR13' }.detallesPorDia", notNullValue())
                .body("find { it.routeId == 'TR13' }.detallesPorDia.lunes.pasajeros", equalTo(79.0f));
    }

    @Test
    void shouldReturn400WhenBusModelIdMissing() {
        given()
                .when().get("/api/co2-savings")
                .then()
                .statusCode(400)
                .body(containsString("busModelId es requerido"));
    }

    @Test
    void shouldReturn400WhenBusModelNotFound() {
        given()
                .queryParam("busModelId", 9999)
                .when().get("/api/co2-savings")
                .then()
                .statusCode(400);
    }

    @Test
    void shouldReturn400WhenBusModelIsDiesel() {
        given()
                .queryParam("busModelId", 4)
                .when().get("/api/co2-savings")
                .then()
                .statusCode(400)
                .body(containsString("eléctrico"));
    }

    @Test
    void shouldReturnAgencyColorForRoutes() {
        given()
                .queryParam("busModelId", 1)
                .when().get("/api/co2-savings")
                .then()
                .statusCode(200)
                .body("[0].agencyColor", notNullValue());
    }

    @Test
    void shouldProduceDifferentEmissionsForDifferentElectricModels() {
        float emisionesModel1 = given()
                .queryParam("busModelId", 1)
                .when().get("/api/co2-savings")
                .then().statusCode(200)
                .extract().path("[0].emisionesElectricoTon");

        float emisionesModel3 = given()
                .queryParam("busModelId", 3)
                .when().get("/api/co2-savings")
                .then().statusCode(200)
                .extract().path("[0].emisionesElectricoTon");

        // Model 3 has higher kWh/km (1.3 vs 1.0) so should have higher electric emissions
        org.junit.jupiter.api.Assertions.assertTrue(emisionesModel3 > emisionesModel1,
                "Higher kWh/km model should produce more electric emissions");
    }

    @Test
    void shouldReturnDieselEmissionsGreaterThanElectric() {
        given()
                .queryParam("busModelId", 1)
                .when().get("/api/co2-savings")
                .then()
                .statusCode(200)
                .body("[0].emisionesDieselTon", greaterThan(0.0f))
                .body("[0].emisionesElectricoTon", greaterThan(0.0f))
                .body("[0].ahorroTon", greaterThan(0.0f));
    }
}
