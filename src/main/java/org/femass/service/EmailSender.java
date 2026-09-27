package org.femass.service;

public interface EmailSender {
    void enviarPin(String destinatario, String pin);
}
