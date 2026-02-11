package flyt.inschool.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import flyt.inschool.domain.Situation;

import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SituationSnapshot(
    @JsonProperty("mutation_id") String mutationId,
    @JsonProperty("mutation_index") Integer mutationIndex,
    @JsonProperty("actual_at") LocalDate actualAt,
    @JsonProperty("situation") Situation situation
) {
}
