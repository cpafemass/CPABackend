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
import org.femass.service.FormularioCatalogoService;

@ApplicationScoped
@Path("/perguntas")
public class PerguntaResource {

    @Inject
    DadosFormularioService service;

    @Inject
    FormularioCatalogoService catalogoService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscarPerguntas(@QueryParam("campaign") String campaign,
                                    @QueryParam("publico") String publico,
                                    @QueryParam("form") String form,
                                    @QueryParam("version") Integer version) {
        try {
            if (campaign == null || campaign.isBlank() || publico == null || publico.isBlank() || form == null || form.isBlank() || version == null)
                return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponseDTO("Campanha, publico, formulario e versao sao obrigatorios")).build();
            return Response.ok(catalogoService.buscarPerguntas(campaign,
                    org.femass.entity.PublicoAvaliacao.from(publico), form, version)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponseDTO(e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponseDTO("Erro ao buscar perguntas"))
                    .build();
        }
    }
}
