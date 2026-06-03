package org.acme.interfaces.rest;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.acme.domain.models.UserStatus;
import org.acme.domain.repository.UserRepository;
import org.acme.infrastructure.firebase.FirebaseUserCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@QuarkusTest
class UserResourceTest {

    @InjectMock
    FirebaseUserCreator firebaseUserCreator;

    @Inject
    UserRepository userRepository;

    @BeforeEach
    void setUp() throws Exception {
        when(firebaseUserCreator.create(anyString(), anyString()))
                .thenAnswer(inv -> "firebase-uid-" + java.util.UUID.randomUUID());
        doNothing().when(firebaseUserCreator).deleteUser(anyString());
        doNothing().when(firebaseUserCreator).disableUser(anyString());
        doNothing().when(firebaseUserCreator).resetPassword(anyString(), anyString());
    }

    @Test
    void postShouldReturn201WhenUserIsCreated() {
        String body = """
                {
                  "firstName": "Juan",
                  "lastName": "Perez",
                  "email": "juan.perez@test.com",
                  "password": "Password1",
                  "roleId": 2
                }
                """;

        Integer id = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/admin/users")
                .then()
                .statusCode(201)
                .body("email", equalTo("juan.perez@test.com"))
                .body("firstName", equalTo("Juan"))
                .body("status", equalTo("ACTIVE"))
                .extract().path("id");

        assertNotNull(id);
        assertTrue(userRepository.findById(id.longValue()).isPresent());
    }

    @Test
    void postShouldReturn409WhenEmailIsDuplicate() {
        String body = """
                {
                  "firstName": "Dup",
                  "lastName": "User",
                  "email": "dup@test.com",
                  "password": "Password1",
                  "roleId": 2
                }
                """;

        given().contentType(ContentType.JSON).body(body)
                .when().post("/admin/users")
                .then().statusCode(201);

        given().contentType(ContentType.JSON).body(body)
                .when().post("/admin/users")
                .then()
                .statusCode(409)
                .body(containsString("El correo ya está registrado"));
    }

