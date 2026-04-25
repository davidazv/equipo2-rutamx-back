package org.acme.interfaces.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class AgencyResourceTest {

    @Test
    void getShouldReturnAllAgencies() {
        given()
                .when().get("/api/agencies")
                .then()
                .statusCode(200)
                .body("$.size()", equalTo(2))
                .body("agencyId", hasItems("SEMOVI", "TROLE"))
                .body("agencyName", hasItems("SEMOVI", "Servicio de Transportes Electricos"));
    }

    @Test
    void getShouldReturnAgenciesWithRequiredFields() {
        given()
                .when().get("/api/agencies")
                .then()
                .statusCode(200)
                .body("[0].agencyId", notNullValue())
                .body("[0].agencyName", notNullValue());
    }
}
