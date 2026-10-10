package org.femass;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.transaction.Transactional;
import org.femass.entity.Validacao;
import org.femass.service.ValidacaoService;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;

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
        ByteBuffer bytes = ByteBuffer.wrap(MessageDigest.getInstance("SHA-256").digest(codigo.getBytes(StandardCharsets.UTF_8)));
        UUID digest = new UUID(bytes.getLong(), bytes.getLong());

        Validacao primeira = validacaoService.armazenarCodigoValidacao(codigo, true);
        Validacao segunda = validacaoService.armazenarCodigoValidacao(codigo, true);

        assertNotNull(primeira.getId());
        assertEquals(primeira.getId(), segunda.getId());
        assertEquals(36, primeira.getCodigoDigest().length());
        assertNull(primeira.getOldToken());
        assertEquals(digest, primeira.getNewHash());
        assertEquals(1L, Validacao.count("newHash", digest));
    }
    @Test
    void deveValidarCodigoOpacoSemRetornarDadosPessoais() throws Exception {
        String codigo = given()
                .contentType("application/json")
                .body("""
                        {
                          "aceiteTermosCondicoesServico": true
                        }
                        """)
                .when().post("/qrcode/gerar")
                .then()
                .statusCode(200)
                .extract()
                .path("codigoValidacao");

        Map<String, Object> resposta = given()
                .queryParam("codigoValidacao", codigo)
                .when().put("/validacao/validar-hash")
                .then()
                .statusCode(200)
                .body("codigoValidacao", is(codigo))
                .extract().as(Map.class);

        assertFalse(resposta.containsKey("cpf"));
        assertFalse(resposta.containsKey("matricula"));
        assertFalse(resposta.containsKey("curso"));
        assertFalse(resposta.containsKey("disciplinas"));
        assertFalse(resposta.containsKey("hash"));
        ByteBuffer bytes = ByteBuffer.wrap(MessageDigest.getInstance("SHA-256").digest(codigo.getBytes(StandardCharsets.UTF_8)));
        UUID digest = new UUID(bytes.getLong(), bytes.getLong());
        String identificador = digest.toString().substring(26);
        assertEquals(identificador, resposta.get("codigoDigestFinal"));
        assertFalse(resposta.containsKey("codigoDigest"));

        List<Map<String, Object>> historico = given()
            .when().get("/validacao/historico")
            .then().statusCode(200).extract().as(List.class);
        assertTrue(historico.size() <= 10);
        assertTrue(historico.stream().anyMatch(item -> identificador.equals(item.get("codigoDigestFinal"))));
        assertTrue(historico.stream().allMatch(item -> !item.containsKey("codigoDigest")));
    }

    @Test
    void deveBloquearReusoDoMesmoCodigoValidado() {
        String codigo = given()
                .contentType("application/json")
                .body("""
                        {
                          "aceiteTermosCondicoesServico": true
                        }
                        """)
                .when().post("/qrcode/gerar")
                .then()
                .statusCode(200)
                .extract()
                .path("codigoValidacao");

        given()
                .queryParam("codigoValidacao", codigo)
                .when().put("/validacao/validar-hash")
                .then()
                .statusCode(200);

        given()
                .queryParam("codigoValidacao", codigo)
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

        ByteBuffer bytes = ByteBuffer.wrap(MessageDigest.getInstance("SHA-256").digest(codigo.getBytes(StandardCharsets.UTF_8)));
        UUID digest = new UUID(bytes.getLong(), bytes.getLong());
        Validacao validacao = Validacao.find("newHash", digest).firstResult();
        validacao.setExpiraEm(LocalDateTime.now().minusMinutes(1));

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                () -> validacaoService.validarCodigo(codigo));
    }
}
