package flyt.inschool.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CalculationResponse(
    @JsonProperty("calculation_metadata") CalculationMetadata calculationMetadata,
    @JsonProperty("calculation_result") CalculationResult calculationResult
) {
}
