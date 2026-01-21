package it.unimol.taxManager.dto;


//non serve documentare questa classe, è un DTO che comunica con il microservizio gestione-utenti ma non con gli utenti finali
public record StudentDetailsDTO(
        String name,
        String surname,
        String id,
        String email

        //FIXME forse possono servire anche
        //,String username,
        //
) {
}
