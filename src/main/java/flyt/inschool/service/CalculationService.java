package flyt.inschool.service;

import flyt.inschool.api.dto.*;
import flyt.inschool.mutation.ExecutionResult;
import flyt.inschool.mutation.MutationExecutor;
import flyt.inschool.validation.ValidationContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CalculationService {

    @Inject
    MutationExecutor executor;

    public CalculationResponse process(CalculationRequest request) {
        String calculationId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();

        ValidationContext validationContext = new ValidationContext();
        List<CalculationMutation> mutations = request.calculationInstructions().mutations();

        // Execute mutations
        ExecutionResult result = executor.execute(mutations, validationContext);

        Instant endTime = Instant.now();
        long durationMs = ChronoUnit.MILLIS.between(startTime, endTime);

        // Determine outcome
        CalculationOutcome outcome = validationContext.hasCriticalError()
            ? CalculationOutcome.FAILURE
            : CalculationOutcome.SUCCESS;

        // Build metadata
        CalculationMetadata metadata = new CalculationMetadata(
            calculationId,
            request.tenantId(),
            startTime,
            endTime,
            durationMs,
            outcome
        );

        // Build processed mutations list
        List<ProcessedMutation> processedMutations = new ArrayList<>();
        for (ExecutionResult.MutationWithMessages mwm : result.processedMutations()) {
            processedMutations.add(new ProcessedMutation(
                mwm.mutation(),
                null,  // forward_patch (bonus feature)
                null,  // backward_patch (bonus feature)
                mwm.messageIndexes()
            ));
        }

        // Determine end situation details
        int lastMutationIndex = result.processedMutations().size() - 1;
        CalculationMutation lastMutation = result.processedMutations().get(lastMutationIndex).mutation();

        SituationSnapshot endSituation = new SituationSnapshot(
            lastMutation.getMutationId(),
            lastMutationIndex,
            lastMutation.getActualAt(),
            result.endSituation()
        );

        // Build initial situation
        CalculationMutation firstMutation = mutations.get(0);
        SituationSnapshot initialSituation = new SituationSnapshot(
            null,
            null,
            firstMutation.getActualAt(),
            result.initialSituation()
        );

        // Build result
        CalculationResult calculationResult = new CalculationResult(
            validationContext.getMessages(),
            processedMutations,
            endSituation,
            initialSituation
        );

        return new CalculationResponse(metadata, calculationResult);
    }
}
