package it.unimol.taxManager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import it.unimol.taxManager.controller.docs.ApiResponseBadGateway;
import it.unimol.taxManager.controller.docs.ApiResponseBadRequest;
import it.unimol.taxManager.controller.docs.StandardApiResponseErrors;
import it.unimol.taxManager.dto.StudentStatusDTO;
import it.unimol.taxManager.dto.TaxDTO;
import it.unimol.taxManager.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/student")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }


    @Operation(summary = "Restituisce la lista delle tasse di uno studente", description = "Recupera tutte le tasse associate a uno studente specifico.")
    @ApiResponse(responseCode = "200", description = "Tasse recuperate con successo",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = TaxDTO.class),
                    examples = @ExampleObject(
                            value = """
                                    [
                                      {
                                        "id": "tax123",
                                        "amount": 100.0,
                                        "dueDate": "2023-12-31",
                                        "status": "PENDING"
                                      },
                                      {
                                        "id": "tax124",
                                        "amount": 150.0,
                                        "dueDate": "2024-01-15",
                                        "status": "PAID"
                                      }
                                    ]
                                    """)))
    @ApiResponseBadRequest
    @ApiResponse(responseCode = "404", description = "Studente non trovato",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "messaggio": "Errore 404 NOT_FOUND: Studente non presente nel sistema",
                                      "status": "error"
                                    }
                                    """)))
    @StandardApiResponseErrors
    @GetMapping("/{studentId}")
    public ResponseEntity<List<TaxDTO>> getStudentById(@RequestHeader("Authorization") String token, @PathVariable String studentId) {
        // Logica per ottenere le informazioni dello studente
        return ResponseEntity.ok(studentService.getStudentTaxes(token, studentId));
    }


    @Operation(summary = "Restituisce lo stato di uno studente", description = "Controlla se lo studente ha tasse in sospeso, scadute o se è in regola.")
    @ApiResponse(responseCode = "200", description = "Stato recuperato con successo",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = StudentStatusDTO.class),
                    examples = {@ExampleObject(
                            name = "Studente in regola",
                            value = """
                                    {
                                    "status": "Attivo",
                                    "inRegola": true
                                    }
                                    """), @ExampleObject(
                            name = "Studente con tasse in sospeso",
                            value = """
                                    {
                                    "status": "Congelato",
                                    "inRegola": false
                                    }
                                    """)}))
    @ApiResponseBadRequest
    @ApiResponse(responseCode = "404", description = "Studente non trovato",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                       "messaggio": "Errore 404 NOT_FOUND: Studente non presente nel sistema",
                                       "status": "error"
                                     }
                                    """)))
    @StandardApiResponseErrors
    @GetMapping("/status/{studentId}")
    public ResponseEntity<StudentStatusDTO> getStudentStatus(@RequestHeader("Authorization") String token, @PathVariable String studentId) {
        // Logica per ottenere lo stato dello studente
        return ResponseEntity.ok(studentService.getStudentStatus(token, studentId));
    }


    @Operation(summary = "Restituisce una specifica tassa", description = "Recupera le informazioni di una specifica tassa fornendole soltanto se inerente allo studente che effettua la richiesta o ad un amministrativo.")
    @ApiResponse(responseCode = "200", description = "Tassa recuperata con successo",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                      {
                                        "id": "tax123",
                                        "amount": 100.0,
                                        "dueDate": "2023-12-31",
                                        "status": "PENDING"
                                      }
                                    """)))
    @ApiResponseBadRequest
    @ApiResponse(responseCode = "404", description = "Studente non trovato",
            content = @Content(mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "Studente non trovato",
                                    value = """
                                            {
                                              "messaggio": "Errore 404 NOT_FOUND: Studente non presente nel sistema",
                                              "status": "error"
                                            }
                                            """),
                            @ExampleObject(name = "Tassa non trovata",
                                    value = """
                                            {
                                              "messaggio": "Errore 404 NOT_FOUND: Tassa non presente nel sistema",
                                              "status": "error"
                                            }
                                            """)}))
    @StandardApiResponseErrors
    @GetMapping("/taxes/{taxId}")
    public ResponseEntity<TaxDTO> getTaxById(@RequestHeader("Authorization") String token, @PathVariable String taxId) {
        return ResponseEntity.ok(studentService.getStudentTax(token, taxId)); // Restituisce la prima tassa come esempio
    }


    @Operation(summary = "Restituisce l'avviso di pagamento in formato PDF di una specifica tassa", description = "Recupera le informazioni di una specifica tassa fornendole soltanto se inerente allo studente che effettua la richiesta o ad un amministrativo.")
    @ApiResponse(responseCode = "200", description = "PDF recuperata con successo",
            content = @Content(mediaType = "application/pdf",
                    examples = @ExampleObject(
                            value = """
                                      PDF DATA
                                    """)))
    @ApiResponseBadRequest
    @ApiResponse(responseCode = "404", description = "Studente non trovato",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "messaggio": "Errore 404 NOT_FOUND: Studente non presente nel sistema",
                                      "status": "error"
                                    }
                                    """)))
    @StandardApiResponseErrors
    @ApiResponseBadGateway
    @GetMapping("/taxes/pdf/{taxId}")
    public ResponseEntity<byte[]> getTaxPdfById(@RequestHeader("Authorization") String token, @PathVariable String taxId) {
        byte[] pdfContent = studentService.getPaymentNotice(token, taxId);
        return ResponseEntity.ok(pdfContent);
    }

}
