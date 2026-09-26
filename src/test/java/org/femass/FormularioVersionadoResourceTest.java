package org.femass;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class FormularioVersionadoResourceTest {

    @Test
    void deveListarCatalogoPorCampanhaEPublico() {
        given()
                .queryParam("campaign", "cpa-2026")
                .queryParam("publico", "aluno")
                .when().get("/formularios")
                .then().statusCode(200)
                .body("size()", equalTo(4))
                .body("find { it.code == 'discente_disciplinas' }.version", equalTo(1))
                .body("find { it.code == 'discente_gestao' }.questions.size()", equalTo(10))
                .body("find { it.code == 'discente_disciplinas' }.questions[0].options.size()", equalTo(5))
                .body("find { it.code == 'discente_disciplinas' }.questions[0].options.find { it.naoSeiResponder }.label",
                        equalTo("Não sei responder"));
    }

    @Test
    void deveAceitarRespostaVersionadaESanitizarComentario() {
        String formulario = """
                {
                  "campaign": "cpa-2026",
                  "form": "docente_disciplinas",
                  "formVersion": 1,
                  "respondent": {
                    "type": "professor",
                    "cpf": "529.982.247-25",
                    "matricula": "professor-versionado",
                    "aceiteTermosCondicoesServico": true
                  },
                  "course": {"name": "Administração"},
                  "subjects": [{
                    "subjectId": "1",
                    "subjectName": "Noções Básicas de Administração",
                    "teacherName": "Professor Teste",
                    "comment": "<b>comentario</b>",
                    "answers": [{
                      "questionId": "q1",
                      "questionText": "Promove o debate e instiga o pensamento crítico, colaborando para a autonomia dos estudantes.",
                      "optionCode": "nao_sei_responder"
                    }]
                  }]
                }
                """;

        given().contentType("application/json").body(formulario)
                .when().post("/formulario")
                .then().statusCode(200);

        given().queryParam("publico", "professor")
                .when().get("/avaliacoes")
                .then().statusCode(200)
                .body("find { it.perguntaId == 'q1' && it.naoSeiResponder == true }.opcaoCodigo",
                        equalTo("nao_sei_responder"))
                .body("find { it.perguntaId == 'q1' && it.naoSeiResponder == true }.comentario",
                        equalTo("comentario"));
    }

    @Test
    void deveRejeitarPerguntaEOpcaoForaDoCatalogo() {
        String base = """
                {
                  "campaign": "cpa-2026",
                  "form": "docente_disciplinas",
                  "formVersion": 1,
                  "respondent": {"type": "professor", "cpf": "529.982.247-25", "matricula": "%s", "aceiteTermosCondicoesServico": true},
                  "course": {"name": "Administração"},
                  "subjects": [{"subjectId": "1", "subjectName": "Noções Básicas de Administração", "teacherName": "Professor Teste", "answers": [{"questionId": "%s", "optionCode": "%s"}]}]
                }
                """;

        given().contentType("application/json").body(base.formatted("versao-pergunta", "q99", "concordo_totalmente"))
                .when().post("/formulario").then().statusCode(400);

        given().contentType("application/json").body(base.formatted("versao-opcao", "q1", "opcao-inexistente"))
                .when().post("/formulario").then().statusCode(400);
    }
}
