package org.femass.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;
import org.femass.entity.Validacao;

import java.util.List;

@ApplicationScoped
public class ValidacaoRepository implements PanacheRepositoryBase<Validacao, Long> {

    public Validacao findByCodigoDigest(String digest) {
        return find("codigoDigest", digest).firstResult();
    }

    public Validacao findByCodigoDigestForUpdate(String digest) {
        return find("codigoDigest", digest)
                .withLock(LockModeType.PESSIMISTIC_WRITE)
                .firstResult();
    }

    public List<Validacao> findLastValidated(int limit) {
        return find("validado = true and dataValidacao is not null order by dataValidacao desc")
                .range(0, Math.max(0, limit - 1))
                .list();
    }
}
