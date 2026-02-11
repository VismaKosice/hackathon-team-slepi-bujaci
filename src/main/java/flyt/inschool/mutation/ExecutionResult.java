package flyt.inschool.mutation;

import flyt.inschool.api.dto.CalculationMutation;
import flyt.inschool.domain.Situation;

import java.util.List;

public record ExecutionResult(
    Situation endSituation,
    List<MutationWithMessages> processedMutations,
    boolean halted,
    Situation initialSituation
) {
    public record MutationWithMessages(
        CalculationMutation mutation,
        List<Integer> messageIndexes
    ) {}
}
