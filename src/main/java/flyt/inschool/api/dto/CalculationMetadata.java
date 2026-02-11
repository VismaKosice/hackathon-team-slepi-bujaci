package flyt.inschool.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record CalculationMetadata(
    @JsonProperty("calculation_id") String calculationId,
    @JsonProperty("tenant_id") String tenantId,
    @JsonProperty("calculation_started_at") Instant calculationStartedAt,
    @JsonProperty("calculation_completed_at") Instant calculationCompletedAt,
    @JsonProperty("calculation_duration_ms") long calculationDurationMs,
    @JsonProperty("calculation_outcome") CalculationOutcome calculationOutcome
) {
}
