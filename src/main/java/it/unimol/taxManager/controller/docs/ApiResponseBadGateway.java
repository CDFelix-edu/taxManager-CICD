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
@ApiResponse(responseCode = "502", description = "Errore con la comunicazione di un server esterno (altro microservizio)", content = @Content(mediaType = "application/json",
        examples = {
                @ExampleObject(
                        name = "Connection refused",
                        value = """
                                        {
                                          "status": "error",
                                          "messaggio": "ConnectionRefused: Server offline o porta chiusa"
                                        }
                                        """
                ),
                @ExampleObject(
                        name = "Socket timeout",
                        value = """
                                        {
                                          "status": "error",
                                          "messaggio": "SocketTimeout: il microservizio non ha risposto nei tempi previsti"
                                        }
                                        """
                ),
                @ExampleObject(
                        name = "Unknown host",
                        value = """
                                        {
                                          "status": "error",
                                          "messaggio": "UnknownHost: nome host non risolto (DNS non trovato)"
                                        }
                                        """
                ),
                @ExampleObject(
                        name = "No route to host",
                        value = """
                                        {
                                          "status": "error",
                                          "messaggio": "NoRouteToHost: Rete irraggiungibile, errori di routing"
                                        }
                                        """
                ),
                @ExampleObject(
                        name = "SSL handshake error",
                        value = """
                                        {
                                          "status": "error",
                                          "messaggio": "SSLHandShake: errore di Handshake SSL (certificati o protocollo)"
                                        }
                                        """
                ),
                @ExampleObject(
                        name = "Errore generico",
                        value = """
                                        {
                                          "status": "error",
                                          "messaggio": "A causa di un problema risulta impossibile contattare il servizio richiesto. Dettagli: Connection aborted"
                                        }
                                        """
                )
        }))
public @interface ApiResponseBadGateway {}