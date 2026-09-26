package org.femass.dto;

public class OpcaoCatalogoDTO {
    public String code;
    public String label;
    public Integer value;
    public boolean naoSeiResponder;

    public OpcaoCatalogoDTO() { }

    public OpcaoCatalogoDTO(String code, String label, Integer value, boolean naoSeiResponder) {
        this.code = code; this.label = label; this.value = value; this.naoSeiResponder = naoSeiResponder;
    }
}
