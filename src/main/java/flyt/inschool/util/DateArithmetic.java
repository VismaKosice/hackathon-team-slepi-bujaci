package flyt.inschool.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DateArithmetic {

    private static final double DAYS_PER_YEAR = 365.25;

    public static double calculateYearsOfService(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            return 0.0;
        }
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        return days / DAYS_PER_YEAR;
    }
}
