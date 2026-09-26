package org.femass.dto;

import java.util.List;

public class AdminFormularioDTO {
    public String codigo;
    public String nome;
    public String publico;
    public String escopo;
    public Integer ordem;
    public boolean commentAllowed;
    public String commentNotice;
    public List<AdminPerguntaDTO> perguntas;
}
