package org.femass.dto;
// Equivalente ao Respondent do json
public class UsuarioDTO {
    public String email;
    public String type; //Talvez seja legal colocar uma verificação de tipo eventualmente ou trocar para um enum
    public String cpf;
    public String matricula;
    public Boolean aceiteTermosCondicoesServico;
    /** Autorizacao opaca emitida apos a confirmacao do PIN de e-mail. */
    public String emailVerificationToken;
}
