package flyt.inschool.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CalculationRequest(
    @JsonProperty("tenant_id") String tenantId,
    @JsonProperty("calculation_instructions") CalculationInstructions calculationInstructions
) {
}
