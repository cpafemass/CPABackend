package org.femass.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.femass.dto.AdminFormularioDTO;
import org.femass.dto.AdminPerguntaDTO;
import org.femass.dto.OpcaoCatalogoDTO;
import org.femass.entity.*;
import org.femass.repository.*;

@ApplicationScoped
public class AdministracaoCampanhaService {
    @Inject CampanhaRepository campanhaRepository;
    @Inject FormularioRepository formularioRepository;
    @Inject FormularioVersaoRepository versaoRepository;
    @Inject PerguntaVersaoRepository perguntaRepository;
    @Inject OpcaoPerguntaRepository opcaoRepository;

    @Transactional
    public Campanha criarCampanha(String codigo, String nome, String mensagem) {
        if (codigo == null || codigo.isBlank() || nome == null || nome.isBlank()) throw new IllegalArgumentException("Codigo e nome da campanha sao obrigatorios");
        if (campanhaRepository.findByCodigo(codigo) != null) throw new IllegalArgumentException("Codigo de campanha ja existe");
        Campanha campanha = new Campanha();
        campanha.setCodigo(codigo); campanha.setNome(nome); campanha.setEstado(EstadoCampanha.RASCUNHO); campanha.setMensagemDisponibilidade(mensagem);
        campanhaRepository.persist(campanha);
        return campanha;
    }

    @Transactional
    public FormularioVersao criarFormulario(String campanhaCodigo, AdminFormularioDTO dto) {
        Campanha campanha = campanha(campanhaCodigo);
        if (formularioRepository.findByCampaignAndCode(campanhaCodigo, dto.codigo) != null) throw new IllegalArgumentException("Codigo de formulario ja existe na campanha");
        Formulario formulario = new Formulario();
        formulario.setCodigo(obrigatorio(dto.codigo, "Codigo do formulario"));
        formulario.setNome(obrigatorio(dto.nome, "Nome do formulario"));
        formulario.setPublico(PublicoAvaliacao.from(dto.publico));
        formulario.setCampanha(campanha);
        formularioRepository.persist(formulario);
        FormularioVersao versao = novaVersao(formulario, 1, dto);
        campanha.setEstado(EstadoCampanha.RASCUNHO);
        return versao;
    }

    @Transactional
    public FormularioVersao clonarVersao(String campanhaCodigo, String formularioCodigo) {
        Campanha campanha = campanha(campanhaCodigo);
        Formulario formulario = formularioRepository.findByCampaignAndCode(campanhaCodigo, formularioCodigo);
        if (formulario == null) throw new IllegalArgumentException("Formulario nao encontrado");
        FormularioVersao origem = versaoRepository.findLatest(formulario.getId());
        if (origem == null) throw new IllegalArgumentException("Formulario nao possui versao");
        FormularioVersao destino = new FormularioVersao();
        destino.setFormulario(formulario); destino.setNumero(origem.getNumero() + 1); destino.setNome(origem.getNome());
        destino.setPublico(origem.getPublico()); destino.setOrdem(origem.getOrdem()); destino.setEscopo(origem.getEscopo());
        destino.setComentarioPermitido(origem.isComentarioPermitido()); destino.setAvisoComentario(origem.getAvisoComentario());
        destino.setEstado(EstadoFormularioVersao.RASCUNHO); versaoRepository.persist(destino);
        for (PerguntaVersao pergunta : perguntaRepository.findByVersion(origem.getId())) {
            PerguntaVersao copia = new PerguntaVersao();
            copia.setFormularioVersao(destino); copia.setCodigo(pergunta.getCodigo()); copia.setTexto(pergunta.getTexto()); copia.setOrdem(pergunta.getOrdem());
            perguntaRepository.persist(copia);
            for (OpcaoPergunta opcao : opcaoRepository.findByQuestion(pergunta.getId())) {
                OpcaoPergunta opcaoCopia = new OpcaoPergunta();
                opcaoCopia.setPergunta(copia); opcaoCopia.setCodigo(opcao.getCodigo()); opcaoCopia.setRotulo(opcao.getRotulo());
                opcaoCopia.setValor(opcao.getValor()); opcaoCopia.setNaoSeiResponder(opcao.isNaoSeiResponder()); opcaoCopia.setOrdem(opcao.getOrdem());
                opcaoRepository.persist(opcaoCopia);
            }
        }
        campanha.setEstado(EstadoCampanha.RASCUNHO);
        return destino;
    }

    @Transactional
    public void atualizarVersao(String campanhaCodigo, String formularioCodigo, Integer numero, AdminFormularioDTO dto) {
        Campanha campanha = campanha(campanhaCodigo);
        Formulario formulario = formularioRepository.findByCampaignAndCode(campanhaCodigo, formularioCodigo);
        if (formulario == null) throw new IllegalArgumentException("Formulario nao encontrado");
        FormularioVersao versao = versaoRepository.findByFormularioAndNumero(formulario.getId(), numero);
        if (versao == null || versao.getEstado() != EstadoFormularioVersao.RASCUNHO)
            throw new IllegalArgumentException("Somente versoes em rascunho podem ser alteradas");
        versao.setNome(obrigatorio(dto.nome, "Nome do formulario"));
        versao.setPublico(PublicoAvaliacao.from(dto.publico));
        versao.setOrdem(dto.ordem == null ? versao.getOrdem() : dto.ordem);
        try { versao.setEscopo(EscopoFormulario.valueOf(obrigatorio(dto.escopo, "Escopo").toUpperCase())); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("Escopo deve ser DISCIPLINA ou GERAL"); }
        versao.setComentarioPermitido(dto.commentAllowed);
        versao.setAvisoComentario(dto.commentAllowed ? obrigatorio(dto.commentNotice, "Aviso de comentario") : null);
        if (dto.perguntas == null || dto.perguntas.isEmpty()) throw new IllegalArgumentException("Formulario deve ter perguntas");
        opcaoRepository.delete("pergunta.formularioVersao.id", versao.getId());
        perguntaRepository.delete("formularioVersao.id", versao.getId());
        for (AdminPerguntaDTO pergunta : dto.perguntas) criarPergunta(versao, pergunta);
        campanha.setEstado(EstadoCampanha.RASCUNHO);
    }

