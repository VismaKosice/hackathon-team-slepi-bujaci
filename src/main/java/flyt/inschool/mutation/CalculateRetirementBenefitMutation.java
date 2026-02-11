package flyt.inschool.mutation;

import flyt.inschool.dto.CalculationResponse.CalculationMessage;
import flyt.inschool.model.Dossier;
import flyt.inschool.model.Person;
import flyt.inschool.model.Policy;
import flyt.inschool.model.Situation;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class CalculateRetirementBenefitMutation extends BaseMutation {

    private static final double DEFAULT_ACCRUAL_RATE = 0.02;

    @Override
    public String getMutationDefinitionName() {
        return "calculate_retirement_benefit";
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

        String retirementDateStr = getStringProperty(properties, "retirement_date");
        LocalDate retirementDate = parseDate(retirementDateStr);

        // Check eligibility
        Person participant = dossier.getPersons().get(0);
        LocalDate birthDate = parseDate(participant.getBirthDate());
        int ageAtRetirement = retirementDate.getYear() - birthDate.getYear();
        
        // Adjust for birthday not yet reached
        if (retirementDate.isBefore(birthDate.withYear(retirementDate.getYear()))) {
            ageAtRetirement--;
        }

        // Calculate total years of service
        double totalYears = 0;
        for (Policy policy : dossier.getPolicies()) {
            double years = calculateYearsOfService(policy.getEmploymentStartDate(), retirementDateStr);
            totalYears += years;
        }

        // Check eligibility: age >= 65 OR total years >= 40
        if (ageAtRetirement < 65 && totalYears < 40) {
            messages.add(createCriticalMessage("NOT_ELIGIBLE", 
                "Participant is under 65 years old on retirement_date AND total years of service < 40"));
            return messages;
        }

        return messages;
    }

    @Override
    public List<CalculationMessage> apply(Situation situation, Map<String, Object> properties) {
        List<CalculationMessage> messages = new ArrayList<>();

        Dossier dossier = situation.getDossier();
        String retirementDateStr = getStringProperty(properties, "retirement_date");
        LocalDate retirementDate = parseDate(retirementDateStr);

        // Calculate years of service and effective salary for each policy
        double totalYears = 0;
        double weightedSum = 0;
        List<Double> yearsPerPolicy = new ArrayList<>();

        for (Policy policy : dossier.getPolicies()) {
            double years = calculateYearsOfService(policy.getEmploymentStartDate(), retirementDateStr);
            
            // Check if retirement is before employment
            if (years == 0 && parseDate(policy.getEmploymentStartDate()).isAfter(retirementDate)) {
                messages.add(createWarningMessage("RETIREMENT_BEFORE_EMPLOYMENT", 
                    "retirement_date is before policy's employment_start_date"));
            }

            double effectiveSalary = policy.getSalary() * policy.getPartTimeFactor();
            
            yearsPerPolicy.add(years);
            totalYears += years;
            weightedSum += effectiveSalary * years;
        }

        // Calculate weighted average salary
        double weightedAvgSalary = totalYears > 0 ? weightedSum / totalYears : 0;

        // Calculate annual pension
        double annualPension = weightedAvgSalary * totalYears * DEFAULT_ACCRUAL_RATE;

        // Distribute pension to each policy
        for (int i = 0; i < dossier.getPolicies().size(); i++) {
            Policy policy = dossier.getPolicies().get(i);
            double policyYears = yearsPerPolicy.get(i);
            
            double policyPension = totalYears > 0 ? 
                annualPension * (policyYears / totalYears) : 0;
            
            policy.setAttainablePension(policyPension);
        }

        // Update dossier status and retirement date
        dossier.setStatus("RETIRED");
        dossier.setRetirementDate(retirementDateStr);

        return messages;
    }
}
