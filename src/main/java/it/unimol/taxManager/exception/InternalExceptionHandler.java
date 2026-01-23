package it.unimol.taxManager.exception;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class InternalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleExternalError(ResponseStatusException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("messaggio", "Errore " + ex.getStatusCode() + ": " + ex.getReason());
        return ResponseEntity.status(ex.getStatusCode()).contentType(MediaType.APPLICATION_JSON).body(response);
    }
/*
    @ExceptionHandler(RetryableException.class)
    public ResponseEntity<Map<String, Object>> handleFeignConnectionError(RetryableException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        Throwable cause = ex.getCause();
        if (cause instanceof ConnectException) {
            response.put("messaggio", "ConnectionRefused: Server offline o porta chiusa");
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
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
    }*/
}


