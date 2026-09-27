package org.femass;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusIntegrationTest
class SwaggerProductionResourceIT extends SwaggerProductionResourceTest {

    @Test
    void naoDeveIncluirSwaggerUiNoArtefatoDeProducao() {
        given()
                .redirects().follow(false)
                .when().get("/swagger-ui/")
                .then()
                .statusCode(404);
    }
}
