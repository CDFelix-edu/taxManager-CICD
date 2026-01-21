package it.unimol.taxManager.dto;

//non serve documentare questa classe, è un DTO che comunica con il microservizio gestione-utenti
// ma non con gli utenti finali (serve per l'aggiornamento del database)
public record StudentDTO(
        String id,              // Matricola (6 cifre)
        String nomeRuolo
) {
}