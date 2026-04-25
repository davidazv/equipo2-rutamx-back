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
}
