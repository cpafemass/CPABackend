package org.femass.dto;

public class QRCodeResponseDTO {
    private String qrCode;
    private String codigoValidacao;

    public QRCodeResponseDTO(String qrCode, String hash) {
        this.qrCode = qrCode;
        this.codigoValidacao = hash;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getHash() {
        return codigoValidacao;
    }

    public void setHash(String hash) {
        this.codigoValidacao = hash;
    }

    public String getCodigoValidacao() { return codigoValidacao; }
    public void setCodigoValidacao(String codigoValidacao) { this.codigoValidacao = codigoValidacao; }
}

