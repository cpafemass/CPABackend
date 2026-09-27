package org.femass;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class SwaggerProductionResourceTest {

    @Test
    void naoDeveExporDocumentacaoForaDoPerfilDeDesenvolvimento() {
        given().when().get("/openapi").then().statusCode(404);
    }
}
