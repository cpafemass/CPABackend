package org.femass;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import io.restassured.path.json.JsonPath;
import org.femass.repository.CursoRepository;
import org.femass.repository.AvaliacaoRepository;
import org.junit.jupiter.api.Test;


import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.jupiter.api.Assertions.assertNull;

@QuarkusTest
class FormularioResourceTest {

    @Inject
    CursoRepository cursoRepository;

    @Inject
    AvaliacaoRepository avaliacaoRepository;

    @Test
    void deveSalvarFormularioERetornarHash() {
        String formulario = """
                {
                  "schemaVersion": "1",
                  "confirmationCode": "confirmacao-teste",
                  "respondent": {
                    "cpf": "529.982.247-25",
                    "matricula": "20260001",
                    "aceiteTermosCondicoesServico": true
                  },
                  "course": {
                    "name": "Administração"
                  },
                  "subjects": [
                    {
                      "subjectName": "Noções Básicas de Administração",
                      "subjectId": "1",
                      "teacherName": "Professor Teste",
                      "answers": [
                        {
                          "questionText": "Pergunta teste?",
                          "score": 5
                        }
                      ],
                      "comment": "Comentario teste"
                    }
                  ]
                }
                """;

        JsonPath response = given()
                .contentType("application/json")
                .body(formulario)
                .when().post("/formulario")
                .then()
                .statusCode(200)
                .body("hash", notNullValue())
                .body("qrCode", notNullValue())
                .extract()
                .jsonPath();

        org.hamcrest.MatcherAssert.assertThat(response.getString("hash"), equalTo(response.getString("qrCode")));
    }

