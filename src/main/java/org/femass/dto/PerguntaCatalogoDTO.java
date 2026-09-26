package org.femass.dto;

import java.util.List;

public class PerguntaCatalogoDTO {
    public String id;
    public String texto;
    public Integer ordem;
    public List<OpcaoCatalogoDTO> options;

    public PerguntaCatalogoDTO(String id, String texto, Integer ordem, List<OpcaoCatalogoDTO> options) {
        this.id = id; this.texto = texto; this.ordem = ordem; this.options = options;
    }
}
