package org.femass.dto;

public class QRCodeResponseDTO {
    private String qrCode;
    private String codigoValidacao;

    public QRCodeResponseDTO(String qrCode, String codigoValidacao) {
        this.qrCode = qrCode;
        this.codigoValidacao = codigoValidacao;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getCodigoValidacao() { return codigoValidacao; }
    public void setCodigoValidacao(String codigoValidacao) { this.codigoValidacao = codigoValidacao; }
}

