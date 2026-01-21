package it.unimol.taxManager.dto;

import io.swagger.v3.oas.annotations.media.Schema;

//non serve documentare questa classe, è un DTO che comunica con il microservizio analisi e reportistica ma non con gli utenti finali
public record StudentProgressDTO(
        String studentId,
        double progressPercentage) {
}