    @Test
    void deveRetornarQRCodeOpacoSemDadosPessoais() {
        String formulario = """
                {
                  "schemaVersion": "1",
                  "confirmationCode": "confirmacao-teste-decodificacao",
                  "respondent": {
                    "cpf": "529.982.247-25",
                    "matricula": "20260002",
                    "aceiteTermosCondicoesServico": true
                  },
                  "course": {
                    "name": "Administração"
                  },
                  "subjects": [
                    {
                      "subjectName": "Noções Básicas de Administração",
                      "subjectId": "1",
                      "teacherName": "Professor Teste",
                      "answers": [
                        {
                          "questionText": "Pergunta teste QR?",
                          "score": 4
                        }
                      ]
                    },
                    {
                      "subjectName": "Filosofia e Ética",
                      "subjectId": "2",
                      "teacherName": "Professor Teste",
                      "answers": [
                        {
                          "questionText": "Pergunta teste QR 2?",
                          "score": 5
                        }
                      ]
                    }
                  ]
                }
                """;

        String qrCode = given()
                .contentType("application/json")
                .body(formulario)
                .when().post("/formulario")
                .then()
                .statusCode(200)
                .body("hash", notNullValue())
                .body("qrCode", notNullValue())
                .extract()
                .path("qrCode");

        org.hamcrest.MatcherAssert.assertThat(qrCode, org.hamcrest.Matchers.matchesPattern("[A-Za-z0-9_-]{22}"));
        org.hamcrest.MatcherAssert.assertThat(qrCode, org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("9876")));
        org.hamcrest.MatcherAssert.assertThat(qrCode, org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("20260002")));
    }

    @Test
    void deveGerarCodigosDiferentesParaFormulariosIguais() {
        String formulario = """
                {
                  "schemaVersion": "1",
                  "confirmationCode": "confirmacao-teste-unico",
                  "respondent": {
                    "cpf": "529.982.247-25",
                    "matricula": "20260003",
                    "aceiteTermosCondicoesServico": true
                  },
                  "course": {
                    "name": "Administração"
                  },
                  "subjects": [
                    {
                      "subjectName": "Matemática I",
                      "subjectId": "3",
                      "teacherName": "Professor Teste",
                      "answers": [
                        {
                          "questionText": "Pergunta codigo unico?",
                          "score": 5
                        }
                      ]
                    }
                  ]
                }
                """;

        String primeiroCodigo = given()
                .contentType("application/json")
                .body(formulario)
                .when().post("/formulario")
                .then()
                .statusCode(200)
                .extract()
                .path("qrCode");

        String segundoCodigo = given()
                .contentType("application/json")
                .body(formulario)
                .when().post("/formulario")
                .then()
                .statusCode(200)
                .extract()
                .path("qrCode");

        org.hamcrest.MatcherAssert.assertThat(segundoCodigo, not(equalTo(primeiroCodigo)));
    }
    @Test
    @Transactional
    void deveFazerRollbackQuandoUmaDisciplinaNaoExiste() {
        String curso = "Curso rollback " + System.nanoTime();
        String formulario = """
                {
                  "respondent": {
                    "cpf": "529.982.247-25",
                    "matricula": "rollback-2026",
                    "aceiteTermosCondicoesServico": true
                  },
                  "course": {"name": "%s"},
                  "subjects": [{
                    "subjectId": "999999",
                    "subjectName": "Disciplina inexistente",
                    "teacherName": "Professor Teste",
                    "answers": [{"questionText": "Pergunta rollback?", "score": 5}]
                  }]
                }
                """.formatted(curso);

        given()
                .contentType("application/json")
                .body(formulario)
                .when().post("/formulario")
                .then().statusCode(400);

        assertNull(cursoRepository.findByNome(curso));
    }

    @Test
    void devePersistirPublicoProfessorSemPersistirIdentidade() {
        String formulario = """
                {
                  "respondent": {
                    "type": "professor",
                    "cpf": "529.982.247-25",
                    "matricula": "professor-2026",
                    "aceiteTermosCondicoesServico": true
                  },
                  "course": {"name": "Administração"},
                  "subjects": [{
                    "subjectId": "1",
                    "subjectName": "Noções Básicas de Administração",
                    "teacherName": "Professor Teste",
                    "answers": [{"questionText": "Pergunta publico?", "score": 4}]
                  }]
                }
                """;

        given()
                .contentType("application/json")
                .body(formulario)
                .when().post("/formulario")
                .then().statusCode(200);

        org.femass.entity.Avaliacao avaliacao = avaliacaoRepository
                .find("publico", org.femass.entity.PublicoAvaliacao.PROFESSOR)
                .list().stream()
                .filter(item -> item.getComentariosGerais() == null)
                .findFirst()
                .orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals(
                org.femass.entity.PublicoAvaliacao.PROFESSOR, avaliacao.getPublico());
    }

    @Test
    void deveRejeitarPublicoDesconhecido() {
        String formulario = """
                {
                  "respondent": {
                    "type": "egresso",
                    "cpf": "529.982.247-25",
                    "matricula": "egresso-2026",
                    "aceiteTermosCondicoesServico": true
                  },
                  "course": {"name": "Administração"},
                  "subjects": [{
                    "subjectId": "1",
                    "subjectName": "Noções Básicas de Administração",
                    "teacherName": "Professor Teste",
                    "answers": [{"questionText": "Pergunta invalida?", "score": 4}]
                  }]
                }
                """;

        given()
                .contentType("application/json")
                .body(formulario)
                .when().post("/formulario")
                .then().statusCode(400);
    }

    @Test
    void deveConsultarRespostasFiltradasPorPublico() {
        String formulario = """
                {
                  "respondent": {
                    "type": "funcionário",
                    "cpf": "529.982.247-25",
                    "matricula": "funcionario-2026",
                    "aceiteTermosCondicoesServico": true
                  },
                  "course": {"name": "Administração"},
                  "subjects": [{
                    "subjectId": "1",
                    "subjectName": "Noções Básicas de Administração",
                    "teacherName": "Professor Teste",
                    "answers": [{"questionText": "Pergunta relatorio?", "score": 3}]
                  }]
                }
                """;

        given()
                .contentType("application/json")
                .body(formulario)
                .when().post("/formulario")
                .then().statusCode(200);

        given()
                .when().get("/avaliacoes?publico=funcionario")
                .then()
                .statusCode(200)
                .body("find { it.pergunta == 'Pergunta relatorio?' }.publico", equalTo("funcionario"))
                .body("find { it.pergunta == 'Pergunta relatorio?' }.nota", equalTo(3))
                .body("find { it.pergunta == 'Pergunta relatorio?' }.cpf", org.hamcrest.Matchers.nullValue())
                .body("find { it.pergunta == 'Pergunta relatorio?' }.matricula", org.hamcrest.Matchers.nullValue())
                .body("find { it.pergunta == 'Pergunta relatorio?' }.email", org.hamcrest.Matchers.nullValue());
    }
}
