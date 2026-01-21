package it.unimol.taxManager.client.gestioneUtenti.decoder;

import feign.Response;
import feign.codec.ErrorDecoder;
import it.unimol.taxManager.exception.ExternalServiceException;
import org.springframework.http.HttpStatus;

public class GestioneUtentiDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (methodKey.contains("GestioneUtentiClient")) {
            if (response.status() == 400) {
                return new ExternalServiceException(HttpStatus.BAD_REQUEST,"il microservizio GestioneUtenti non ha potuto completare la richiesta (400)");
            } else if (response.status() == 401) {
                return new ExternalServiceException(HttpStatus.UNAUTHORIZED, "il servizio di GestioneUtenti non ha accettato il token JWT (401)");
            } else if (response.status() == 403) {
                return new ExternalServiceException(HttpStatus.FORBIDDEN, "autorizzazioni utente non sufficienti all'interno del microservizio GestioneUtenti (403)");
            } else if (response.status() == 404) {
                return new ExternalServiceException(HttpStatus.NOT_FOUND, "risorsa assente all'interno del microservizio GestioneUtenti (404)");
            }else if (response.status() == 408) {
                return new ExternalServiceException(HttpStatus.REQUEST_TIMEOUT, "il microservizio GestioneUtenti non risponde (408)");
            } else if (response.status() == 500) {
                return new ExternalServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "Errore interno nel microservizio GestioneUtenti (500)");
            } else{
                return new ExternalServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "Errore generico nel microservizio GestioneUtenti (500)");
            }
        }
        return defaultDecoder.decode(methodKey, response);
    }
}