package org.femass.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "CAMPANHA")
public class Campanha {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false)
    private boolean ativa = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCampanha estado = EstadoCampanha.RASCUNHO;

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }
    public EstadoCampanha getEstado() { return estado; }
    public void setEstado(EstadoCampanha estado) { this.estado = estado; }
    public String getMensagemDisponibilidade() { return mensagemDisponibilidade; }
    public void setMensagemDisponibilidade(String mensagemDisponibilidade) { this.mensagemDisponibilidade = mensagemDisponibilidade; }
}
