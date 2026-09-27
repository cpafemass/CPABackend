package org.femass.service;

public class EmailDeliveryException extends RuntimeException {
    public EmailDeliveryException() {
        super("Nao foi possivel enviar o e-mail de verificacao");
    }
}
