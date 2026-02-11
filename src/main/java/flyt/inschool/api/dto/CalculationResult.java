package flyt.inschool.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CalculationResult(
    @JsonProperty("messages") List<CalculationMessage> messages,
    @JsonProperty("mutations") List<ProcessedMutation> mutations,
    @JsonProperty("end_situation") SituationSnapshot endSituation,
    @JsonProperty("initial_situation") SituationSnapshot initialSituation
) {
}
