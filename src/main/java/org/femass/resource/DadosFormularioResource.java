package org.femass.resource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.femass.dto.ErrorResponseDTO;
import org.femass.service.DadosFormularioService;

@ApplicationScoped
@Path("/dados-formulario")
public class DadosFormularioResource {

    @Inject
    DadosFormularioService service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscarDadosFormulario(@QueryParam("campaign") String campaign,
                                          @QueryParam("publico") String publico) {
        try {
            if (campaign == null || campaign.isBlank() || publico == null || publico.isBlank())
                return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponseDTO("Campanha e publico sao obrigatorios")).build();
            return Response.ok(service.buscarDadosFormulario(campaign, org.femass.entity.PublicoAvaliacao.from(publico))).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponseDTO(e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponseDTO("Erro ao buscar dados do formulario"))
                    .build();
        }
    }
}
