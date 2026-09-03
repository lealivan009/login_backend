package com.authbase;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

@QuarkusTest
class UserResourceTest {

    @Test
    void anonymousCannotListUsers() {
        given()
                .when()
                .get("/api/users")
                .then()
                .statusCode(401);
    }

    @Test
    void userCannotListUsers() {
        String email = "member-" + System.nanoTime() + "@example.com";
        String token = register(email, "Usuario Demo");

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/users")
                .then()
                .statusCode(403)
                .body("error", equalTo("FORBIDDEN"));
    }

    @Test
    void adminCanManageUsers() {
        String adminToken = login("admin@example.com", "Admin1234!");
        String email = "managed-" + System.nanoTime() + "@example.com";

        String userId = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "email": "%s",
                          "password": "Secret123",
                          "firstName": "Usuario",
                          "lastName": "Gestionado",
                          "role": "USER"
                        }
                        """.formatted(email))
                .when()
                .post("/api/users")
                .then()
                .statusCode(200)
                .body("email", equalTo(email))
                .body("role", equalTo("USER"))
                .body("enabled", equalTo(true))
                .extract()
                .path("id");

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/users")
                .then()
                .statusCode(200)
                .body("email", hasItem(email));

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "role": "ADMIN",
                          "enabled": false
                        }
                        """)
                .when()
                .patch("/api/users/" + userId)
                .then()
                .statusCode(200)
                .body("role", equalTo("ADMIN"))
                .body("enabled", equalTo(false));

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/api/users/" + userId)
                .then()
                .statusCode(200)
                .body("message", equalTo("Usuario eliminado"));

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/users")
                .then()
                .statusCode(200)
                .body("email", not(hasItem(email)));
    }

    @Test
    void adminCannotDeleteSelf() {
        String adminToken = login("admin@example.com", "Admin1234!");
        String adminId = given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/auth/me")
                .then()
                .statusCode(200)
                .extract()
                .path("id");

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/api/users/" + adminId)
                .then()
                .statusCode(400)
                .body("error", equalTo("CANNOT_DELETE_SELF"));
    }

    private String register(String email, String firstName) {
        return given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "email": "%s",
                          "password": "Secret123",
                          "firstName": "%s",
                          "lastName": "Demo"
                        }
                        """.formatted(email, firstName))
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(200)
                .extract()
                .path("accessToken");
    }

    private String login(String email, String password) {
        return given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "email": "%s",
                          "password": "%s"
                        }
                        """.formatted(email, password))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("accessToken");
    }
}
