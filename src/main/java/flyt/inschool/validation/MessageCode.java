package flyt.inschool.validation;

public enum MessageCode {
    // Dossier errors
    DOSSIER_ALREADY_EXISTS("Dossier already exists"),
    DOSSIER_NOT_FOUND("Dossier not found"),

    // Person/Birth date errors
    INVALID_BIRTH_DATE("Invalid birth date - cannot be in the future"),
    INVALID_NAME("Name cannot be empty"),

    // Policy errors
    INVALID_SALARY("Salary must be greater than or equal to 0"),
    INVALID_PART_TIME_FACTOR("Part time factor must be between 0 and 1"),
    DUPLICATE_POLICY("Policy with same scheme_id and employment_start_date already exists"),
    NO_POLICIES("No policies found in dossier"),
    NO_MATCHING_POLICIES("No policies match the filter criteria"),
    NEGATIVE_SALARY_CLAMPED("Salary after indexation was negative and has been clamped to 0"),

    // Retirement/eligibility errors
    NOT_ELIGIBLE("Not eligible for retirement - must be 65+ years old OR have 40+ years of service"),
    RETIREMENT_BEFORE_EMPLOYMENT("Retirement date is before employment start date");

    private final String defaultMessage;

    MessageCode(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
