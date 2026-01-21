package it.unimol.taxManager.exception;

public class JwtTokenException extends RuntimeException {

    public enum Reason {
        INVALID, UNAUTHORIZED_ROLE
    }

    private final Reason reason;

    public JwtTokenException(String message, Reason reason) {
        super(message);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}

