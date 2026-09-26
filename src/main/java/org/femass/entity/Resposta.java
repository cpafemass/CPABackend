package org.femass.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

@Entity

@Table(name = "RESPOSTA")
public class Resposta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "avaliacao_id", nullable = false)
    private Avaliacao avaliacao; // Avaliação associada à resposta

    @ManyToOne
    @JoinColumn(name = "pergunta_id")
    private Pergunta pergunta; // Pergunta legada

    @ManyToOne
    @JoinColumn(name = "pergunta_versao_id")
    private PerguntaVersao perguntaVersao;

    @Column(nullable = true)
    private Integer nota; // Resposta à pergunta (1 a 5)

    @Column(name = "OPCAO_CODIGO", length = 30)
    private String opcaoCodigo;

    @Column(name = "OPCAO_ROTULO", length = 100)
    private String opcaoRotulo;

    @Column(name = "NAO_SEI_RESPONDER", nullable = false)
    private boolean naoSeiResponder;

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Avaliacao getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(Avaliacao avaliacao) {
        this.avaliacao = avaliacao;
    }

    public Pergunta getPergunta() {
        return pergunta;
    }

    public void setPergunta(Pergunta pergunta) {
        this.pergunta = pergunta;
    }

    public PerguntaVersao getPerguntaVersao() { return perguntaVersao; }
    public void setPerguntaVersao(PerguntaVersao perguntaVersao) { this.perguntaVersao = perguntaVersao; }

    public Integer getNota() {
        return nota;
    }

    public void setNota(Integer nota) {
        this.nota = nota;
    }

    public String getOpcaoCodigo() { return opcaoCodigo; }
    public void setOpcaoCodigo(String opcaoCodigo) { this.opcaoCodigo = opcaoCodigo; }
    public String getOpcaoRotulo() { return opcaoRotulo; }
    public void setOpcaoRotulo(String opcaoRotulo) { this.opcaoRotulo = opcaoRotulo; }
    public boolean isNaoSeiResponder() { return naoSeiResponder; }
    public void setNaoSeiResponder(boolean naoSeiResponder) { this.naoSeiResponder = naoSeiResponder; }
}
