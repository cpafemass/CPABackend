package org.femass;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
@TestProfile(DevelopmentSwaggerProfile.class)
class SwaggerDevelopmentResourceTest {

    @Test
    void deveExporDocumentoOpenApiEUiNoPerfilDeDesenvolvimento() {
        given()
                .when().get("/openapi")
                .then()
                .statusCode(200)
                .body(containsString("CPA Backend API"))
                .body(containsString("/qrcode/gerar"));

        given()
                .redirects().follow(true)
                .when().get("/swagger-ui/")
                .then()
                .statusCode(200);
    }
}
