package it.unimol.taxManager.client.gestioneUtenti;

import it.unimol.taxManager.dto.LoginRequestDTO;
import it.unimol.taxManager.dto.StudentDTO;
import it.unimol.taxManager.dto.StudentDetailsDTO;
import it.unimol.taxManager.dto.TokenJWTDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "gestione-utenti", url = "${microservizi.gestioneutenti.base-url}", configuration = GestioneUtentiConfig.class)
public interface GestioneUtentiClient {
    @GetMapping("/api/v1/users")
    List<StudentDTO> getStudentList(@RequestHeader("Authorization") String token);

    @GetMapping("/api/v1/users/{id}")
    StudentDetailsDTO getStudentById(@RequestHeader("Authorization") String token, @PathVariable("id") String userId);

    @PostMapping("/api/v1/auth/login")
    TokenJWTDTO login(@RequestBody LoginRequestDTO body);
}
