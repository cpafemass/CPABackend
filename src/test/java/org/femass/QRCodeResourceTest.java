package org.femass;

import io.quarkus.test.junit.QuarkusTest;
import org.femass.entity.Validacao;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.UUID;

@QuarkusTest
class QRCodeResourceTest {

    @Test
    void deveGerarCodigoParaQRCode() throws Exception {
        Map<String, String> primeiraResposta = given()
                .contentType("application/json")
                .body("""
                        {
                          "aceiteTermosCondicoesServico": true
                        }
                        """)
                .when().post("/qrcode/gerar")
                .then()
                .statusCode(200)
                .body("qrCode", notNullValue())
                .body("codigoValidacao", notNullValue())
                .extract()
                .as(Map.class);

        String qrCode = primeiraResposta.get("qrCode");
        String codigoValidacao = primeiraResposta.get("codigoValidacao");
        assertEquals(qrCode, codigoValidacao);
        assertFalse(primeiraResposta.containsKey("hash"));
        assertEquals(16, Base64.getUrlDecoder().decode(codigoValidacao).length);

        String segundoCodigo = given()
                .contentType("application/json")
                .body("{\"aceiteTermosCondicoesServico\":true}")
                .when().post("/qrcode/gerar")
                .then().statusCode(200)
                .extract().path("codigoValidacao");

        org.hamcrest.MatcherAssert.assertThat(qrCode, org.hamcrest.Matchers.matchesPattern("[A-Za-z0-9_-]{22}"));
        assertNotEquals(codigoValidacao, segundoCodigo);
        ByteBuffer bytes = ByteBuffer.wrap(MessageDigest.getInstance("SHA-256").digest(codigoValidacao.getBytes(StandardCharsets.UTF_8)));
        UUID digest = new UUID(bytes.getLong(), bytes.getLong());
        Validacao validacao = Validacao.find("newHash", digest).firstResult();
        assertNotNull(validacao);
        org.hamcrest.MatcherAssert.assertThat(digest.toString(), org.hamcrest.Matchers.matchesPattern("[0-9a-f-]{36}"));
        org.hamcrest.MatcherAssert.assertThat(digest.toString(), not(org.hamcrest.Matchers.equalTo(qrCode)));
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
    void naoDeveDecodificarNemExporDadosPessoais() {
        Map<String, Object> resposta = given()
                .contentType("text/plain")
                .body("codigo-opaco")
                .when().post("/qrcode/decodificar")
                .then()
                .statusCode(410)
                .body("error", is("Codigos sao opacos e nao podem ser decodificados"))
                .extract().as(Map.class);

        assertFalse(resposta.containsKey("cpf"));
        assertFalse(resposta.containsKey("matricula"));
        assertFalse(resposta.containsKey("curso"));
        assertFalse(resposta.containsKey("disciplinas"));
    }
}
