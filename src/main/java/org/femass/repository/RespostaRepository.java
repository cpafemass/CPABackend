package org.femass.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.femass.entity.Resposta;

@ApplicationScoped
public class RespostaRepository implements PanacheRepositoryBase<Resposta, Long> {
}
