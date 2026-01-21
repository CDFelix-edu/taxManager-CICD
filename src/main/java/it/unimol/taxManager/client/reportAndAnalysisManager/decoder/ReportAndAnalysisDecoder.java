package it.unimol.taxManager.client.reportAndAnalysisManager.decoder;

import feign.Response;
import feign.codec.ErrorDecoder;
import it.unimol.taxManager.exception.ExternalServiceException;
import org.springframework.http.HttpStatus;

public class ReportAndAnalysisDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (methodKey.contains("ReportAndAnalysisClient")) {
            if (response.status() == 400) {
                return new ExternalServiceException(HttpStatus.BAD_REQUEST, "il microservizio Analis e reportistica non ha potuto completare la richiesta (400)");
            } else if (response.status() == 401) {
                return new ExternalServiceException(HttpStatus.UNAUTHORIZED, "il servizio di Analisi e reportistica non ha accettato il token JWT (401)");
            } else if (response.status() == 403) {
                return new ExternalServiceException(HttpStatus.FORBIDDEN, "autorizzazioni utente non sufficienti all'interno del microservizio Analisi e reportistica (403)");
            } else if (response.status() == 404) {
                return new ExternalServiceException(HttpStatus.NOT_FOUND, "risorsa assente all'interno del microservizio Analisi e reportistica (404)");
            } else if (response.status() == 408) {
                return new ExternalServiceException(HttpStatus.REQUEST_TIMEOUT, "il microservizio Analisi e reportistica non risponde (408)");
            } else if (response.status() == 500) {
                return new ExternalServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "Errore interno nel microservizio Analisi e reportistica (500)");
            } else {
                return new ExternalServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "Errore generico nel microservizio Analisi e reportistica (500)");
            }
        }
        return defaultDecoder.decode(methodKey, response);
    }
}