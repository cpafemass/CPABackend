package org.femass.dto;

import java.util.ArrayList;
import java.util.Date;

/// Esse DTO é baseado no json que o Joshua mandou no zap
public class FormularioDTO {
    public String schemaVersion;
    public String confirmationCode;
    public Date submittedAt;
    public String campaign;
    public String form;
    public Integer formVersion;
    public UsuarioDTO respondent; //Quem respondeu ao formulário
    public CursoDTO course;
    public ArrayList<SubjectDTO> subjects;
    public ArrayList<RespostaDTO> answers;
    public String comment;

}
