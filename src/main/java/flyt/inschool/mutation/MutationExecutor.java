package flyt.inschool.mutation;

import flyt.inschool.api.dto.CalculationMutation;
import flyt.inschool.domain.Situation;
import flyt.inschool.validation.ValidationContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@ApplicationScoped
public class MutationExecutor {

    @Inject
    MutationRegistry registry;

    public ExecutionResult execute(List<CalculationMutation> mutations, ValidationContext validationContext) {
        Situation initialSituation = new Situation(null);
        Situation currentSituation = initialSituation;
        List<ExecutionResult.MutationWithMessages> processedMutations = new ArrayList<>();

        for (int i = 0; i < mutations.size(); i++) {
            CalculationMutation mutation = mutations.get(i);
            int msgIndexBefore = validationContext.getMessageCount();

            MutationProcessor processor = registry.get(mutation.getMutationDefinitionName());
            if (processor == null) {
                throw new IllegalArgumentException("Unknown mutation: " + mutation.getMutationDefinitionName());
            }

            MutationContext context = new MutationContext(
                currentSituation,
                mutation,
                validationContext,
                i
            );

            MutationResult result = processor.process(context);

            int msgIndexAfter = validationContext.getMessageCount();
            List<Integer> messageIndexes = IntStream.range(msgIndexBefore, msgIndexAfter)
                .boxed()
                .toList();

            processedMutations.add(new ExecutionResult.MutationWithMessages(mutation, messageIndexes));

            if (result.shouldHalt()) {
                // CRITICAL error - halt processing, return state BEFORE failed mutation
                return new ExecutionResult(currentSituation, processedMutations, true, initialSituation);
            }

            currentSituation = result.newSituation();
        }

        return new ExecutionResult(currentSituation, processedMutations, false, initialSituation);
    }
}
