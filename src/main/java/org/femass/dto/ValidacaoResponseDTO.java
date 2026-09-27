package org.femass.dto;

public class ValidacaoResponseDTO {
    private String message;
    private String id;
    private String codigoValidacao;
    private String status;

    public ValidacaoResponseDTO(String message, String id, String codigoValidacao, String status) {
        this.message = message;
        this.id = id;
        this.codigoValidacao = codigoValidacao;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCodigoValidacao() { return codigoValidacao; }
    public void setCodigoValidacao(String codigoValidacao) { this.codigoValidacao = codigoValidacao; }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

