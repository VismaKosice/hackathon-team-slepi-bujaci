package flyt.inschool.mutation.impl;

import flyt.inschool.domain.Dossier;
import flyt.inschool.domain.Person;
import flyt.inschool.domain.Policy;
import flyt.inschool.domain.Situation;
import flyt.inschool.mutation.MutationContext;
import flyt.inschool.mutation.MutationProcessor;
import flyt.inschool.mutation.MutationResult;
import flyt.inschool.util.DateArithmetic;
import flyt.inschool.validation.EligibilityValidator;
import flyt.inschool.validation.MessageCode;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class CalculateRetirementBenefitProcessor implements MutationProcessor {

    private static final double DEFAULT_ACCRUAL_RATE = 0.02;

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
        LocalDate retirementDate = LocalDate.parse((String) props.get("retirement_date"));

        // Get participant's birth date
        Person participant = dossier.persons().get(0);
        LocalDate birthDate = participant.birthDate();

        // Step 1: Calculate years of service per policy
        List<PolicyCalculation> calculations = new ArrayList<>();
        double totalYears = 0.0;

        for (Policy policy : dossier.policies()) {
            // Check for retirement before employment
            if (retirementDate.isBefore(policy.employmentStartDate())) {
                context.validationContext().addWarning(
                    MessageCode.RETIREMENT_BEFORE_EMPLOYMENT,
                    "Policy " + policy.policyId() + " has retirement date before employment start date"
                );
            }

            double years = DateArithmetic.calculateYearsOfService(policy.employmentStartDate(), retirementDate);
            double effectiveSalary = policy.salary() * policy.partTimeFactor();

            PolicyCalculation calc = new PolicyCalculation(policy, years, effectiveSalary);
            calculations.add(calc);
            totalYears += years;
        }

        // Validate: eligibility (age >= 65 OR total_years >= 40)
        if (!EligibilityValidator.isEligibleForRetirement(birthDate, retirementDate, totalYears)) {
            context.validationContext().addCritical(MessageCode.NOT_ELIGIBLE);
            return new MutationResult(context.currentSituation(), true);
        }

        // Step 2 & 3: Calculate weighted average salary
        double weightedSum = 0.0;
        for (PolicyCalculation calc : calculations) {
            weightedSum += calc.effectiveSalary * calc.years;
        }
        double weightedAvgSalary = totalYears > 0 ? weightedSum / totalYears : 0.0;

        // Step 4: Calculate annual pension
        double annualPension = weightedAvgSalary * totalYears * DEFAULT_ACCRUAL_RATE;

        // Step 5: Distribute pension per policy
        List<Policy> updatedPolicies = new ArrayList<>();
        for (PolicyCalculation calc : calculations) {
            double policyPension = totalYears > 0
                ? annualPension * (calc.years / totalYears)
                : 0.0;

            Policy updatedPolicy = calc.policy.withAttainablePension(policyPension);
            updatedPolicies.add(updatedPolicy);
        }

        // Update dossier: status=RETIRED, set retirement_date, update policies
        Dossier updatedDossier = dossier
            .withRetirement(retirementDate)
            .updatePolicies(updatedPolicies);

        Situation newSituation = context.currentSituation().withDossier(updatedDossier);

        return new MutationResult(newSituation, false);
    }

    private record PolicyCalculation(Policy policy, double years, double effectiveSalary) {}
}