    @Transactional
    public void publicarVersao(String campanhaCodigo, String formularioCodigo, Integer numero) {
        Campanha campanha = campanha(campanhaCodigo);
        Formulario formulario = formularioRepository.findByCampaignAndCode(campanhaCodigo, formularioCodigo);
        if (formulario == null) throw new IllegalArgumentException("Formulario nao encontrado");
        FormularioVersao versao = versaoRepository.findByFormularioAndNumero(formulario.getId(), numero);
        if (versao == null || versao.getEstado() != EstadoFormularioVersao.RASCUNHO) throw new IllegalArgumentException("Versao em rascunho nao encontrada");
        versao.setEstado(EstadoFormularioVersao.PUBLICADA);
        campanha.setEstado(EstadoCampanha.RASCUNHO);
    }

    @Transactional public void aprovar(String codigo) {
        Campanha campanha = campanha(codigo);
        if (versaoRepository.count("formulario.campanha.id = ?1 and estado = 'PUBLICADA'", campanha.getId()) == 0)
            throw new IllegalArgumentException("Campanha deve possuir ao menos uma versao publicada antes da aprovacao");
        campanha.setEstado(EstadoCampanha.APROVADA);
    }
    @Transactional public void abrir(String codigo) {
        Campanha campanha = campanha(codigo);
        if (campanha.getEstado() != EstadoCampanha.APROVADA) throw new IllegalArgumentException("Campanha deve estar aprovada antes de abrir");
        campanha.setEstado(EstadoCampanha.ABERTA);
    }
    @Transactional public void encerrar(String codigo) { campanha(codigo).setEstado(EstadoCampanha.ENCERRADA); }

    private FormularioVersao novaVersao(Formulario formulario, int numero, AdminFormularioDTO dto) {
        FormularioVersao versao = new FormularioVersao();
        versao.setFormulario(formulario); versao.setNumero(numero); versao.setNome(obrigatorio(dto.nome, "Nome do formulario"));
        versao.setPublico(PublicoAvaliacao.from(dto.publico)); versao.setOrdem(dto.ordem == null ? numero : dto.ordem);
        try { versao.setEscopo(EscopoFormulario.valueOf(obrigatorio(dto.escopo, "Escopo").toUpperCase())); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("Escopo deve ser DISCIPLINA ou GERAL"); }
        versao.setComentarioPermitido(dto.commentAllowed); versao.setAvisoComentario(dto.commentAllowed ? obrigatorio(dto.commentNotice, "Aviso de comentario") : null);
        versao.setEstado(EstadoFormularioVersao.RASCUNHO); versaoRepository.persist(versao);
        if (dto.perguntas == null || dto.perguntas.isEmpty()) throw new IllegalArgumentException("Formulario deve ter perguntas");
        for (AdminPerguntaDTO perguntaDto : dto.perguntas) criarPergunta(versao, perguntaDto);
        return versao;
    }

    private void criarPergunta(FormularioVersao versao, AdminPerguntaDTO dto) {
        PerguntaVersao pergunta = new PerguntaVersao();
        pergunta.setFormularioVersao(versao); pergunta.setCodigo(obrigatorio(dto.codigo, "Codigo da pergunta"));
        pergunta.setTexto(obrigatorio(dto.texto, "Texto da pergunta")); pergunta.setOrdem(dto.ordem == null ? 1 : dto.ordem);
        perguntaRepository.persist(pergunta);
        if (dto.opcoes == null || dto.opcoes.isEmpty()) throw new IllegalArgumentException("Pergunta deve ter opcoes");
        for (OpcaoCatalogoDTO opcaoDto : dto.opcoes) {
            OpcaoPergunta opcao = new OpcaoPergunta(); opcao.setPergunta(pergunta);
            opcao.setCodigo(obrigatorio(opcaoDto.code, "Codigo da opcao")); opcao.setRotulo(obrigatorio(opcaoDto.label, "Rotulo da opcao"));
            opcao.setValor(opcaoDto.value); opcao.setNaoSeiResponder(opcaoDto.naoSeiResponder);
            opcao.setOrdem(opcaoDto.value == null ? 3 : opcaoDto.value); opcaoRepository.persist(opcao);
        }
    }

    private Campanha campanha(String codigo) { Campanha campanha = campanhaRepository.findByCodigo(codigo); if (campanha == null) throw new IllegalArgumentException("Campanha nao encontrada"); return campanha; }
    private String obrigatorio(String valor, String campo) { if (valor == null || valor.isBlank()) throw new IllegalArgumentException(campo + " e obrigatorio"); return valor; }
}
