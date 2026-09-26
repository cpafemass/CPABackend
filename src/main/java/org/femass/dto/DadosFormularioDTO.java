package org.femass.dto;

import java.util.List;

public class DadosFormularioDTO {
    public List<CursoFormularioDTO> cursos;
    public List<PerguntaFormularioDTO> perguntas;
    public List<FormularioCatalogoDTO> formularios;

    public DadosFormularioDTO() {
    }

    public DadosFormularioDTO(List<CursoFormularioDTO> cursos, List<PerguntaFormularioDTO> perguntas) {
        this.cursos = cursos;
        this.perguntas = perguntas;
    }

    public DadosFormularioDTO(List<CursoFormularioDTO> cursos, List<FormularioCatalogoDTO> formularios, boolean versionado) {
        this.cursos = cursos;
        this.formularios = formularios;
        this.perguntas = List.of();
    }
}
