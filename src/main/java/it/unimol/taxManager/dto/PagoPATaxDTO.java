package it.unimol.taxManager.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

//non serve documentare questa classe, è un DTO che comunica con il microservizio PagoPA ma non con gli utenti finali
public record PagoPATaxDTO(
        String id,
        String studentSurname,
        String studentName,
        String ente,
        double taxAmount,
        LocalDate expirationTime) {
}
