package org.femass.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.femass.dto.FormularioDTO;
import java.util.Base64;
import java.util.List;
import java.security.SecureRandom;

@ApplicationScoped
public class QRCodeService {

    private static final int TOKEN_BYTES = 16;
    private final SecureRandom secureRandom = new SecureRandom();

    public String criarCodigo() {
        byte[] segredo = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(segredo);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(segredo);
    }

    public String codificar(FormularioDTO formularioDTO) {
        return criarCodigo();
    }
}
