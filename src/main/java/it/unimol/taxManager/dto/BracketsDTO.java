package it.unimol.taxManager.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO per inserire il valore delle soglie ISEE per il calcolo delle tasse universitarie")
public record BracketsDTO(
        @Schema(description = "Anno accademico per il quale si applicano le soglie", example = "2025")
        int anno,
        @Schema(description = "Intero importo che si dovrebbe pagare per le tasse universitarie", example = "3200.00")
        double importoBase,
        @Schema(description = "Valore isee fino al quale è valida questa fascia", example = "18000.00")
        double soglia1,
        @Schema(description = "Percentuale di sconto applicata alle tasse per questa fascia (100 per no-Tax-Area)", example = "75")
        int sconto1,
        @Schema(description = "Valore isee fino al quale è valida questa fascia", example = "25000.00")
        double soglia2,
        @Schema(description = "Percentuale di sconto applicata alle tasse per questa fascia (100 per no-Tax-Area)", example = "50")
        int sconto2,
        @Schema(description = "Valore isee fino al quale è valida questa fascia", example = "35000.00")
        double soglia3,
        @Schema(description = "Percentuale di sconto applicata alle tasse per questa fascia (100 per no-Tax-Area)", example = "25")
        int sconto3,
        @Schema(description = "Valore isee fino al quale è valida questa fascia", example = "50000.00")
        double soglia4,
        @Schema(description = "Percentuale di sconto applicata alle tasse per questa fascia (100 per no-Tax-Area)", example = "0")
        int sconto4) {
}
