package flyt.inschool.validation;

import java.time.LocalDate;

public class DateValidator {

    public static boolean isFutureDate(LocalDate date) {
        return date.isAfter(LocalDate.now());
    }

    public static boolean isBefore(LocalDate date1, LocalDate date2) {
        return date1.isBefore(date2);
    }

    public static boolean isValidBirthDate(LocalDate birthDate) {
        return !isFutureDate(birthDate);
    }
}
