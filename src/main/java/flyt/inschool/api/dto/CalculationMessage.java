package flyt.inschool.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CalculationMessage(
    @JsonProperty("id") int id,
    @JsonProperty("level") MessageLevel level,
    @JsonProperty("code") String code,
    @JsonProperty("message") String message
) {
}
