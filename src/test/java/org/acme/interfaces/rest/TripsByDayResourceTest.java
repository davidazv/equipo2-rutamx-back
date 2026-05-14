package org.acme.interfaces.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class TripsByDayResourceTest {

    @Test
    void shouldReturn200AndListOfRoutes() {
        given()
                .when().get("/api/routes/trips-by-day")
                .then()
                .statusCode(200)
                .body("$.size()", greaterThanOrEqualTo(1));
    }

    @Test
    void shouldReturnAllSeedRoutes() {
        given()
                .when().get("/api/routes/trips-by-day")
                .then()
                .statusCode(200)
                .body("$.size()", equalTo(3));
    }

    @Test
    void shouldReturnExpectedFields() {
        given()
                .when().get("/api/routes/trips-by-day")
                .then()
                .statusCode(200)
                .body("[0].routeId", notNullValue())
                .body("[0].routeName", notNullValue())
                .body("[0].agencyColor", notNullValue())
                .body("[0].monday", notNullValue())
                .body("[0].tuesday", notNullValue())
                .body("[0].wednesday", notNullValue())
                .body("[0].thursday", notNullValue())
                .body("[0].friday", notNullValue())
                .body("[0].saturday", notNullValue())
                .body("[0].sunday", notNullValue())
                .body("[0].totalSemanal", notNullValue())
                .body("[0].calidadDatos", notNullValue());
    }

    @Test
    void shouldSetCalidadBajaWhenNoAfluenciaInTestDb() {
        given()
                .when().get("/api/routes/trips-by-day")
                .then()
                .statusCode(200)
                .body("find { it.routeId == 'TEST_ROUTE' }.calidadDatos", equalTo("Baja"))
                .body("find { it.routeId == 'TEST_ROUTE' }.demandaDiariaPromedio", nullValue());
    }

    @Test
    void shouldComputeCorrectTotalSemanalForSeedData() {
        // Each seed route has 1 trip with calendar: all days = 1
        // totalSemanal = 1+1+1+1+1+1+1 = 7
        given()
                .when().get("/api/routes/trips-by-day")
                .then()
                .statusCode(200)
                .body("[0].totalSemanal", equalTo(7));
    }

    @Test
    void shouldReturnOneTripPerDayForSeedCalendar() {
        // TR13_SERVICE and B_0 both have all days = 1, one trip each
        given()
                .when().get("/api/routes/trips-by-day")
                .then()
                .statusCode(200)
                .body("[0].monday", equalTo(1))
                .body("[0].tuesday", equalTo(1))
                .body("[0].wednesday", equalTo(1))
                .body("[0].thursday", equalTo(1))
                .body("[0].friday", equalTo(1))
                .body("[0].saturday", equalTo(1))
                .body("[0].sunday", equalTo(1));
    }

    @Test
    void shouldReturnAgencyColorForTr13() {
        given()
                .when().get("/api/routes/trips-by-day")
                .then()
                .statusCode(200)
                .body("find { it.routeId == 'TR13' }.agencyColor", equalTo("009B3A"));
    }

    @Test
    void shouldReturnRouteNameForTr13() {
        given()
                .when().get("/api/routes/trips-by-day")
                .then()
                .statusCode(200)
                .body("find { it.routeId == 'TR13' }.routeName", equalTo("Trolebus Linea 13"));
    }

    @Test
    void shouldReturnAgencyColorForTestRoute() {
        given()
                .when().get("/api/routes/trips-by-day")
                .then()
                .statusCode(200)
                .body("find { it.routeId == 'TEST_ROUTE' }.agencyColor", equalTo("1F5AF0"));
    }
}
