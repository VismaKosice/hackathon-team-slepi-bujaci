package flyt.inschool.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProcessedMutation(
    @JsonProperty("mutation") CalculationMutation mutation,
    @JsonProperty("forward_patch_to_situation_after_this_mutation") Object forwardPatch,
    @JsonProperty("backward_patch_to_previous_situation") Object backwardPatch,
    @JsonProperty("calculation_message_indexes") List<Integer> calculationMessageIndexes
) {
}
