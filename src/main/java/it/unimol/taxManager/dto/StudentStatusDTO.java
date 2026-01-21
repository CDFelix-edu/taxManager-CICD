package it.unimol.taxManager.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO utilizzato per ottenere lo stato dei pagamenti di uno studente")
public record StudentStatusDTO(
        @Schema(description = "Restituisce lo stato dello studente, da questa lista: " +
                "    - Immatricolato:              (Appena registrato ma senza tasse da pagare (ancora non vengono generate))\n" +
                "    - Iscritto:                   (Pagata prima rata e senza tasse scadute)\n" +
                "    - Non iscritto:               (Nessuna rata pagata)\n" +
                "    - Congelato:                  (Regolarmente iscritto ma con tasse scadute)\n" +
                "    - Attivo:                     (Tutte le rate pagate)\n" +
                "    - Completato:                 (Ha completato il percorso di studi)\n"+
                "    - Cessato:                    (Studi interrotti per rinuncia)")
        String status,
        @Schema(description = "Indica se lo studente è abilitato allo svolgimento degli esami in quanto in regola con tutti i pagamenti")
        boolean inRegola) {
}
