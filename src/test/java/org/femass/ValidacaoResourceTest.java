package org.femass;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.transaction.Transactional;
import org.femass.entity.Validacao;
import org.femass.service.ValidacaoService;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class ValidacaoResourceTest {
    @jakarta.inject.Inject
    ValidacaoService validacaoService;
    @Test
    void testHelloEndpoint() {
        given()
          .when().get("/validacao/verificar-status")
          .then()
             .statusCode(400)
             .body("error", is("Codigo de validacao e obrigatorio"));
    }

    @Test
    @Transactional
    void deveUsarIdTecnicoEManterDigestUnico() throws Exception {
        String codigo = "codigo-tecnico-" + UUID.randomUUID();
        String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(codigo.getBytes(StandardCharsets.UTF_8)));

        Validacao primeira = validacaoService.armazenarCodigoValidacao(codigo, true);
        Validacao segunda = validacaoService.armazenarCodigoValidacao(codigo, true);

        assertNotNull(primeira.getId());
        assertEquals(primeira.getId(), segunda.getId());
        assertEquals(64, primeira.getCodigoDigest().length());
        assertEquals(digest, primeira.getCodigoDigest());
        assertEquals(1L, Validacao.count("codigoDigest", digest));
    }
    @Test
    void deveValidarCodigoOpacoSemRetornarDadosPessoais() {
        String identificador = UUID.randomUUID().toString();
        String codigo = given()
                .contentType("application/json")
                .body("""
                        {
                          "cpf": "1234",
                          "matricula": "20260001",
                          "aceiteTermosCondicoesServico": true,
                          "cursos": ["Sistemas da Informacao"],
                          "disciplinas": ["ALG", "SIS"],
                          "identificador": "%s"
                        }
                        """.formatted(identificador))
                .when().post("/qrcode/gerar")
                .then()
                .statusCode(200)
                .extract()
                .path("hash");

        given()
                .queryParam("hash", codigo)
                .when().put("/validacao/validar-hash")
                .then()
                .statusCode(200)
                .body("codigoValidacao", is(codigo))
                .body("cpf", org.hamcrest.Matchers.nullValue())
                .body("matricula", org.hamcrest.Matchers.nullValue());
    }

    @Test
    void deveBloquearReusoDoMesmoCodigoValidado() {
        String identificador = UUID.randomUUID().toString();
        String codigo = given()
                .contentType("application/json")
                .body("""
                        {
                          "cpf": "5678",
                          "matricula": "20260002",
                          "aceiteTermosCondicoesServico": true,
                          "cursos": ["Administracao"],
                          "disciplinas": ["ADM"],
                          "identificador": "%s"
                        }
                        """.formatted(identificador))
                .when().post("/qrcode/gerar")
                .then()
                .statusCode(200)
                .extract()
                .path("hash");

        given()
                .queryParam("hash", codigo)
                .when().put("/validacao/validar-hash")
                .then()
                .statusCode(200);

        given()
                .queryParam("hash", codigo)
                .when().put("/validacao/validar-hash")
                .then()
                .statusCode(409)
                .body("error", is("Codigo ja foi validado e nao pode ser reutilizado"));
    }

    @Test
    @Transactional
    void deveRecusarCodigoExpirado() throws Exception {
        String codigo = given()
                .contentType("application/json")
                .body("{\"aceiteTermosCondicoesServico\":true}")
                .when().post("/qrcode/gerar")
                .then().statusCode(200).extract().path("codigoValidacao");

        String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(codigo.getBytes(StandardCharsets.UTF_8)));
        Validacao validacao = Validacao.find("codigoDigest", digest).firstResult();
        validacao.setExpiraEm(LocalDateTime.now().minusMinutes(1));

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                () -> validacaoService.validarCodigo(codigo));
    }
}
