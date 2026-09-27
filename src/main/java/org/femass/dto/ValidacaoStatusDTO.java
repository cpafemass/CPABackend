package org.femass.dto;

public class ValidacaoStatusDTO {
    private String codigoValidacao;
    private Boolean validado;
    private String status;
    private String mensagem;

    public ValidacaoStatusDTO(String codigoValidacao, Boolean validado, String status, String mensagem) {
        this.codigoValidacao = codigoValidacao;
        this.validado = validado;
        this.status = status;
        this.mensagem = mensagem;
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

