package it.unimol.taxManager.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO per aggiornare il valore delle soglie ISEE per il calcolo delle tasse universitarie")
public record UpdateBracketsDTO(
        @Schema(description = "Intero importo che si dovrebbe pagare per le tasse universitarie", example = "3200.00")
        Double importoBase,
        @Schema(description = "Valore isee fino al quale è valida questa fascia", example = "18000.00")
        Double soglia1,
        @Schema(description = "Percentuale di sconto applicata alle tasse per questa fascia (100 per no-Tax-Area)", example = "75")
        Integer sconto1,
        @Schema(description = "Valore isee fino al quale è valida questa fascia", example = "25000.00")
        Double soglia2,
        @Schema(description = "Percentuale di sconto applicata alle tasse per questa fascia (100 per no-Tax-Area)", example = "50")
        Integer sconto2,
        @Schema(description = "Valore isee fino al quale è valida questa fascia", example = "35000.00")
        Double soglia3,
        @Schema(description = "Percentuale di sconto applicata alle tasse per questa fascia (100 per no-Tax-Area)", example = "25")
        Integer sconto3,
        @Schema(description = "Valore isee fino al quale è valida questa fascia", example = "50000.00")
        Double soglia4,
        @Schema(description = "Percentuale di sconto applicata alle tasse per questa fascia (100 per no-Tax-Area)", example = "0")
        Integer sconto4) {
}
