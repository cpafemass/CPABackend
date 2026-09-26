package org.femass.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.femass.dto.AdminCampanhaDTO;
import org.femass.dto.AdminFormularioDTO;
import org.femass.entity.Campanha;
import org.femass.entity.FormularioVersao;
import org.femass.service.AdministracaoCampanhaService;

@ApplicationScoped
@Path("/admin/campanhas")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("cpa-admin")
public class AdminCampanhaResource {
    @Inject AdministracaoCampanhaService service;

    @POST
    public Response criar(AdminCampanhaDTO dto) {
        try { Campanha campanha = service.criarCampanha(dto.codigo, dto.nome); return Response.status(Response.Status.CREATED).entity(campanha).build(); }
        catch (IllegalArgumentException e) { return erro(e); }
    }

    @POST @Path("/{campaign}/formularios")
    public Response criarFormulario(@PathParam("campaign") String campaign, AdminFormularioDTO dto) {
        try { FormularioVersao versao = service.criarFormulario(campaign, dto); return Response.status(Response.Status.CREATED).entity(versao.getNumero()).build(); }
        catch (IllegalArgumentException e) { return erro(e); }
    }

    @POST @Path("/{campaign}/formularios/{form}/versoes")
    public Response clonar(@PathParam("campaign") String campaign, @PathParam("form") String form) {
        try { FormularioVersao versao = service.clonarVersao(campaign, form); return Response.status(Response.Status.CREATED).entity(versao.getNumero()).build(); }
        catch (IllegalArgumentException e) { return erro(e); }
    }

    @PUT @Path("/{campaign}/formularios/{form}/versoes/{version}")
    public Response atualizar(@PathParam("campaign") String campaign, @PathParam("form") String form,
                              @PathParam("version") Integer version, AdminFormularioDTO dto) {
        try { service.atualizarVersao(campaign, form, version, dto); return Response.noContent().build(); }
        catch (IllegalArgumentException e) { return erro(e); }
    }

    @POST @Path("/{campaign}/formularios/{form}/versoes/{version}/publicar")
    public Response publicar(@PathParam("campaign") String campaign, @PathParam("form") String form, @PathParam("version") Integer version) {
        try { service.publicarVersao(campaign, form, version); return Response.noContent().build(); }
        catch (IllegalArgumentException e) { return erro(e); }
    }

    @POST @Path("/{campaign}/aprovar")
    public Response aprovar(@PathParam("campaign") String campaign) { try { service.aprovar(campaign); return Response.noContent().build(); } catch (IllegalArgumentException e) { return erro(e); } }
    @POST @Path("/{campaign}/abrir")
    public Response abrir(@PathParam("campaign") String campaign) { try { service.abrir(campaign); return Response.noContent().build(); } catch (IllegalArgumentException e) { return erro(e); } }
    @POST @Path("/{campaign}/encerrar")
    public Response encerrar(@PathParam("campaign") String campaign) { try { service.encerrar(campaign); return Response.noContent().build(); } catch (IllegalArgumentException e) { return erro(e); } }

    private Response erro(IllegalArgumentException e) { return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build(); }
}
