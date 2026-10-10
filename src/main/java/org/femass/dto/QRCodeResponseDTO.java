package org.femass.dto;

public class QRCodeResponseDTO {
    private String qrCode;
    private String codigoValidacao;
    private String codigoDigestFinal;

    public QRCodeResponseDTO(String qrCode, String codigoValidacao) {
        this.qrCode = qrCode;
        this.codigoValidacao = codigoValidacao;
    }

    public QRCodeResponseDTO(String qrCode, String codigoValidacao, String codigoDigestFinal) {
        this(qrCode, codigoValidacao);
        this.codigoDigestFinal = codigoDigestFinal;
    }

    public String getCodigoDigestFinal() { return codigoDigestFinal; }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getCodigoValidacao() { return codigoValidacao; }
    public void setCodigoValidacao(String codigoValidacao) { this.codigoValidacao = codigoValidacao; }
}

