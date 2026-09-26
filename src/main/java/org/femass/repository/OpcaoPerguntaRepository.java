package org.femass.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.femass.entity.OpcaoPergunta;

@ApplicationScoped
public class OpcaoPerguntaRepository implements PanacheRepositoryBase<OpcaoPergunta, Long> {
    public OpcaoPergunta findByQuestionAndCode(Long perguntaId, String codigo) {
        return find("pergunta.id = ?1 and codigo = ?2", perguntaId, codigo).firstResult();
    }

    public java.util.List<OpcaoPergunta> findByQuestion(Long perguntaId) {
        return find("pergunta.id = ?1 order by ordem", perguntaId).list();
    }
}
