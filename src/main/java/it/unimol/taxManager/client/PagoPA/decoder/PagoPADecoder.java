package it.unimol.taxManager.client.PagoPA.decoder;

import feign.Response;
import feign.codec.ErrorDecoder;
import it.unimol.taxManager.exception.ExternalServiceException;
import org.springframework.http.HttpStatus;

public class PagoPADecoder implements ErrorDecoder {
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (methodKey.contains("PagoPAClient")) {
            if (response.status() == 400) {
                return new ExternalServiceException(HttpStatus.BAD_REQUEST, "il microservizio PagoPA non ha potuto completare la richiesta (400)");
            } else if (response.status() == 401) {
                return new ExternalServiceException(HttpStatus.UNAUTHORIZED, "il servizio di PagoPA non ha accettato il token JWT (401)");
            } else if (response.status() == 403) {
                return new ExternalServiceException(HttpStatus.FORBIDDEN, "autorizzazioni utente non sufficienti all'interno del microservizio PagoPA (403)");
            } else if (response.status() == 404) {
                return new ExternalServiceException(HttpStatus.NOT_FOUND, "risorsa assente all'interno del microservizio PagoPA (404)");
            } else if (response.status() == 408) {
                return new ExternalServiceException(HttpStatus.REQUEST_TIMEOUT, "il microservizio PagoPA non risponde (408)");
            } else if (response.status() == 500) {
                return new ExternalServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "Errore interno nel microservizio PagoPA (500)");
            } else {
                return new ExternalServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "Errore generico nel microservizio PagoPA (500)");
            }
        }
        return defaultDecoder.decode(methodKey, response);
    }

}
