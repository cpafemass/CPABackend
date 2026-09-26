package org.femass.resource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.femass.dto.ErrorResponseDTO;
import org.femass.dto.QRCodePayloadDTO;
import org.femass.dto.QRCodeResponseDTO;
import org.femass.entity.Validacao;
import org.femass.service.QRCodeService;
import org.femass.service.ValidacaoService;

@ApplicationScoped
@Path("/qrcode")
public class QRCodeResource {

    @Inject
    ValidacaoService validacaoService;

    @Inject
    QRCodeService qrCodeService;

    @POST
    @Path("/gerar")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response gerarCodigoParaQRCode(QRCodePayloadDTO payload) {
        try {
            if (payload == null) {
                throw new IllegalArgumentException("Payload do QR Code e obrigatorio");
            }
            if (!Boolean.TRUE.equals(payload.aceiteTermosCondicoesServico)) {
                throw new IllegalArgumentException("Aceite dos termos e condicoes de servico e obrigatorio");
            }
            String codigo = qrCodeService.criarCodigo();
            Validacao validacao = validacaoService.armazenarCodigoValidacao(codigo, payload.aceiteTermosCondicoesServico);
            return Response.ok(new QRCodeResponseDTO(codigo, codigo)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO(e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponseDTO("Erro ao gerar codigo para QR Code"))
                    .build();
        }
    }

    @POST
    @Path("/decodificar")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response decodificarQRCode(String codigo) {
        try {
            return Response.status(Response.Status.GONE).entity(new ErrorResponseDTO("Codigos sao opacos e nao podem ser decodificados")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO(e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponseDTO("Erro ao decodificar QR Code"))
                    .build();
        }
    }
}