    @Test
    void putShouldReturn200WhenUserIsUpdated() {
        String createBody = """
                {
                  "firstName": "Maria",
                  "lastName": "Lopez",
                  "email": "maria.update@test.com",
                  "password": "Password1",
                  "roleId": 2
                }
                """;

        Integer id = given()
                .contentType(ContentType.JSON)
                .body(createBody)
                .when().post("/admin/users")
                .then().statusCode(201)
                .extract().path("id");

        String updateBody = """
                {
                  "firstName": "Maria Updated"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(updateBody)
                .when().put("/admin/users/" + id)
                .then()
                .statusCode(200)
                .body("firstName", equalTo("Maria Updated"))
                .body("lastName", equalTo("Lopez"));

        assertEquals("Maria Updated",
                userRepository.findById(id.longValue()).get().getFirstName());
    }

    @Test
    void putShouldReturn404WhenUserDoesNotExist() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"firstName\": \"Ghost\"}")
                .when().put("/admin/users/999999")
                .then()
                .statusCode(404);
    }

    @Test
    void deleteShouldReturn204WhenUserIsDeleted() {
        String createBody = """
                {
                  "firstName": "Delete",
                  "lastName": "Me",
                  "email": "delete.me@test.com",
                  "password": "Password1",
                  "roleId": 2
                }
                """;

        Integer id = given()
                .contentType(ContentType.JSON)
                .body(createBody)
                .when().post("/admin/users")
                .then().statusCode(201)
                .extract().path("id");

        given()
                .when().delete("/admin/users/" + id)
                .then()
                .statusCode(204);

        assertTrue(userRepository.findById(id.longValue()).isEmpty());
    }

    @Test
    void deleteShouldReturn404WhenUserDoesNotExist() {
        given()
                .when().delete("/admin/users/999999")
                .then()
                .statusCode(404);
    }

    @Test
    void patchSuspendShouldReturn200WhenUserIsSuspended() {
        String createBody = """
                {
                  "firstName": "Suspend",
                  "lastName": "Me",
                  "email": "suspend.me@test.com",
                  "password": "Password1",
                  "roleId": 2
                }
                """;

        Integer id = given()
                .contentType(ContentType.JSON)
                .body(createBody)
                .when().post("/admin/users")
                .then().statusCode(201)
                .extract().path("id");

        given()
                .when().patch("/admin/users/" + id + "/suspend")
                .then()
                .statusCode(200)
                .body("status", equalTo("SUSPENDED"));

        assertEquals(UserStatus.SUSPENDED,
                userRepository.findById(id.longValue()).get().getStatus());
    }

    @Test
    void patchSuspendShouldReturn409WhenAlreadySuspended() {
        String createBody = """
                {
                  "firstName": "Already",
                  "lastName": "Suspended",
                  "email": "already.suspended@test.com",
                  "password": "Password1",
                  "roleId": 2
                }
                """;

        Integer id = given()
                .contentType(ContentType.JSON)
                .body(createBody)
                .when().post("/admin/users")
                .then().statusCode(201)
                .extract().path("id");

        given().when().patch("/admin/users/" + id + "/suspend")
                .then().statusCode(200);

        given()
                .when().patch("/admin/users/" + id + "/suspend")
                .then()
                .statusCode(409)
                .body(containsString("El usuario ya está suspendido"));
    }

    // ── HU25 – Export CSV ──────────────────────────────────────────────────

    @Test
    void exportShouldReturn200WithCsvContentType() {
        given()
                .when().get("/admin/users/export")
                .then()
                .statusCode(200)
                .contentType("text/csv");
    }

    @Test
    void exportShouldReturnCsvWithHeader() {
        String body = given()
                .when().get("/admin/users/export")
                .then()
                .statusCode(200)
                .extract().asString();

        assertTrue(body.startsWith("nombre,correo,rol\n"),
                "CSV debe iniciar con la cabecera nombre,correo,rol");
    }

    @Test
    void exportShouldIncludeAtLeastOneSeedUser() {
        String body = given()
                .when().get("/admin/users/export")
                .then()
                .statusCode(200)
                .extract().asString();

        assertTrue(body.lines().count() > 1, "CSV debe tener al menos una fila de datos");
    }

    @Test
    void exportShouldReturnContentDispositionHeader() {
        given()
                .when().get("/admin/users/export")
                .then()
                .statusCode(200)
                .header("Content-Disposition", org.hamcrest.Matchers.containsString("attachment"))
                .header("Content-Disposition", org.hamcrest.Matchers.containsString("usuarios_"));
    }

    // ── Reset password ─────────────────────────────────────────────────────

    @Test
    void patchResetPasswordShouldReturn204WhenSuccessful() {
        String createBody = """
                {
                  "firstName": "Reset",
                  "lastName": "Pass",
                  "email": "reset.pass@test.com",
                  "password": "Password1",
                  "roleId": 2
                }
                """;

        Integer id = given()
                .contentType(ContentType.JSON)
                .body(createBody)
                .when().post("/admin/users")
                .then().statusCode(201)
                .extract().path("id");

        given()
                .contentType(ContentType.JSON)
                .body("{\"newPassword\": \"NuevaPass1\"}")
                .when().patch("/admin/users/" + id + "/reset-password")
                .then()
                .statusCode(204);
    }

    @Test
    void patchResetPasswordShouldReturn404WhenUserNotFound() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"newPassword\": \"NuevaPass1\"}")
                .when().patch("/admin/users/999999/reset-password")
                .then()
                .statusCode(404);
    }

    @Test
    void patchResetPasswordShouldReturn400WhenPasswordInvalid() {
        String createBody = """
                {
                  "firstName": "Bad",
                  "lastName": "Pass",
                  "email": "bad.pass@test.com",
                  "password": "Password1",
                  "roleId": 2
                }
                """;

        Integer id = given()
                .contentType(ContentType.JSON)
                .body(createBody)
                .when().post("/admin/users")
                .then().statusCode(201)
                .extract().path("id");

        given()
                .contentType(ContentType.JSON)
                .body("{\"newPassword\": \"sinmayuscula\"}")
                .when().patch("/admin/users/" + id + "/reset-password")
                .then()
                .statusCode(400);
    }
}
