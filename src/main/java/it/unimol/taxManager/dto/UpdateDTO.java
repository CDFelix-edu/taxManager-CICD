package it.unimol.taxManager.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO usato per la risposta del endpoint di aggiornamento dataBase")
public record UpdateDTO(
        @Schema(description = "Numero di nuovi utenti aggiunti al database")
        int nuoviUtenti) {
}
