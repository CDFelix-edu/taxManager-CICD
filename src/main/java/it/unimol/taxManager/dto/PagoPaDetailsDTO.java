package it.unimol.taxManager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import it.unimol.taxManager.util.TaxStatus;

import java.time.LocalDate;
@Schema(description = "DTO che il servizio PagoPA utilizza per comunicare l'avvenuto pagamento di una tassa da parte di uno studente")
public record PagoPaDetailsDTO(
        @Schema(description = "Codice di avviso PagoPA", example = "000000000000001975")
        String pagoPaNoticeCode,
        @Schema(description = "Stato della tassa")
        TaxStatus status,
        @Schema(description = "Data del pagamento")
        LocalDate paymentDate
) {
}
