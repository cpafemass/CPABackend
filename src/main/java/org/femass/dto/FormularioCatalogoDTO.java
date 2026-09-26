package org.femass.dto;

import java.util.List;

public class FormularioCatalogoDTO {
    public String campaign;
    public String code;
    public String name;
    public String audience;
    public Integer version;
    public Integer order;
    public String scope;
    public boolean commentAllowed;
    public String commentNotice;
    public List<PerguntaCatalogoDTO> questions;

    public FormularioCatalogoDTO(String campaign, String code, String name, String audience, Integer version,
                                 Integer order, String scope, boolean commentAllowed, String commentNotice,
                                 List<PerguntaCatalogoDTO> questions) {
        this.campaign = campaign; this.code = code; this.name = name; this.audience = audience;
        this.version = version; this.order = order; this.scope = scope;
        this.commentAllowed = commentAllowed; this.commentNotice = commentNotice; this.questions = questions;
    }
}
