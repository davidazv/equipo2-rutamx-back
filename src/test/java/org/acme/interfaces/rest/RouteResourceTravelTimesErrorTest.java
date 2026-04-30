package org.acme.interfaces.rest;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.acme.application.usecase.GetRouteTravelTimesUseCase;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;

/**
 * Tests the error path of GET /api/routes/travel-times.
 * Isolated in its own class because @InjectMock replaces the real CDI bean for
 * all tests in the class, which would break the happy-path tests in RouteResourceTest.
 */
@QuarkusTest
class RouteResourceTravelTimesErrorTest {

    @InjectMock
    GetRouteTravelTimesUseCase getTravelTimesUseCase;

    @Test
    void getTravelTimesShouldReturn500WhenUseCaseThrows() {
        when(getTravelTimesUseCase.execute()).thenThrow(new RuntimeException("DB failure"));

        given()
                .when().get("/api/routes/travel-times")
                .then()
                .statusCode(500)
                .body(containsString("Error al obtener tiempos de recorrido"));
    }
}
