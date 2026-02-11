package flyt.inschool.mutation;

import flyt.inschool.dto.CalculationResponse.CalculationMessage;
import flyt.inschool.model.Situation;
import java.util.List;

public interface Mutation {
    /**
     * Validates the mutation against the current situation.
     * 
     * @param situation Current situation
     * @param properties Mutation properties
     * @return List of validation messages (CRITICAL or WARNING)
     */
    List<CalculationMessage> validate(Situation situation, java.util.Map<String, Object> properties);

    /**
     * Applies the mutation to the situation.
     * 
     * @param situation Current situation (will be modified)
     * @param properties Mutation properties
     * @return List of messages generated during application (typically WARNINGs for non-critical issues)
     */
    List<CalculationMessage> apply(Situation situation, java.util.Map<String, Object> properties);

    /**
     * Gets the mutation definition name.
     * 
     * @return Mutation definition name
     */
    String getMutationDefinitionName();
}
