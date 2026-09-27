package org.femass;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.femass.service.EmailSender;
import org.junit.jupiter.api.Test;

import org.mockito.ArgumentCaptor;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@QuarkusTest
class VerificacaoEmailResourceTest {
    @InjectMock
    EmailSender emailSender;

    @Test
    void deveEnviarPinConfirmarEUsoUnico() {
        Number verificationId = given().contentType("application/json").body("""
                {"email":"professor@femass.edu.br","publico":"professor","campaign":"cpa-2026","form":"docente_gestao","formVersion":1}
                """).when().post("/verificacao-email/solicitar")
                .then().statusCode(202).body("verificationId", notNullValue())
                .extract().path("verificationId");

        ArgumentCaptor<String> pinCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailSender).enviarPin(anyString(), pinCaptor.capture());
        String pin = pinCaptor.getValue();
        assertEquals(16, pin.length());

        String token = given().contentType("application/json").body("""
                {"verificationId":%d,"pin":"%s"}
                """.formatted(verificationId.longValue(), pin)).when().post("/verificacao-email/confirmar")
                .then().statusCode(200).body("submissionToken", notNullValue())
                .extract().path("submissionToken");

        given().contentType("application/json").body("""
                {"campaign":"cpa-2026", "form":"docente_gestao", "formVersion":1,
                 "respondent":{"type":"professor","cpf":"529.982.247-25","matricula":"prof-2026","emailVerificationToken":"%s","aceiteTermosCondicoesServico":true},
                 "answers":[{"questionId":"q1","optionCode":"concordo_totalmente"}]}
                """.formatted(token)).when().post("/formulario").then().statusCode(200);

        given().contentType("application/json").body("""
                {"campaign":"cpa-2026", "form":"docente_gestao", "formVersion":1,
                 "respondent":{"type":"professor","cpf":"529.982.247-25","matricula":"prof-2026","emailVerificationToken":"%s","aceiteTermosCondicoesServico":true},
                 "answers":[{"questionId":"q1","optionCode":"concordo_totalmente"}]}
                """.formatted(token)).when().post("/formulario").then().statusCode(400);
    }

    @Test
    void deveRejeitarEmailExterno() {
        given().contentType("application/json").body("""
                {"email":"pessoa@gmail.com","publico":"funcionario","campaign":"cpa-2026","form":"funcionario_gestao","formVersion":1}
                """).when().post("/verificacao-email/solicitar").then().statusCode(400);
    }

    @Test
    void reenvioInvalidaPinAnterior() {
        String request = """
                {"email":"reenvio@femass.edu.br","publico":"funcionario","campaign":"cpa-2026","form":"funcionario_gestao","formVersion":1}
                """;
        Number primeiro = given().contentType("application/json").body(request).when().post("/verificacao-email/solicitar")
                .then().statusCode(202).extract().path("verificationId");
        Number segundo = given().contentType("application/json").body(request).when().post("/verificacao-email/solicitar")
                .then().statusCode(202).extract().path("verificationId");
        ArgumentCaptor<String> pinCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailSender, times(2)).enviarPin(anyString(), pinCaptor.capture());
        String pinAnterior = pinCaptor.getAllValues().get(0);

        given().contentType("application/json").body("""
                {"verificationId":%d,"pin":"%s"}
                """.formatted(primeiro.longValue(), pinAnterior)).when().post("/verificacao-email/confirmar").then().statusCode(400);
        assertEquals(false, primeiro.longValue() == segundo.longValue());
    }
}
