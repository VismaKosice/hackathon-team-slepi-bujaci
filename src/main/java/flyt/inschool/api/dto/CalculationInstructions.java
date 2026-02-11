package flyt.inschool.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CalculationInstructions(
    @JsonProperty("mutations") List<CalculationMutation> mutations
) {
}
