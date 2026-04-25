package org.acme.interfaces.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class RouteResourceTest {

    @Test
    void getShouldReturnRoutesWithDistance() {
        given()
                .when().get("/api/routes")
                .then()
                .statusCode(200)
                .body("$.size()", greaterThanOrEqualTo(1))
                .body("[0].routeId", notNullValue())
                .body("[0].distanceKm", notNullValue())
                .body("[0].distanceKm", greaterThan(0.0f));
    }

    @Test
    void getShouldReturnSingleRouteWithDistance() {
        given()
                .when().get("/api/routes/TR13")
                .then()
                .statusCode(200)
                .body("routeId", equalTo("TR13"))
                .body("routeShortName", equalTo("13"))
                .body("distanceKm", greaterThan(0.0f));
    }

    @Test
    void getShouldReturn404WhenRouteNotFound() {
        given()
                .when().get("/api/routes/NONEXISTENT")
                .then()
                .statusCode(404);
    }

    @Test
    void getShouldReturnExactNumberOfSeedRoutes() {
        given()
                .when().get("/api/routes")
                .then()
                .statusCode(200)
                .body("$.size()", equalTo(2));
    }

    @Test
    void getShouldReturnTestRouteWithDistance() {
        given()
                .when().get("/api/routes/TEST_ROUTE")
                .then()
                .statusCode(200)
                .body("routeId", equalTo("TEST_ROUTE"))
                .body("distanceKm", equalTo(15.0f));
    }

    @Test
    void getShouldReturnTr13WithExactDistance() {
        given()
                .when().get("/api/routes/TR13")
                .then()
                .statusCode(200)
                .body("distanceKm", equalTo(20.0f));
    }

    @Test
    void getShapesShouldReturnRoutesWithCoordinates() {
        given()
                .when().get("/api/routes/shapes")
                .then()
                .statusCode(200)
                .body("$.size()", equalTo(2))
                .body("[0].routeId", notNullValue())
                .body("[0].coordinates", notNullValue())
                .body("[0].coordinates.size()", greaterThanOrEqualTo(2))
                .body("[0].distanceKm", greaterThan(0.0f));
    }

    @Test
    void getShapesByAgencyShouldFilterRoutes() {
        given()
                .queryParam("agencyId", "SEMOVI")
                .when().get("/api/routes/shapes")
                .then()
                .statusCode(200)
                .body("$.size()", equalTo(1))
                .body("[0].routeId", equalTo("TR13"))
                .body("[0].agencyId", equalTo("SEMOVI"));
    }

    @Test
    void getShapesByUnknownAgencyShouldReturnEmpty() {
        given()
                .queryParam("agencyId", "NONEXISTENT")
                .when().get("/api/routes/shapes")
                .then()
                .statusCode(200)
                .body("$.size()", equalTo(0));
    }

    @Test
    void getShapesByBlankAgencyShouldReturnAllRoutes() {
        given()
                .queryParam("agencyId", "  ")
                .when().get("/api/routes/shapes")
                .then()
                .statusCode(200)
                .body("$.size()", equalTo(2));
    }

    @Test
    void getShouldReturnRouteWithAllFields() {
        given()
                .when().get("/api/routes/TR13")
                .then()
                .statusCode(200)
                .body("routeId", equalTo("TR13"))
                .body("agencyId", equalTo("SEMOVI"))
                .body("routeShortName", equalTo("13"))
                .body("routeLongName", equalTo("Trolebus Linea 13"))
                .body("routeType", equalTo(11))
                .body("distanceKm", equalTo(20.0f));
    }

    @Test
    void getShapesShouldReturnTr13With4Points() {
        given()
                .when().get("/api/routes/shapes")
                .then()
                .statusCode(200)
                .body("find { it.routeId == 'TR13' }.coordinates.size()", equalTo(4))
                .body("find { it.routeId == 'TR13' }.distanceKm", equalTo(20.0f))
                .body("find { it.routeId == 'TR13' }.routeShortName", equalTo("13"));
    }
}
