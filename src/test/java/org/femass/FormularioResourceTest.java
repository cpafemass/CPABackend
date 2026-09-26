package org.femass;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class FormularioResourceTest {
    @Test
    void deveRejeitarPayloadLegado() {
        given().contentType("application/json").body("""
                {"respondent":{"type":"aluno","cpf":"529.982.247-25","matricula":"legado","aceiteTermosCondicoesServico":true}}
                """).when().post("/formulario").then().statusCode(400);
    }

    @Test
    void deveSalvarFormularioDisciplinarVersionado() {
        given().contentType("application/json").body("""
                {
                  "campaign":"cpa-2026", "form":"discente_disciplinas", "formVersion":1,
                  "respondent":{"type":"aluno","cpf":"529.982.247-25","matricula":"discente-2026","aceiteTermosCondicoesServico":true},
                  "course":{"name":"Administração"},
                  "subjects":[{"subjectId":"1","answers":[{"questionId":"q1","optionCode":"concordo_totalmente"}]}]
                }
                """).when().post("/formulario").then().statusCode(200).body("codigoValidacao", notNullValue());
    }

    @Test
    void deveSalvarFormularioGeralSemDisciplina() {
        given().contentType("application/json").body("""
                {
                  "campaign":"cpa-2026", "form":"funcionario_gestao", "formVersion":1,
                  "respondent":{"type":"funcionario","cpf":"529.982.247-25","matricula":"funcionario-2026","aceiteTermosCondicoesServico":true},
                  "answers":[{"questionId":"q1","optionCode":"nao_sei_responder"}]
                }
                """).when().post("/formulario").then().statusCode(200);

        given().queryParam("publico", "funcionario").when().get("/avaliacoes")
                .then().statusCode(200).body("find { it.naoSeiResponder }.disciplina", org.hamcrest.Matchers.nullValue());
    }
}
