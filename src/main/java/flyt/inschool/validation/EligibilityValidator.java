package flyt.inschool.validation;

import java.time.LocalDate;
import java.time.Period;

public class EligibilityValidator {

    private static final int MIN_AGE = 65;
    private static final double MIN_YEARS_OF_SERVICE = 40.0;

    public static boolean isEligibleForRetirement(LocalDate birthDate, LocalDate retirementDate, double totalYearsOfService) {
        int age = Period.between(birthDate, retirementDate).getYears();
        return age >= MIN_AGE || totalYearsOfService >= MIN_YEARS_OF_SERVICE;
    }
}
