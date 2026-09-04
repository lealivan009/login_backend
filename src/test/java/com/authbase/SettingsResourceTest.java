package com.authbase;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class SettingsResourceTest {

    @Test
    void publicSettingsAreReadableWithoutAuth() {
        given()
                .when()
                .get("/api/settings/public")
                .then()
                .statusCode(200)
                .body("allowPublicRegistration", equalTo(true));
    }

    @Test
    void adminCanDisablePublicRegistration() {
        String adminToken = login("admin@example.com", "Admin1234!");

        try {
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(ContentType.JSON)
                    .body("""
                            {
                              "allowPublicRegistration": false
                            }
                            """)
                    .when()
                    .patch("/api/settings")
                    .then()
                    .statusCode(200)
                    .body("allowPublicRegistration", equalTo(false));

            given()
                    .contentType(ContentType.JSON)
                    .body("""
                            {
                              "email": "blocked-%s@example.com",
                              "password": "Secret123",
                              "firstName": "Bloqueado",
                              "lastName": "Registro"
                            }
                            """.formatted(System.nanoTime()))
                    .when()
                    .post("/api/auth/register")
                    .then()
                    .statusCode(403)
                    .body("error", equalTo("REGISTRATION_DISABLED"));
        } finally {
            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(ContentType.JSON)
                    .body("""
                            {
                              "allowPublicRegistration": true
                            }
                            """)
                    .when()
                    .patch("/api/settings")
                    .then()
                    .statusCode(200)
                    .body("allowPublicRegistration", equalTo(true));
        }
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
