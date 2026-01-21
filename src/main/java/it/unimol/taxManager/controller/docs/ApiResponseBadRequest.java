package it.unimol.taxManager.controller.docs;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)

@ApiResponse(responseCode = "400", description = "BAD REQUEST: Dati non conformi", content = @Content(mediaType = "application/json",
        examples = @ExampleObject(
                value = """
                        {
                           "status": "error",
                           "messaggio": "Errore 400 BAD_REQUEST: {messaggio specifico di errore}}"
                         }
                        """)))
public @interface ApiResponseBadRequest {
}
