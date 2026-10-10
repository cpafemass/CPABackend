package org.femass;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.femass.entity.Validacao;
import org.femass.repository.ValidacaoRepository;
import org.femass.service.ValidacaoService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import java.util.UUID;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class ValidacaoServiceTest {

    @Inject
    ValidacaoService validacaoService;

    @InjectMock
    ValidacaoRepository validacaoRepository;

    @Test
    void deveDelegarBuscaEPersistenciaAoRepository() {
        when(validacaoRepository.findByCodigoDigest(any(UUID.class))).thenReturn(null);

        Validacao validacao = validacaoService.armazenarCodigoValidacao("codigo-unitario", true);

        assertNotNull(validacao);
        verify(validacaoRepository).findByCodigoDigest(any(UUID.class));
        verify(validacaoRepository).persist(any(Validacao.class));
    }
}
