package org.femass.dto;

public class SolicitarVerificacaoEmailResponseDTO {
    public Long verificationId;
    public String message;

    public SolicitarVerificacaoEmailResponseDTO(Long verificationId, String message) {
        this.verificationId = verificationId;
        this.message = message;
    }
}
