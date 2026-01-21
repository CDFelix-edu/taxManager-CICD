package it.unimol.taxManager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;

@Schema(description = "DTO per aggiornare il valore ISEE")
public record IseeUpdateDTO(double isee) {
}
