package flyt.inschool.mutation;

import flyt.inschool.dto.CalculationResponse.CalculationMessage;
import flyt.inschool.model.Dossier;
import flyt.inschool.model.Policy;
import flyt.inschool.model.Situation;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ApplyIndexationMutation extends BaseMutation {

    @Override
    public String getMutationDefinitionName() {
        return "apply_indexation";
    }

    @Override
    public List<CalculationMessage> validate(Situation situation, Map<String, Object> properties) {
        List<CalculationMessage> messages = new ArrayList<>();

        // Check if dossier exists
        if (situation.getDossier() == null) {
            messages.add(createCriticalMessage("DOSSIER_NOT_FOUND", "No dossier in the situation"));
            return messages;
        }

        Dossier dossier = situation.getDossier();

        // Check if policies exist
        if (dossier.getPolicies().isEmpty()) {
            messages.add(createCriticalMessage("NO_POLICIES", "Dossier has no policies"));
            return messages;
        }

        // Check if any policies match the filters
        Double percentage = getDoubleProperty(properties, "percentage");
        String schemeId = getStringProperty(properties, "scheme_id");
        String effectiveBefore = getStringProperty(properties, "effective_before");

        List<Policy> matchingPolicies = filterPolicies(dossier.getPolicies(), schemeId, effectiveBefore);

        if (matchingPolicies.isEmpty() && (schemeId != null || effectiveBefore != null)) {
            messages.add(createWarningMessage("NO_MATCHING_POLICIES", 
                "Filters were provided but no policies match the criteria"));
        }

        return messages;
    }

    @Override
    public List<CalculationMessage> apply(Situation situation, Map<String, Object> properties) {
        List<CalculationMessage> messages = new ArrayList<>();

        Dossier dossier = situation.getDossier();
        Double percentage = getDoubleProperty(properties, "percentage");
        String schemeId = getStringProperty(properties, "scheme_id");
        String effectiveBefore = getStringProperty(properties, "effective_before");

        List<Policy> matchingPolicies = filterPolicies(dossier.getPolicies(), schemeId, effectiveBefore);

        boolean hasNegativeSalary = false;

        for (Policy policy : matchingPolicies) {
            double newSalary = policy.getSalary() * (1 + percentage);
            
            if (newSalary < 0) {
                policy.setSalary(0);
                hasNegativeSalary = true;
            } else {
                policy.setSalary(newSalary);
            }
        }

        if (hasNegativeSalary) {
            messages.add(createWarningMessage("NEGATIVE_SALARY_CLAMPED", 
                "After applying the percentage, one or more salaries would be negative. Salary is clamped to 0."));
        }

        return messages;
    }

    private List<Policy> filterPolicies(List<Policy> allPolicies, String schemeId, String effectiveBefore) {
        List<Policy> filtered = new ArrayList<>();

        for (Policy policy : allPolicies) {
            boolean matches = true;

            // Filter by scheme_id if provided
            if (schemeId != null && !policy.getSchemeId().equals(schemeId)) {
                matches = false;
            }

            // Filter by effective_before if provided
            if (effectiveBefore != null && matches) {
                LocalDate policyStartDate = parseDate(policy.getEmploymentStartDate());
                LocalDate beforeDate = parseDate(effectiveBefore);
                if (!policyStartDate.isBefore(beforeDate)) {
                    matches = false;
                }
            }

            if (matches) {
                filtered.add(policy);
            }
        }

        return filtered;
    }
}
