package com.authbase;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class AuthResourceTest {

    @Test
    void registerLoginAndMe() {
        String email = "user-" + System.nanoTime() + "@example.com";

        String accessToken = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "email": "%s",
                          "password": "Secret123",
                          "fullName": "Usuario Demo"
                        }
                        """.formatted(email))
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .body("user.email", equalTo(email))
                .extract()
                .path("accessToken");

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "email": "%s",
                          "password": "Secret123"
                        }
                        """.formatted(email))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("user.fullName", equalTo("Usuario Demo"));

        given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/auth/me")
                .then()
                .statusCode(200)
                .body("email", equalTo(email));
    }

    @Test
    void loginRejectsBadPassword() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "email": "nobody@example.com",
                          "password": "Wrong123"
                        }
                        """)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(401)
                .body("error", equalTo("INVALID_CREDENTIALS"));
    }

    @Test
    void registerRejectsWeakPassword() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "email": "weak@example.com",
                          "password": "password",
                          "fullName": "Usuario Demo"
                        }
                        """)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(400)
                .body("error", equalTo("WEAK_PASSWORD"));
    }
}
