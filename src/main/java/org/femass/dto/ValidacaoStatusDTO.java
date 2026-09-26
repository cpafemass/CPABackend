package org.femass.dto;

public class ValidacaoStatusDTO {
    private String codigoValidacao;
    private Boolean validado;
    private String status;
    private String mensagem;

    public ValidacaoStatusDTO(String hash, Boolean validado, String status, String mensagem) {
        this.codigoValidacao = hash;
        this.validado = validado;
        this.status = status;
        this.mensagem = mensagem;
    }

    public String getHash() {
        return codigoValidacao;
    }

    public void setHash(String hash) {
        this.codigoValidacao = hash;
    }

    public String getCodigoValidacao() { return codigoValidacao; }
    public void setCodigoValidacao(String codigoValidacao) { this.codigoValidacao = codigoValidacao; }

    public Boolean getValidado() {
        return validado;
    }

    public void setValidado(Boolean validado) {
        this.validado = validado;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
}

