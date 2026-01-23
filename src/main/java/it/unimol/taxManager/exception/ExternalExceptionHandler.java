package it.unimol.taxManager.exception;

import feign.RetryableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.net.ssl.SSLHandshakeException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ExternalExceptionHandler {

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<Map<String, Object>> handleExternalError(ExternalServiceException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("messaggio", ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).contentType(MediaType.APPLICATION_JSON).body(response);
    }

    @ExceptionHandler(RetryableException.class)
    public ResponseEntity<Map<String, Object>> handleFeignConnectionError(RetryableException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        Throwable cause = ex.getCause();
        if (cause instanceof ConnectException) {
            response.put("messaggio", "ConnectionRefused: Server secondario offline o porta chiusa");
        } else if (cause instanceof SocketTimeoutException) {
            response.put("messaggio", "SocketTimeout: il microservizio non ha risposto nei tempi previsti");
        } else if (cause instanceof UnknownHostException) {
            response.put("messaggio", "UnknownHost: nome host non risolto (DNS non trovato)");
        } else if (cause instanceof NoRouteToHostException) {
            response.put("messaggio", "NoRouteToHost: Rete irraggiungibile, errori di routing");
        } else if (cause instanceof SSLHandshakeException) {
            response.put("messaggio", "SSLHandShake: errore di Handshake SSL (certificati o protocollo)");
        } else {
            response.put("messaggio", "a causa di un problema risulta impossibile contattare il servizio richiesto. Dettagli: " + ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).contentType(MediaType.APPLICATION_JSON).body(response);
    }
}
