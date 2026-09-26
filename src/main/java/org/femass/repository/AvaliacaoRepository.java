package org.femass.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.femass.entity.Avaliacao;

@ApplicationScoped
public class AvaliacaoRepository implements PanacheRepositoryBase<Avaliacao, Long> {
}
