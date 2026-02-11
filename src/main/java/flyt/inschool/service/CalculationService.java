package flyt.inschool.service;

import flyt.inschool.dto.CalculationMutation;
import flyt.inschool.dto.CalculationRequest;
import flyt.inschool.dto.CalculationResponse;
import flyt.inschool.dto.CalculationResponse.*;
import flyt.inschool.model.Situation;
import flyt.inschool.mutation.Mutation;
import flyt.inschool.mutation.MutationRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CalculationService {

    @Inject
    MutationRegistry mutationRegistry;

    public CalculationResponse processCalculationRequest(CalculationRequest request) {
        Instant startTime = Instant.now();
        String calculationId = UUID.randomUUID().toString();

        CalculationResponse response = new CalculationResponse();
        
        // Initialize metadata
        CalculationMetadata metadata = new CalculationMetadata();
        metadata.setCalculationId(calculationId);
        metadata.setTenantId(request.getTenantId());
        metadata.setCalculationStartedAt(startTime.toString());
        response.setCalculationMetadata(metadata);

        // Initialize result
        CalculationResult result = new CalculationResult();
        response.setCalculationResult(result);

        // Process mutations
        List<CalculationMutation> mutations = request.getCalculationInstructions().getMutations();
        Situation currentSituation = new Situation(null);
        
        // Set initial situation
        SituationSnapshot initialSituation = new SituationSnapshot();
        if (!mutations.isEmpty()) {
            initialSituation.setActualAt(mutations.get(0).getActualAt());
        }
        initialSituation.setSituation(cloneSituation(currentSituation));
        result.setInitialSituation(initialSituation);

        boolean failed = false;
        int lastSuccessfulMutationIndex = -1;
        String lastSuccessfulMutationId = null;
        String lastSuccessfulActualAt = null;

        for (int i = 0; i < mutations.size(); i++) {
            CalculationMutation mutation = mutations.get(i);
            
            ProcessedMutation processedMutation = new ProcessedMutation(mutation);
            
            // Get the mutation implementation
            Mutation mutationImpl = mutationRegistry.getMutation(mutation.getMutationDefinitionName());
            
            if (mutationImpl == null) {
                // Unknown mutation type - treat as critical error
                int messageIndex = result.getMessages().size();
                result.getMessages().add(new CalculationMessage("CRITICAL", "UNKNOWN_MUTATION", 
                    "Unknown mutation definition: " + mutation.getMutationDefinitionName()));
                processedMutation.getCalculationMessageIndexes().add(messageIndex);
                result.getMutations().add(processedMutation);
                failed = true;
                break;
            }

            // Validate the mutation
            List<CalculationMessage> validationMessages = mutationImpl.validate(
                currentSituation, mutation.getMutationProperties());

            // Add messages and track indexes
            for (CalculationMessage msg : validationMessages) {
                int messageIndex = result.getMessages().size();
                result.getMessages().add(msg);
                processedMutation.getCalculationMessageIndexes().add(messageIndex);

                if ("CRITICAL".equals(msg.getLevel())) {
                    failed = true;
                }
            }

            // If validation failed with CRITICAL, stop processing
            if (failed) {
                result.getMutations().add(processedMutation);
                break;
            }

            // Apply the mutation
            List<CalculationMessage> applicationMessages = mutationImpl.apply(
                currentSituation, mutation.getMutationProperties());

            // Add application messages
            for (CalculationMessage msg : applicationMessages) {
                int messageIndex = result.getMessages().size();
                result.getMessages().add(msg);
                processedMutation.getCalculationMessageIndexes().add(messageIndex);
            }

            result.getMutations().add(processedMutation);
            
            // Track last successful mutation
            lastSuccessfulMutationIndex = i;
            lastSuccessfulMutationId = mutation.getMutationId();
            lastSuccessfulActualAt = mutation.getActualAt();
        }

        // Set end situation
        SituationSnapshot endSituation = new SituationSnapshot();
        if (lastSuccessfulMutationIndex >= 0) {
            endSituation.setMutationId(lastSuccessfulMutationId);
            endSituation.setMutationIndex(lastSuccessfulMutationIndex);
            endSituation.setActualAt(lastSuccessfulActualAt);
        } else if (!mutations.isEmpty()) {
            // No successful mutation, use first mutation's data
            endSituation.setMutationId(mutations.get(0).getMutationId());
            endSituation.setMutationIndex(0);
            endSituation.setActualAt(mutations.get(0).getActualAt());
        }
        endSituation.setSituation(cloneSituation(currentSituation));
        result.setEndSituation(endSituation);

        // Finalize metadata
        Instant endTime = Instant.now();
        metadata.setCalculationCompletedAt(endTime.toString());
        metadata.setCalculationDurationMs(endTime.toEpochMilli() - startTime.toEpochMilli());
        metadata.setCalculationOutcome(failed ? "FAILURE" : "SUCCESS");

        return response;
    }

    private Situation cloneSituation(Situation original) {
        // For now, we'll use a simple approach - create a new situation
        // In production, we might use deep cloning or a copy library
        Situation cloned = new Situation();
        cloned.setDossier(original.getDossier());
        return cloned;
    }
}
