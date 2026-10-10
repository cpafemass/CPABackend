package org.femass.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.femass.dto.QRCodeResponseDTO;
import org.femass.entity.Validacao;
import org.femass.service.FormularioService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FormularioDigestResourceTest {
    @Test
    void comprovanteRetornaOSufixoDoDigestSemAlterarOConteudoDoQRCode() throws Exception {
        String digest = "aaaaaaaa-aaaa-aaaa-aaaa-aa000000abcf";
        var validacao = new Validacao();
        validacao.setCodigoDigest(digest);
        validacao.setCodigoValidacao("codigo-opaco");
        var service = mock(FormularioService.class);
        when(service.salvarEGerarHash(null)).thenReturn(validacao);
        var resource = new FormularioResource();
        var field = FormularioResource.class.getDeclaredField("formularioService");
        field.setAccessible(true);
        field.set(resource, service);

        try (var response = resource.PostFormulario(null)) {
            assertEquals(200, response.getStatus());
            var comprovante = (QRCodeResponseDTO) response.getEntity();
            assertEquals("codigo-opaco", comprovante.getQrCode());
            assertEquals("codigo-opaco", comprovante.getCodigoValidacao());
            assertEquals("000000abcf", comprovante.getCodigoDigestFinal());
            String json = new ObjectMapper().writeValueAsString(comprovante);
            assertTrue(json.contains("\"codigoDigestFinal\":\"000000abcf\""));
            assertFalse(json.contains(digest));
        }
        verify(service, times(1)).salvarEGerarHash(null);
    }
}
