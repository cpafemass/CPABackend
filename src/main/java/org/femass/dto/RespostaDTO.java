package org.femass.dto;
//Esse DTO é referente ao Answers do Json
public class RespostaDTO {
    public String questionId;
    public String questionText;
    public Integer score;
    public String scoreLabel; // Poderia ser Enum
    public String optionCode;
}
