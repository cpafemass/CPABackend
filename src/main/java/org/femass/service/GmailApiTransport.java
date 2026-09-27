package org.femass.service;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Small transport seam so the Gmail API integration can be tested without network access. */
@ApplicationScoped
public class GmailApiTransport {
    @Inject
    @ConfigProperty(name = "verificacao-email.gmail.connect-timeout", defaultValue = "PT10S")
    Duration connectTimeout;

    private HttpClient httpClient;

    @PostConstruct
    void init() {
        httpClient = HttpClient.newBuilder().connectTimeout(connectTimeout).build();
    }

    public HttpResponse<String> send(HttpRequest request) throws IOException, InterruptedException {
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
