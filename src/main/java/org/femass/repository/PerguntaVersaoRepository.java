package org.femass.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.femass.entity.PerguntaVersao;

@ApplicationScoped
public class PerguntaVersaoRepository implements PanacheRepositoryBase<PerguntaVersao, Long> {
    public PerguntaVersao findByVersionAndCode(Long versionId, String codigo) {
        return find("formularioVersao.id = ?1 and codigo = ?2", versionId, codigo).firstResult();
    }

    public java.util.List<PerguntaVersao> findByVersion(Long versionId) {
        return find("formularioVersao.id = ?1 order by ordem", versionId).list();
    }
}
