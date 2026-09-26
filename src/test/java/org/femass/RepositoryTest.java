package org.femass;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.femass.entity.Avaliacao;
import org.femass.entity.Disciplina;
import org.femass.entity.Pergunta;
import org.femass.entity.PublicoAvaliacao;
import org.femass.entity.Resposta;
import org.femass.entity.Validacao;
import org.femass.repository.AvaliacaoRepository;
import org.femass.repository.CursoRepository;
import org.femass.repository.DisciplinaRepository;
import org.femass.repository.PerguntaRepository;
import org.femass.repository.RespostaRepository;
import org.femass.repository.ValidacaoRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@QuarkusTest
class RepositoryTest {

    @Inject CursoRepository cursoRepository;
    @Inject DisciplinaRepository disciplinaRepository;
    @Inject PerguntaRepository perguntaRepository;
    @Inject AvaliacaoRepository avaliacaoRepository;
    @Inject RespostaRepository respostaRepository;
    @Inject ValidacaoRepository validacaoRepository;

    @Test
    @Transactional
    void deveConsultarCursoDisciplinaEPergunta() {
        assertNotNull(cursoRepository.findByNome("Administração"));

        Disciplina disciplina = disciplinaRepository.findByIdAndCurso(1L, 1L);
        assertNotNull(disciplina);
        assertEquals(1L, disciplina.getCurso().getId());

        Pergunta pergunta = perguntaRepository.findByCodigo("q1");
        assertNotNull(pergunta);
        assertSame(pergunta, perguntaRepository.findByTexto(pergunta.getTexto()));
    }

    @Test
    @Transactional
    void devePersistirAvaliacaoComResposta() {
        Disciplina disciplina = disciplinaRepository.findByIdAndCurso(1L, 1L);
        Pergunta pergunta = perguntaRepository.findByCodigo("q1");

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setDisciplina(disciplina);
        avaliacao.setPublico(PublicoAvaliacao.PROFESSOR);

        Resposta resposta = new Resposta();
        resposta.setAvaliacao(avaliacao);
        resposta.setPergunta(pergunta);
        resposta.setNota(5);
        avaliacao.setRespostas(List.of(resposta));

        avaliacaoRepository.persistAndFlush(avaliacao);

        assertNotNull(avaliacao.getId());
        assertNotNull(resposta.getId());
        assertNotNull(respostaRepository.findById(resposta.getId()));
        assertEquals(PublicoAvaliacao.PROFESSOR, avaliacaoRepository.findById(avaliacao.getId()).getPublico());
    }

    @Test
    @Transactional
    void deveConsultarValidacaoComDigestLockEHistorico() {
        Validacao validacao = new Validacao();
        validacao.setCodigoDigest(UUID.randomUUID().toString().replace("-", ""));
        validacao.setExpiraEm(LocalDateTime.now().plusDays(1));
        validacao.setValidado(true);
        validacao.setDataValidacao(LocalDateTime.now());

        validacaoRepository.persistAndFlush(validacao);

        assertSame(validacao, validacaoRepository.findByCodigoDigest(validacao.getCodigoDigest()));
        assertSame(validacao, validacaoRepository.findByCodigoDigestForUpdate(validacao.getCodigoDigest()));
        assertEquals(1, validacaoRepository.findLastValidated(10).stream()
                .filter(item -> item.getId().equals(validacao.getId()))
                .count());
    }
}
