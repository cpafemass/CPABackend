package org.femass.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.femass.entity.Validacao;

import java.util.List;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.time.Duration;
import java.time.LocalDateTime;

@ApplicationScoped
public class ValidacaoService {

    @Inject
    @ConfigProperty(name = "validacao.codigo-expiracao", defaultValue = "PT336H")
    Duration codigoExpiracao;

    @Transactional
    public Validacao armazenarCodigoValidacao(String codigoValidacao) {
        return armazenarCodigoValidacao(codigoValidacao, false);
    }

    @Transactional
    public Validacao armazenarCodigoValidacao(String codigoValidacao, Boolean aceiteTermosCondicoesServico) {
        if (codigoValidacao == null || codigoValidacao.isBlank()) {
            throw new IllegalArgumentException("Codigo de validacao e obrigatorio");
        }

        String digest = calcularDigest(codigoValidacao);
        Validacao validacaoExistente = Validacao.find("codigoDigest", digest).firstResult();
        if (validacaoExistente != null) {
            return validacaoExistente;
        }

        Validacao validacao = new Validacao();
        validacao.setCodigoDigest(digest);
        validacao.setValidado(false);
        validacao.setExpiraEm(LocalDateTime.now().plus(codigoExpiracao));
        validacao.setAceiteTermosCondicoesServico(Boolean.TRUE.equals(aceiteTermosCondicoesServico));
        validacao.persist();

        return validacao;
    }

    @Transactional
    public Validacao validarCodigo(String codigoValidacao) {
        Validacao validacao = Validacao.find("codigoDigest", calcularDigest(codigoValidacao))
                .withLock(LockModeType.PESSIMISTIC_WRITE).firstResult();

        if (validacao == null) {
            throw new IllegalArgumentException("Codigo nao encontrado");
        }

        if (Boolean.TRUE.equals(validacao.getValidado())) {
            throw new IllegalStateException("Codigo ja foi validado e nao pode ser reutilizado");
        }
        if (validacao.getExpiraEm() != null && validacao.getExpiraEm().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Codigo expirado");
        }

        if (validacao.getTentativasValidacao() == null) {
            validacao.setTentativasValidacao(0);
        }
        validacao.setTentativasValidacao(validacao.getTentativasValidacao() + 1);
        validacao.setValidado(true);
        validacao.setDataValidacao(java.time.LocalDateTime.now());

        return validacao;
    }

    public java.util.Map<String, Object> validarCodigoComDetalhes(String codigoValidacao) {
        Validacao validacao = validarCodigo(codigoValidacao);

        long tempoDecorridoMs = 0;
        if (validacao.getDataCriacao() != null && validacao.getDataValidacao() != null) {
            tempoDecorridoMs = java.time.temporal.ChronoUnit.MILLIS.between(
                    validacao.getDataCriacao(),
                    validacao.getDataValidacao()
            );
        }

        java.util.Map<String, Object> detalhes = new java.util.HashMap<>();
        detalhes.put("codigoValido", true);
        detalhes.put("validado", validacao.getValidado());
        detalhes.put("dataCriacao", validacao.getDataCriacao());
        detalhes.put("dataValidacao", validacao.getDataValidacao());
        detalhes.put("tentativasValidacao", validacao.getTentativasValidacao());
        detalhes.put("tempoDecorridoMs", tempoDecorridoMs);

        return detalhes;
    }

    public Boolean verificarStatusValidacao(String codigoValidacao) {
        Validacao validacao = Validacao.find("codigoDigest", calcularDigest(codigoValidacao)).firstResult();

        if (validacao == null) {
            throw new IllegalArgumentException("Codigo nao encontrado");
        }

        return validacao.getValidado();
    }

    public Validacao buscarCodigo(String codigoValidacao) {
        Validacao validacao = Validacao.find("codigoDigest", calcularDigest(codigoValidacao)).firstResult();

        if (validacao == null) {
            throw new IllegalArgumentException("Codigo nao encontrado");
        }

        return validacao;
    }
    private String calcularDigest(String codigo) {
        if (codigo == null || codigo.isBlank()) throw new IllegalArgumentException("Codigo de validacao e obrigatorio");
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(codigo.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception e) { throw new IllegalStateException("Nao foi possivel processar o codigo"); }
    }
    public List<Validacao> buscarDezUltimosCodigosValidados(){
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
