package org.femass;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.Test;


import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.not;

@QuarkusTest
class FormularioResourceTest {

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
}
