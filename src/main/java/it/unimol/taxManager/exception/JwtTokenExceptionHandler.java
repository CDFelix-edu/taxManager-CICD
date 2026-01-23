package it.unimol.taxManager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@RestControllerAdvice
public class JwtTokenExceptionHandler {

    @ExceptionHandler(JwtTokenException.class)
    public ResponseEntity<Map<String, Object>> handleJwtTokenError(JwtTokenException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("messaggio", ex.getMessage());

        HttpStatus status = switch (ex.getReason()) {
            case INVALID -> HttpStatus.UNAUTHORIZED;
            case UNAUTHORIZED_ROLE -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.BAD_REQUEST;
        };

        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(response);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Map<String, Object>> handleMissingHeader(MissingRequestHeaderException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("messaggio", "Token mancante: il campo '" + ex.getHeaderName() + "' dell'header deve contenere un token JWT valido.");
        return ResponseEntity.status(401).contentType(MediaType.APPLICATION_JSON).body(response);
    }
}
