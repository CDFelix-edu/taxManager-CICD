package it.unimol.taxManager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import it.unimol.taxManager.util.TaxStatus;

import java.time.LocalDate;

@Schema(description = "DTO per le tasse universitarie degli studenti")
public record TaxDTO(
        @Schema(description = "ID della tassa", example = "001258")
        String id,
        @Schema(description = "ID dello studente", example = "000123")
        String studentId,
        @Schema(description = "Numero del'avviso di pagamento della tassa", example = "1578942687415987362")
        String pagoPaNoticeCode,
        @Schema(description = "Tassa della tassa", example = "480.33")
        double amount,
        @Schema(description = "Stato della tassa", example = "PENDING")
        TaxStatus status,
        @Schema(description = "Data di scadenza della tassa", example = "2025-06-30")
        LocalDate expirationTime,
        @Schema(description = "Data di pagamento della tassa", example = "2025-06-15")
        LocalDate paymentDate,
        @Schema(description = "Data di creazione della tassa", example = "2024-10-01")
        LocalDate creationDate
) {
}
