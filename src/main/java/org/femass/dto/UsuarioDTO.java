package org.femass.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Equivalente ao Respondent do json
@JsonIgnoreProperties({"email", "cpf", "matricula"})
public class UsuarioDTO {
    public String type;
    public Boolean aceiteTermosCondicoesServico;
    /** Autorizacao opaca emitida apos a confirmacao do PIN de e-mail. */
    public String emailVerificationToken;
}
