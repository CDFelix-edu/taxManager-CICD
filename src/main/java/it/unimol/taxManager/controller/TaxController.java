package it.unimol.taxManager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import it.unimol.taxManager.controller.docs.ApiResponseBadRequest;
import it.unimol.taxManager.controller.docs.StandardApiResponseErrors;
import it.unimol.taxManager.dto.PagoPaDetailsDTO;
import it.unimol.taxManager.service.TaxService;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tax")
public class TaxController {
    TaxService taxService;

    public TaxController(TaxService taxService) {
        this.taxService = taxService;
    }


    @Operation(summary = "Endpoint utilizzato dal servizio pagoPA per comunicare l'avvenuto pagamento di una tassa",
            description = "Ricezione della notifica di pagamento da parte del servizio pagoPA.")
    @ApiResponse(responseCode = "200", description = "Pagamento tassa registrato", content = @Content(
            examples = @ExampleObject(
            value = """
                    {
                      "message": "Pagamento tassa registrato con successo"
                    }
                    """)))
    @ApiResponseBadRequest
    @ApiResponse(responseCode = "404", description = "Tassa non trovata",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "message": "Risorsa non trovata"
                                    }
                                    """)))
    @StandardApiResponseErrors
    @PostMapping("/PagoPa/registerTax")
    public ResponseEntity<String> registerTax(@RequestHeader("Authorization") String token, @RequestBody PagoPaDetailsDTO pagoPADetails) {
        taxService.registerPagoPaTax(token, pagoPADetails);
        return ResponseEntity.ok("Codice avviso pagoPA generato con successo");
    }
}
