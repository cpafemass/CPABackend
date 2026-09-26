package org.femass.resource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.femass.dto.ErrorResponseDTO;
import org.femass.entity.PublicoAvaliacao;
import org.femass.service.AvaliacaoService;

@ApplicationScoped
@Path("/avaliacoes")
public class AvaliacaoResource {

    @Inject
    AvaliacaoService service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscarRespostas(@QueryParam("publico") @DefaultValue("") String publico) {
        try {
            return Response.ok(service.buscarRespostas(PublicoAvaliacao.from(publico))).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO(e.getMessage()))
                    .build();
        }
    }
}
