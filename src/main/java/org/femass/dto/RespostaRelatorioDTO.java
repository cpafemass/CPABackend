package org.femass.dto;

public class RespostaRelatorioDTO {
    public Long avaliacaoId;
    public String publico;
    public String curso;
    public String disciplina;
    public String professor;
    public String perguntaId;
    public String pergunta;
    public Integer nota;
    public String comentario;

    public RespostaRelatorioDTO(Long avaliacaoId, String publico, String curso,
                                String disciplina, String professor, String perguntaId,
                                String pergunta, Integer nota, String comentario) {
        this.avaliacaoId = avaliacaoId;
        this.publico = publico;
        this.curso = curso;
        this.disciplina = disciplina;
        this.professor = professor;
        this.perguntaId = perguntaId;
        this.pergunta = pergunta;
        this.nota = nota;
        this.comentario = comentario;
    }
}
