package org.femass.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.femass.entity.Formulario;
import org.femass.entity.PublicoAvaliacao;

@ApplicationScoped
public class FormularioRepository implements PanacheRepositoryBase<Formulario, Long> {
    public Formulario findByCampaignAndCode(String campanha, String codigo) {
        return find("campanha.codigo = ?1 and codigo = ?2", campanha, codigo).firstResult();
    }
}
