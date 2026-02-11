package flyt.inschool.mutation;

import flyt.inschool.dto.CalculationResponse.CalculationMessage;
import flyt.inschool.model.Situation;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class BaseMutation implements Mutation {
    
    protected CalculationMessage createCriticalMessage(String code, String message) {
        return new CalculationMessage("CRITICAL", code, message);
    }

    protected CalculationMessage createWarningMessage(String code, String message) {
        return new CalculationMessage("WARNING", code, message);
    }

    protected boolean isNullOrBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    protected boolean isValidDate(String dateStr) {
        if (isNullOrBlank(dateStr)) {
            return false;
        }
        try {
            LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    protected LocalDate parseDate(String dateStr) {
        return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    protected double calculateYearsOfService(String startDateStr, String endDateStr) {
        // Calculate years of service using days / 365.25 as per requirements
        // This accounts for leap years in the calculation
        LocalDate startDate = parseDate(startDateStr);
        LocalDate endDate = parseDate(endDateStr);
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        return Math.max(0, days / 365.25);
    }

    @SuppressWarnings("unchecked")
    protected <T> T getProperty(Map<String, Object> properties, String key, Class<T> type) {
        Object value = properties.get(key);
        if (value == null) {
            return null;
        }
        if (type.isInstance(value)) {
            return (T) value;
        }
        if (type == Double.class && value instanceof Number) {
            return (T) Double.valueOf(((Number) value).doubleValue());
        }
        if (type == Integer.class && value instanceof Number) {
            return (T) Integer.valueOf(((Number) value).intValue());
        }
        return null;
    }

    protected String getStringProperty(Map<String, Object> properties, String key) {
        return getProperty(properties, key, String.class);
    }

    protected Double getDoubleProperty(Map<String, Object> properties, String key) {
        return getProperty(properties, key, Double.class);
    }

    protected Integer getIntegerProperty(Map<String, Object> properties, String key) {
        return getProperty(properties, key, Integer.class);
    }
}
