package org.femass;

import io.quarkus.test.junit.QuarkusTest;
import org.femass.entity.Validacao;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import java.util.regex.Pattern;

@QuarkusTest
class QRCodeResourceTest {

    @Test
    void deveGerarCodigoParaQRCode() {
        String qrCode = given()
                .contentType("application/json")
                .body("""
                        {
                          "cpf": "1234",
                          "matricula": "20260001",
                          "aceiteTermosCondicoesServico": true,
                          "cursos": ["Sistemas da Informacao"],
                          "disciplinas": ["ALG", "SIS"],
                          "identificador": "11111111-1111-1111-1111-111111111111"
                        }
                        """)
                .when().post("/qrcode/gerar")
                .then()
                .statusCode(200)
                .body("hash", notNullValue())
                .body("qrCode", notNullValue())
                .body("codigoValidacao", notNullValue())
                .extract()
                .path("qrCode");

        org.hamcrest.MatcherAssert.assertThat(qrCode, org.hamcrest.Matchers.matchesPattern("[A-Za-z0-9_-]{22}"));
        org.hamcrest.MatcherAssert.assertThat(qrCode, not(org.hamcrest.Matchers.containsString("1234")));
        String digest = Validacao.findAll().stream().map(Validacao.class::cast)
                .map(Validacao::getCodigoDigest).filter(Pattern.compile("[0-9a-f]{64}").asPredicate())
                .findFirst().orElseThrow();
        org.hamcrest.MatcherAssert.assertThat(digest, org.hamcrest.Matchers.matchesPattern("[0-9a-f]{64}"));
        org.hamcrest.MatcherAssert.assertThat(digest, not(org.hamcrest.Matchers.equalTo(qrCode)));
    }

    @Test
    void deveRetornarErroQuandoPayloadForVazio() {
        given()
                .contentType("application/json")
                .body("")
                .when().post("/qrcode/gerar")
                .then()
                .statusCode(400)
                .body("error", is("Payload do QR Code e obrigatorio"));
    }

    @Test
    void deveRetornarErroQuandoCodigoForInvalido() {
        given()
                .contentType("text/plain")
                .body("codigo-invalido")
                .when().post("/qrcode/decodificar")
                .then()
                .statusCode(410)
                .body("error", is("Codigos sao opacos e nao podem ser decodificados"));
    }
}
