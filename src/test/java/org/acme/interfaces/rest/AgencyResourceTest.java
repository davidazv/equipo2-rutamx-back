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
                .body("$.size()", equalTo(3))
                .body("agencyId", hasItems("SEMOVI", "TROLE", "MB"))
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

    @Test
    void getShouldReturnAgenciesWithAllFields() {
        given()
                .when().get("/api/agencies")
                .then()
                .statusCode(200)
                .body("[0].agencyUrl", notNullValue())
                .body("[0].agencyTimezone", notNullValue())
                .body("[0].agencyLang", notNullValue())
                .body("[0].agencyColor", notNullValue());
    }

    @Test
    void getWithColorsShouldReturn200() {
        given()
                .when().get("/api/agencies/with-colors")
                .then()
                .statusCode(200)
                .body("$.size()", equalTo(3));
    }

    @Test
    void getWithColorsShouldReturnAgencyFields() {
        given()
                .when().get("/api/agencies/with-colors")
                .then()
                .statusCode(200)
                .body("[0].agencyId", notNullValue())
                .body("[0].agencyName", notNullValue())
                .body("[0].sampleRouteColors", notNullValue())
                .body("agencyId", hasItems("SEMOVI", "TROLE"));
    }

    @Test
    void getWithColorsShouldReturnMultiColorFlag() {
        given()
                .when().get("/api/agencies/with-colors")
                .then()
                .statusCode(200)
                .body("[0].multiColor", notNullValue());
    }
}
