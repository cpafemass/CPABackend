package org.femass.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.femass.dto.RespostaRelatorioDTO;
import org.femass.entity.Avaliacao;
import org.femass.entity.PublicoAvaliacao;
import org.femass.entity.Resposta;
import org.femass.repository.AvaliacaoRepository;

import java.util.List;

@ApplicationScoped
public class AvaliacaoService {

    @Inject
    AvaliacaoRepository avaliacaoRepository;

    @Transactional
    public List<RespostaRelatorioDTO> buscarRespostas(PublicoAvaliacao publico) {
        return avaliacaoRepository.findByPublico(publico).stream()
                .flatMap(avaliacao -> avaliacao.getRespostas().stream()
                        .map(resposta -> toDTO(avaliacao, resposta)))
                .toList();
    }

    private RespostaRelatorioDTO toDTO(Avaliacao avaliacao, Resposta resposta) {
        return new RespostaRelatorioDTO(
                avaliacao.getId(),
                avaliacao.getPublico().getValor(),
                avaliacao.getDisciplina().getCurso().getNome(),
                avaliacao.getDisciplina().getNome(),
                avaliacao.getDisciplina().getProfessor(),
                resposta.getPergunta().getCodigo(),
                resposta.getPergunta().getTexto(),
                resposta.getNota(),
                avaliacao.getComentariosGerais()
        );
    }
}
