package flyt.inschool.mutation.impl;

import flyt.inschool.domain.Dossier;
import flyt.inschool.domain.Policy;
import flyt.inschool.domain.Situation;
import flyt.inschool.mutation.MutationContext;
import flyt.inschool.mutation.MutationProcessor;
import flyt.inschool.mutation.MutationResult;
import flyt.inschool.validation.MessageCode;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ApplyIndexationProcessor implements MutationProcessor {

    @Override
    public MutationResult process(MutationContext context) {
        // Validate: dossier exists
        Dossier dossier = context.currentSituation().dossier();
        if (dossier == null) {
            context.validationContext().addCritical(MessageCode.DOSSIER_NOT_FOUND);
            return new MutationResult(context.currentSituation(), true);
        }

        // Validate: has policies
        if (dossier.policies().isEmpty()) {
            context.validationContext().addCritical(MessageCode.NO_POLICIES);
            return new MutationResult(context.currentSituation(), true);
        }

        Map<String, Object> props = context.mutation().getMutationProperties();

        // Extract properties
        double percentage = ((Number) props.get("percentage")).doubleValue();
        String schemeIdFilter = (String) props.get("scheme_id");
        LocalDate effectiveBeforeFilter = props.get("effective_before") != null
            ? LocalDate.parse((String) props.get("effective_before"))
            : null;

        // Filter policies
        List<Policy> updatedPolicies = new ArrayList<>();
        int matchCount = 0;
        boolean hadNegativeSalary = false;

        for (Policy policy : dossier.policies()) {
            boolean matches = true;

            // Apply scheme_id filter
            if (schemeIdFilter != null && !policy.schemeId().equals(schemeIdFilter)) {
                matches = false;
            }

            // Apply effective_before filter
            if (effectiveBeforeFilter != null && !policy.employmentStartDate().isBefore(effectiveBeforeFilter)) {
                matches = false;
            }

            if (matches) {
                matchCount++;
                double newSalary = policy.salary() * (1 + percentage);

                // Clamp negative salaries to 0
                if (newSalary < 0) {
                    newSalary = 0;
                    hadNegativeSalary = true;
                }

                updatedPolicies.add(policy.withSalary(newSalary));
            } else {
                updatedPolicies.add(policy);
            }
        }

        // Warning if no matching policies
        if (matchCount == 0) {
            context.validationContext().addWarning(MessageCode.NO_MATCHING_POLICIES);
        }

        // Warning if any salary was clamped to 0
        if (hadNegativeSalary) {
            context.validationContext().addWarning(MessageCode.NEGATIVE_SALARY_CLAMPED);
        }

        // Update dossier with modified policies
        Dossier updatedDossier = dossier.updatePolicies(updatedPolicies);
        Situation newSituation = context.currentSituation().withDossier(updatedDossier);

        return new MutationResult(newSituation, false);
    }
}
