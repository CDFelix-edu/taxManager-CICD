package it.unimol.taxManager.controller;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.*;
import it.unimol.taxManager.controller.docs.ApiResponseBadRequest;
import it.unimol.taxManager.controller.docs.ApiResponseBadGateway;
import it.unimol.taxManager.dto.IseeUpdateDTO;
import it.unimol.taxManager.dto.BracketsDTO;
import it.unimol.taxManager.dto.UpdateBracketsDTO;
import it.unimol.taxManager.dto.UpdateDTO;
import it.unimol.taxManager.service.AdminService;
import it.unimol.taxManager.controller.docs.StandardApiResponseErrors;
import it.unimol.taxManager.service.NotificationSender;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;
    NotificationSender notificationSender;

    public AdminController(AdminService adminService, NotificationSender notificationSender) {
        this.adminService = adminService;
        this.notificationSender = notificationSender;
    }


    @Operation(summary = "Aggiorna il database studenti", description = "Scarica l'elenco aggiornato dal microservizio Gestione Utenti e Ruoli e aggiorna il database interno.")
    @StandardApiResponseErrors
    @ApiResponse(responseCode = "200", description = "Database aggiornato correttamente", content = @Content(mediaType = "application/json",
            examples = {
                    @ExampleObject(
                            name = "Database già aggiornato",
                            value = """
                                    {
                                      "nuoviUtenti": 0
                                    }
                                    """),
                    @ExampleObject(
                            name = "Database aggiornato con i valori richiesti",
                            value = """
                                    {
                                       "nuoviUtenti": 22
                                     }
                                    """
                    )}
    ))
    @ApiResponseBadGateway
    @PostMapping("/UpdateData")
    public ResponseEntity<UpdateDTO> updateStudentDatabase(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(adminService.updateDatabase(token));
    }


    @Operation(
            summary = "Aggiorna il valore ISEE di uno studente",
            description = "Permette agli utenti amministrativi di modificare il valore ISEE di uno specifico studente",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = false,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "isee": 15340.00
                                            }
                                            """
                            ))))
    @StandardApiResponseErrors
    @ApiResponse(responseCode = "200", description = "ISEE aggiornato correttamente", content = @Content(mediaType = "application/json",
            examples = {
                    @ExampleObject(name = "Success", value = """
                            true
                            """),
                    @ExampleObject(name = "Nessun risultato", value = """
                            false
                            """)}))
    @ApiResponse(responseCode = "404", description = "Studente non trovato", content = @Content(mediaType = "application/json",
            examples = @ExampleObject(value = """
                    {
                      "status": "error",
                      "messaggio": "Errore 404 NOT_FOUND: Studente non trovato"
                    }
                    """)))
    @PutMapping("student/isee/{studentId}")
    public ResponseEntity<Boolean> updateISEE(
            @RequestHeader("Authorization") String token,
            @PathVariable String studentId,
            @RequestBody IseeUpdateDTO body
    ) {
        return ResponseEntity.ok(adminService.updateStudentISEE(token, studentId, body));
    }


    @Operation(summary = "Modifica le soglie ISEE", description = "Permette agli utenti amministrativi di aggiornare le soglie ISEE e l’importo base delle tasse universitarie per un determinato anno accademico.")
    @ApiResponse(responseCode = "200", description = "Soglie ISEE aggiornate correttamente", content = @Content(mediaType = "application/json",
            examples = @ExampleObject(value = """
                    true
                    """)))
    @ApiResponseBadRequest
    @StandardApiResponseErrors
    @PostMapping("/brackets")
    public ResponseEntity<Boolean> updateSoglieIsee(@RequestHeader("Authorization") String token, @RequestBody BracketsDTO soglieDTO) {
        return ResponseEntity.ok(adminService.insertSoglieIsee(token, soglieDTO));
    }

    //TODO: dovrebbe funzionare solo se non sono ancora state generate le tasse?
    @Operation(summary = "Aggiorna le soglie ISEE per un anno specifico", description = "Permette agli utenti amministrativi di aggiornare le soglie ISEE e l’importo base delle tasse universitarie per un anno accademico specifico.")
    @ApiResponse(responseCode = "200", description = "Soglie ISEE aggiornate correttamente", content = @Content(mediaType = "application/json",
            examples = {@ExampleObject(
                    name = "Success",
                    value = """
                              true
                            """), @ExampleObject(
                    name = "Nessuna modifica",
                    value = """
                            false
                            """)}))
    @ApiResponseBadRequest
    @StandardApiResponseErrors
    @PatchMapping("/brackets/{year}")
    public ResponseEntity<Boolean> updateSoglieIseeByYear(@RequestHeader("Authorization") String token, @PathVariable int year, @RequestBody UpdateBracketsDTO soglieDTO) {
        return ResponseEntity.ok(adminService.updateSoglieIsee(token, year, soglieDTO));
    }


    @Operation(summary = "Genera gli avvisi di pagamento per tutti gli studenti", description = "Permette agli utenti amministrativi di generare gli avvisi di pagamento per tutti gli studenti.")
    @ApiResponse(responseCode = "200", description = "Generazione avvenuta", content = @Content(mediaType = "application/json",
            examples = @ExampleObject(
                    value = """
                            {
                              "status": "success",
                              "messaggio": "Soglie ISEE per l'anno 2025 sono state aggiornate correttamente."
                            }
                            """)))
    @ApiResponseBadRequest
    @StandardApiResponseErrors
    @ApiResponseBadGateway
    @PostMapping("generate/{year}")
    public ResponseEntity<Map<String, Object>> generatePaymentNotice(@RequestHeader("Authorization") String token, @PathVariable int year) {
        return ResponseEntity.ok(adminService.generatePaymentNotices(token, year));
    }

    @GetMapping("/test/forcedSendMessage")
    public void forcedSendMessage(@RequestHeader("Authorization") String token) {
        notificationSender.sendNotification();
    }

    

}
