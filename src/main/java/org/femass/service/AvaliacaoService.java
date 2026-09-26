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
        String perguntaId = resposta.getPerguntaVersao() != null ? resposta.getPerguntaVersao().getCodigo() : resposta.getPergunta().getCodigo();
        String pergunta = resposta.getPerguntaVersao() != null ? resposta.getPerguntaVersao().getTexto() : resposta.getPergunta().getTexto();
        RespostaRelatorioDTO dto = new RespostaRelatorioDTO(
                avaliacao.getId(),
                avaliacao.getPublico().getValor(),
                avaliacao.getDisciplina() == null ? null : avaliacao.getDisciplina().getCurso().getNome(),
                avaliacao.getDisciplina() == null ? null : avaliacao.getDisciplina().getNome(),
                avaliacao.getDisciplina() == null ? null : avaliacao.getDisciplina().getProfessor(),
                perguntaId,
                pergunta,
                resposta.getNota(),
                avaliacao.getComentariosGerais()
        );
        dto.opcaoCodigo = resposta.getOpcaoCodigo();
        dto.opcaoRotulo = resposta.getOpcaoRotulo();
        dto.naoSeiResponder = resposta.isNaoSeiResponder();
        return dto;
    }
}
