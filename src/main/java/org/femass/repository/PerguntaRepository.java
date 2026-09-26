package org.femass.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.femass.entity.Pergunta;

import java.util.List;

@ApplicationScoped
public class PerguntaRepository implements PanacheRepositoryBase<Pergunta, Long> {

    public Pergunta findByCodigo(String codigo) {
        return find("codigo", codigo).firstResult();
    }

    public Pergunta findByTexto(String texto) {
        return find("texto", texto).firstResult();
    }

    public List<Pergunta> findWithCodigo() {
        return find("codigo is not null").list();
    }
}
