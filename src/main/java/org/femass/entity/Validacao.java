package org.femass.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "VALIDACAO")
public class Validacao extends PanacheEntityBase {
    @Transient
    private String codigoValidacao;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "CODIGO_DIGEST", length = 64, nullable = false, unique = true)
    private String codigoDigest;

    @Column(name = "VALIDADO", nullable = false)
    private Boolean validado;

    @Column(name = "DATA_CRIACAO", nullable = true)
    private LocalDateTime dataCriacao;

    @Column(name = "EXPIRA_EM", nullable = false)
    private LocalDateTime expiraEm;

    @Column(name = "DATA_VALIDACAO", nullable = true)
    private LocalDateTime dataValidacao;

    @Column(name = "TENTATIVAS_VALIDACAO", nullable = true)
    private Integer tentativasValidacao;

    @Column(name = "ACEITE_TERMOS_CONDICOES_SERVICO", nullable = false)
    private Boolean aceiteTermosCondicoesServico;

    @PrePersist
    public void prePersist() {
        if (this.dataCriacao == null) {
            this.dataCriacao = LocalDateTime.now();
        }
        if (this.validado == null) {
            this.validado = false;
        }
        if (this.tentativasValidacao == null) {
            this.tentativasValidacao = 0;
        }
        if (this.aceiteTermosCondicoesServico == null) {
            this.aceiteTermosCondicoesServico = false;
        }
    }

    public String getCodigoDigest() {
        return codigoDigest;
    }

    public void setCodigoDigest(String codigoDigest) {
        this.codigoDigest = codigoDigest;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoValidacao() { return codigoValidacao; }
    public void setCodigoValidacao(String codigoValidacao) { this.codigoValidacao = codigoValidacao; }

    /** Compatibilidade binária temporária; não deve ser exposto em respostas. */
    @Deprecated
    public String getHash() {
        return codigoDigest;
    }

    @Deprecated
    public void setHash(String hash) {
        this.codigoDigest = hash;
    }

    public Boolean getValidado() {
        return validado;
    }

    public void setValidado(Boolean validado) {
        this.validado = validado;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getExpiraEm() {
        return expiraEm;
    }

    public void setExpiraEm(LocalDateTime expiraEm) {
        this.expiraEm = expiraEm;
    }

    public LocalDateTime getDataValidacao() {
        return dataValidacao;
    }

    public void setDataValidacao(LocalDateTime dataValidacao) {
        this.dataValidacao = dataValidacao;
    }

    public Integer getTentativasValidacao() {
        return tentativasValidacao;
    }

    public void setTentativasValidacao(Integer tentativasValidacao) {
        this.tentativasValidacao = tentativasValidacao;
    }

    public Boolean getAceiteTermosCondicoesServico() {
        return aceiteTermosCondicoesServico;
    }

    public void setAceiteTermosCondicoesServico(Boolean aceiteTermosCondicoesServico) {
        this.aceiteTermosCondicoesServico = aceiteTermosCondicoesServico;
    }
}

