package org.femass.resource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.femass.dto.*;
import org.femass.entity.Validacao;
import org.femass.service.QRCodeService;
import org.femass.service.ValidacaoService;

import java.util.List;

@ApplicationScoped
@Path("/validacao")
public class ValidacaoResource {

    @Inject
    ValidacaoService service;

    @Inject
    QRCodeService qrCodeService;

    @POST
    @Path("/armazenar-codigo")
    @Produces(MediaType.APPLICATION_JSON)
    public Response armazenarCodigo(String codigo) {
        Validacao validacao = service.armazenarCodigoValidacao(codigo);
        ValidacaoResponseDTO response = new ValidacaoResponseDTO(
            "Codigo recebido com sucesso!",
            validacao.getHash(),
            validacao.getHash(),
            codigo
        );
        return Response.ok(response, MediaType.APPLICATION_JSON).build();
    }

    @PUT
    @Path("/validar-hash")
    @Produces(MediaType.APPLICATION_JSON)
    public Response validarHash(@QueryParam("hash") String hash) {
        if (hash == null || hash.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponseDTO("Hash é obrigatório como parâmetro de query"))
                .type(MediaType.APPLICATION_JSON)
                .build();
        }


        try {
            Validacao validacao = service.validarHash(hash);
            return Response.ok(new ValidacaoStatusDTO("codigo", true, "VALIDADO", "Codigo validado com sucesso"), MediaType.APPLICATION_JSON).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponseDTO(e.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponseDTO(e.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponseDTO("Erro ao validar hash: " + e.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
        }
    }

    @PUT
    @Path("/validar-hash/detalhado")
    @Produces(MediaType.APPLICATION_JSON)
    public Response validarHashDetalhado(@QueryParam("hash") String hash) {
        if (hash == null || hash.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO("Hash é obrigatório como parâmetro de query"))
                    .build();
        }

        try {
            Validacao validacao = service.validarHash(hash);
            long tempoDecorrido = java.time.temporal.ChronoUnit.MILLIS.between(
                validacao.getDataCriacao(),
                validacao.getDataValidacao()
            );

            ValidacaoDetailResponseDTO response = new ValidacaoDetailResponseDTO(
                true,
                "codigo",
                "codigo",
                validacao.getValidado(),
                validacao.getDataCriacao(),
                validacao.getDataValidacao(),
                validacao.getTentativasValidacao(),
                tempoDecorrido
            );
            return Response.ok(response, MediaType.APPLICATION_JSON).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponseDTO(e.getMessage()))
                    .type(MediaType.APPLICATION_JSON)
                .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponseDTO(e.getMessage()))
                    .type(MediaType.APPLICATION_JSON)
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponseDTO("Erro ao validar hash: " + e.getMessage()))
                    .type(MediaType.APPLICATION_JSON)
                .build();
        }
    }

    @GET
    @Path("/verificar-status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response verificarStatus(@QueryParam("hash") String hash) {
        if (hash == null || hash.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponseDTO("Hash é obrigatório como parâmetro de query"))
                .build();
        }

        try {
            Boolean validado = service.verificarStatusValidacao(hash);
            String status = validado ? "VALIDADO" : "PENDENTE";
            String mensagem = validado
                ? "Este hash foi validado com sucesso"
                : "Este hash ainda está pendente de validação";

            ValidacaoStatusDTO response = new ValidacaoStatusDTO(hash, validado, status, mensagem);
            return Response.ok(response, MediaType.APPLICATION_JSON).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponseDTO(e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponseDTO("Erro ao verificar status: " + e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/buscar-hash")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscarHash(@QueryParam("hash") String hash) {
        if (hash == null || hash.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponseDTO("Hash é obrigatório como parâmetro de query"))
                .build();
        }

        try {
            Validacao validacao = service.buscarHash(hash);
            ValidacaoResponseDTO response = new ValidacaoResponseDTO(
                "Hash encontrado com sucesso!",
                validacao.getHash(),
                validacao.getHash(),
                validacao.getValidado() ? "validado" : "pendente"
            );
            return Response.ok().entity(response).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponseDTO(e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponseDTO("Erro ao buscar hash: " + e.getMessage()))
                .build();
        }
    }
    @GET
    @Path("historico")
    @Produces(MediaType.APPLICATION_JSON)
    public Response historico() {
       try{
           List<Validacao> historico = service.buscarDezUltimosHashs();
           return Response.ok(historico.stream().map(v -> new ValidacaoStatusDTO("codigo", true, "VALIDADO", "Codigo validado")).toList(), MediaType.APPLICATION_JSON).build();
       }catch (IllegalArgumentException e) {
           return Response.status(Response.Status.NOT_FOUND)
                   .entity(new ErrorResponseDTO(e.getMessage()))
                   .build();
       } catch (Exception e) {
           return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                   .entity(new ErrorResponseDTO("Erro ao buscar historico: " + e.getMessage()))
                   .build();
       }

    }


}
