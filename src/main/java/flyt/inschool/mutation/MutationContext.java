package flyt.inschool.mutation;

import flyt.inschool.api.dto.CalculationMutation;
import flyt.inschool.domain.Situation;
import flyt.inschool.validation.ValidationContext;

public record MutationContext(
    Situation currentSituation,
    CalculationMutation mutation,
    ValidationContext validationContext,
    int mutationIndex
) {
}
