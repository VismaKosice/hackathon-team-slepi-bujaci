package flyt.inschool.mutation;

import flyt.inschool.dto.CalculationResponse.CalculationMessage;
import flyt.inschool.model.Dossier;
import flyt.inschool.model.Policy;
import flyt.inschool.model.Situation;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class AddPolicyMutation extends BaseMutation {

    @Override
    public String getMutationDefinitionName() {
        return "add_policy";
    }

    @Override
    public List<CalculationMessage> validate(Situation situation, Map<String, Object> properties) {
        List<CalculationMessage> messages = new ArrayList<>();

        // Check if dossier exists
        if (situation.getDossier() == null) {
            messages.add(createCriticalMessage("DOSSIER_NOT_FOUND", "No dossier in the situation"));
            return messages;
        }

        // Validate salary
        Double salary = getDoubleProperty(properties, "salary");
        if (salary == null || salary < 0) {
            messages.add(createCriticalMessage("INVALID_SALARY", "salary < 0"));
            return messages;
        }

        // Validate part_time_factor
        Double partTimeFactor = getDoubleProperty(properties, "part_time_factor");
        if (partTimeFactor == null || partTimeFactor < 0 || partTimeFactor > 1) {
            messages.add(createCriticalMessage("INVALID_PART_TIME_FACTOR", 
                "part_time_factor < 0 or > 1"));
            return messages;
        }

        // Check for duplicate policy
        String schemeId = getStringProperty(properties, "scheme_id");
        String employmentStartDate = getStringProperty(properties, "employment_start_date");

        Dossier dossier = situation.getDossier();
        for (Policy existingPolicy : dossier.getPolicies()) {
            if (existingPolicy.getSchemeId().equals(schemeId) &&
                existingPolicy.getEmploymentStartDate().equals(employmentStartDate)) {
                messages.add(createWarningMessage("DUPLICATE_POLICY", 
                    "A policy with the same scheme_id AND same employment_start_date already exists"));
                break;
            }
        }

        return messages;
    }

    @Override
    public List<CalculationMessage> apply(Situation situation, Map<String, Object> properties) {
        List<CalculationMessage> messages = new ArrayList<>();

        Dossier dossier = situation.getDossier();
        
        String schemeId = getStringProperty(properties, "scheme_id");
        String employmentStartDate = getStringProperty(properties, "employment_start_date");
        Double salary = getDoubleProperty(properties, "salary");
        Double partTimeFactor = getDoubleProperty(properties, "part_time_factor");

        // Generate policy_id: {dossier_id}-{sequence_number}
        int sequenceNumber = dossier.getPolicies().size() + 1;
        String policyId = dossier.getDossierId() + "-" + sequenceNumber;

        Policy policy = new Policy(policyId, schemeId, employmentStartDate, salary, partTimeFactor);
        dossier.getPolicies().add(policy);

        return messages;
    }
}
