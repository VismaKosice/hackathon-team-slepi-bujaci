package flyt.inschool.mutation.impl;

import flyt.inschool.domain.Dossier;
import flyt.inschool.domain.Policy;
import flyt.inschool.domain.Situation;
import flyt.inschool.mutation.MutationContext;
import flyt.inschool.mutation.MutationProcessor;
import flyt.inschool.mutation.MutationResult;
import flyt.inschool.util.PolicyIdGenerator;
import flyt.inschool.validation.MessageCode;
import flyt.inschool.validation.PolicyValidator;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class AddPolicyProcessor implements MutationProcessor {

    @Override
    public MutationResult process(MutationContext context) {
        // Validate: dossier exists
        Dossier dossier = context.currentSituation().dossier();
        if (dossier == null) {
            context.validationContext().addCritical(MessageCode.DOSSIER_NOT_FOUND);
            return new MutationResult(context.currentSituation(), true);
        }

        Map<String, Object> props = context.mutation().getMutationProperties();

        // Extract properties
        String schemeId = (String) props.get("scheme_id");
        LocalDate employmentStartDate = LocalDate.parse((String) props.get("employment_start_date"));
        double salary = ((Number) props.get("salary")).doubleValue();
        double partTimeFactor = ((Number) props.get("part_time_factor")).doubleValue();

        // Validate: salary >= 0
        if (!PolicyValidator.isValidSalary(salary)) {
            context.validationContext().addCritical(MessageCode.INVALID_SALARY);
            return new MutationResult(context.currentSituation(), true);
        }

        // Validate: part_time_factor between 0 and 1
        if (!PolicyValidator.isValidPartTimeFactor(partTimeFactor)) {
            context.validationContext().addCritical(MessageCode.INVALID_PART_TIME_FACTOR);
            return new MutationResult(context.currentSituation(), true);
        }

        // Check for duplicates (scheme_id + employment_start_date)
        Set<String> existingPolicies = new HashSet<>();
        for (Policy policy : dossier.policies()) {
            String key = policy.schemeId() + "|" + policy.employmentStartDate();
            existingPolicies.add(key);
        }

        String newPolicyKey = schemeId + "|" + employmentStartDate;
        if (existingPolicies.contains(newPolicyKey)) {
            context.validationContext().addWarning(MessageCode.DUPLICATE_POLICY);
            // WARNING - continue processing
        }

        // Generate policy_id
        String policyId = PolicyIdGenerator.generate(dossier.dossierId(), dossier.getPolicyCount());

        // Create policy
        Policy newPolicy = new Policy(
            policyId,
            schemeId,
            employmentStartDate,
            salary,
            partTimeFactor,
            null,  // attainable_pension is null initially
            null   // projections is null initially
        );

        // Add policy to dossier
        Dossier updatedDossier = dossier.addPolicy(newPolicy);
        Situation newSituation = context.currentSituation().withDossier(updatedDossier);

        return new MutationResult(newSituation, false);
    }
}
