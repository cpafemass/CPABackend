package org.femass.resource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.femass.entity.PublicoAvaliacao;
import org.femass.service.FormularioCatalogoService;

@ApplicationScoped
@Path("/formularios")
@Produces(MediaType.APPLICATION_JSON)
public class FormularioCatalogoResource {
    @Inject FormularioCatalogoService service;

    @GET
    public Response buscar(@QueryParam("campaign") String campaign,
                           @QueryParam("publico") String publico) {
        try {
            if (campaign == null || campaign.isBlank() || publico == null || publico.isBlank())
                return Response.status(Response.Status.BAD_REQUEST).entity("Campanha e publico sao obrigatorios").build();
            return Response.ok(service.buscar(campaign, PublicoAvaliacao.from(publico))).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }
}
