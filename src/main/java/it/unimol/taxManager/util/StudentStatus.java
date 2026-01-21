package it.unimol.taxManager.util;

public enum StudentStatus {
    IMMATRICOLATO("Immatricolato"),     //0: Solo immatricolato ma senza tasse da pagare (ancora non vengono generate
    ISCRITTO("Iscritto"),               //1: Dal pagamento della prima rata senza tasse scadute
    NON_ISCRITTO("Non iscritto"),       //2: Se ancora non ha pagato nemmeno ua rata
    CONGELATO("Congelato"),             //3: Regolarmente iscritto ma con tasse scadute
    ATTIVO("Attivo"),                   //4: Tutte le rate pagate
    COMPLETATO("Completato"),           //5: Ha completato il percorso di studi
    FUORI_CORSO("Fuori corso"),         //6: Non so nemmeno se mi serve
    CESSATO("Cessato");                 //7: Rinuncia agli studi

    private final String status;

    StudentStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
