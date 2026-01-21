package it.unimol.taxManager.controller.docs;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses(value = {@ApiResponse(responseCode = "401", description = "Accesso negato, JWT Token non valido o scaduto", content = @Content(mediaType = "application/json",
        examples = {
                @ExampleObject(
                        name = "Expired token",
                        value = """
                                {
                                  "status": "error",
                                  "messaggio": "Token JWT scaduto"
                                }
                                """),
                @ExampleObject(
                        name = "Unset token",
                        value = """
                                {
                                   "status": "error",
                                   "messaggio": "Token mancante: il campo 'Authorization'. dell'header deve contenere un token JWT valido."
                                 }
                                """),
                @ExampleObject(
                        name = "Invalid signature",
                        value = """
                                {
                                   "status": "error",
                                   "messaggio": "Firma del token JWT non valida"
                                 }
                                """),
                @ExampleObject(
                        name = "Token malformato",
                        value = """
                                {
                                   "status": "error",
                                   "messaggio": "Token JWT malformatoToken JWT malformato"
                                 }
                                """),
                @ExampleObject(
                        name = "Altro",
                        value = """
                                {
                                   "status": "error",
                                   "messaggio": "Errore nella lettura del token JWT"
                                 }
                                """)})),
        @ApiResponse(responseCode = "403", description = "Accesso negato, utente non abilitato", content = @Content(mediaType = "application/json",
                examples = @ExampleObject(
                        value = """
                                {
                                   "status": "error",
                                   "messaggio": "L'utente non ha i permessi sufficienti."
                                 }
                                """))),
        @ApiResponse(responseCode = "500", description = "Errore interno del server", content = @Content(mediaType = "application/json",
                examples = @ExampleObject(
                        value = """
                                {
                                   "status": "error",
                                   "messaggio": "internal server error"
                                 }
                                """)))
})
public @interface StandardApiResponseErrors {
}
