package flyt.inschool.mutation;

import flyt.inschool.domain.Situation;

public record MutationResult(
    Situation newSituation,
    boolean shouldHalt
) {
}
