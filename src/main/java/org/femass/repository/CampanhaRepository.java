package org.femass.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.femass.entity.Campanha;

@ApplicationScoped
public class CampanhaRepository implements PanacheRepositoryBase<Campanha, Long> {
    public Campanha findByCodigo(String codigo) { return find("codigo", codigo).firstResult(); }
}
