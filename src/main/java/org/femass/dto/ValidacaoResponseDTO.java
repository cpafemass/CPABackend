package org.femass.dto;

public class ValidacaoResponseDTO {
    private String message;
    private String id;
    private String codigoValidacao;
    private String payload;

    public ValidacaoResponseDTO(String message, String id, String hash, String payload) {
        this.message = message;
        this.id = id;
        this.codigoValidacao = hash;
        this.payload = payload;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getHash() {
        return codigoValidacao;
    }

    public void setHash(String hash) {
        this.codigoValidacao = hash;
    }

    public String getCodigoValidacao() { return codigoValidacao; }
    public void setCodigoValidacao(String codigoValidacao) { this.codigoValidacao = codigoValidacao; }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}

