package org.femass;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.femass.service.EmailDeliveryException;
import org.femass.service.GmailApiEmailSender;
import org.femass.service.GmailApiTransport;
import org.junit.jupiter.api.Test;

import java.net.http.HttpRequest;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import javax.net.ssl.SSLSession;
import java.net.URI;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
class GmailApiEmailSenderTest {
    @Inject
    GmailApiEmailSender sender;

    @InjectMock
    GmailApiTransport transport;

    @Test
    void enviaPinDepoisDeObterAccessToken() throws Exception {
        when(transport.send(any(HttpRequest.class))).thenReturn(
                response(200, "{\"access_token\":\"access-token-test\"}"),
                response(200, "{}"));

        sender.enviarPin("destinatario@femass.edu.br", "PIN-NAO-DEVE-SAIR");
    }

    @Test
    void converteFalhaOAuthEmErroSeguro() throws Exception {
        when(transport.send(any(HttpRequest.class)))
                .thenReturn(response(401, "client_secret=test-client-secret refresh_token=test-refresh-token"));

        EmailDeliveryException exception = assertThrows(EmailDeliveryException.class,
                () -> sender.enviarPin("destinatario@femass.edu.br", "PIN-SECRETO"));

        assertEquals("Nao foi possivel enviar o e-mail de verificacao", exception.getMessage());
    }

    @Test
    void converteQuotaETimeoutEmErroSeguro() throws Exception {
        when(transport.send(any(HttpRequest.class)))
                .thenReturn(response(200, "{\"access_token\":\"access-token-test\"}"), response(429, "quota"));
        assertThrows(EmailDeliveryException.class,
                () -> sender.enviarPin("destinatario@femass.edu.br", "PIN-SECRETO"));

        when(transport.send(any(HttpRequest.class)))
                .thenThrow(new HttpTimeoutException("refresh-token-test timeout"));
        EmailDeliveryException exception = assertThrows(EmailDeliveryException.class,
                () -> sender.enviarPin("destinatario@femass.edu.br", "PIN-SECRETO"));
        assertEquals("Nao foi possivel enviar o e-mail de verificacao", exception.getMessage());
    }

    @Test
    void rejeitaEnderecoComQuebraDeCabecalhoSemChamarGmail() {
        assertThrows(EmailDeliveryException.class,
                () -> sender.enviarPin("destinatario@femass.edu.br\r\nBcc: atacante@example.com", "PIN"));
    }

    private HttpResponse<String> response(int status, String body) {
        return new HttpResponse<>() {
            @Override public int statusCode() { return status; }
            @Override public HttpRequest request() { return null; }
            @Override public Optional<HttpResponse<String>> previousResponse() { return Optional.empty(); }
            @Override public HttpHeaders headers() { return HttpHeaders.of(java.util.Map.of(), (a, b) -> true); }
            @Override public String body() { return body; }
            @Override public Optional<SSLSession> sslSession() { return Optional.empty(); }
            @Override public URI uri() { return URI.create("https://test.invalid"); }
            @Override public HttpClient.Version version() { return HttpClient.Version.HTTP_1_1; }
        };
    }
}
