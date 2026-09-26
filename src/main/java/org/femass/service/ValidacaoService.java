package org.femass.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.femass.entity.Validacao;

import java.util.List;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@ApplicationScoped
public class ValidacaoService {

    @Transactional
    public Validacao armazenarCodigoValidacao(String codigoValidacao) {
        return armazenarCodigoValidacao(codigoValidacao, false);
    }

    @Transactional
    public Validacao armazenarCodigoValidacao(String codigoValidacao, Boolean aceiteTermosCondicoesServico) {
        if (codigoValidacao == null || codigoValidacao.isBlank()) {
            throw new IllegalArgumentException("Codigo de validacao e obrigatorio");
        }

        String digest = digest(codigoValidacao);
        Validacao validacaoExistente = Validacao.find("hash", digest).firstResult();
        if (validacaoExistente != null) {
            return validacaoExistente;
        }

        Validacao validacao = new Validacao();
        validacao.setHash(digest);
        validacao.setValidado(false);
        validacao.setAceiteTermosCondicoesServico(Boolean.TRUE.equals(aceiteTermosCondicoesServico));
        validacao.persist();

        return validacao;
    }

    @Transactional
    public Validacao validarHash(String hash) {
        Validacao validacao = Validacao.find("hash", digest(hash)).firstResult();

        if (validacao == null) {
            throw new IllegalArgumentException("Hash nao encontrado no banco de dados");
        }

        if (Boolean.TRUE.equals(validacao.getValidado())) {
            throw new IllegalStateException("Codigo ja foi validado e nao pode ser reutilizado");
        }

        if (validacao.getTentativasValidacao() == null) {
            validacao.setTentativasValidacao(0);
        }
        validacao.setTentativasValidacao(validacao.getTentativasValidacao() + 1);
        validacao.setValidado(true);
        validacao.setDataValidacao(java.time.LocalDateTime.now());

        return validacao;
    }

    public java.util.Map<String, Object> validarHashComDetalhes(String hash) {
        Validacao validacao = validarHash(hash);

        long tempoDecorridoMs = 0;
        if (validacao.getDataCriacao() != null && validacao.getDataValidacao() != null) {
            tempoDecorridoMs = java.time.temporal.ChronoUnit.MILLIS.between(
                    validacao.getDataCriacao(),
                    validacao.getDataValidacao()
            );
        }

        java.util.Map<String, Object> detalhes = new java.util.HashMap<>();
        detalhes.put("hashValido", true);
        detalhes.put("hash", validacao.getHash());
        detalhes.put("validado", validacao.getValidado());
        detalhes.put("dataCriacao", validacao.getDataCriacao());
        detalhes.put("dataValidacao", validacao.getDataValidacao());
        detalhes.put("tentativasValidacao", validacao.getTentativasValidacao());
        detalhes.put("tempoDecorridoMs", tempoDecorridoMs);

        return detalhes;
    }

    public Boolean verificarStatusValidacao(String hash) {
        Validacao validacao = Validacao.find("hash", digest(hash)).firstResult();

        if (validacao == null) {
            throw new IllegalArgumentException("Hash nao encontrado no banco de dados");
        }

        return validacao.getValidado();
    }

    public Validacao buscarHash(String hash) {
        Validacao validacao = Validacao.find("hash", digest(hash)).firstResult();

        if (validacao == null) {
            throw new IllegalArgumentException("Hash nao encontrado no banco de dados");
        }

        return validacao;
    }
    private String digest(String codigo) {
        if (codigo == null || codigo.isBlank()) throw new IllegalArgumentException("Codigo de validacao e obrigatorio");
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(codigo.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception e) { throw new IllegalStateException("Nao foi possivel processar o codigo"); }
    }
    public List<Validacao> buscarDezUltimosHashs(){
        List<Validacao> lista = Validacao
                .find("""
                    validado = true
                    and dataValidacao is not null
                    ORDER BY dataValidacao DESC
                    """)
                .range(0, 9)
                .list();
            return lista;
    }
}
