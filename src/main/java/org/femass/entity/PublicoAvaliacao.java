package org.femass.entity;

import java.text.Normalizer;
import java.util.Locale;

public enum PublicoAvaliacao {
    ALUNO("aluno"),
    PROFESSOR("professor"),
    FUNCIONARIO("funcionario");

    private final String valor;

    PublicoAvaliacao(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static PublicoAvaliacao from(String valor) {
        if (valor == null || valor.isBlank()) {
            return ALUNO;
        }

        String normalizado = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toLowerCase(Locale.ROOT);

        for (PublicoAvaliacao publico : values()) {
            if (publico.valor.equals(normalizado)) {
                return publico;
            }
        }

        throw new IllegalArgumentException(
                "Tipo de respondente invalido. Valores aceitos: aluno, professor ou funcionario");
    }
}
