package flyt.inschool.validation;

public class PolicyValidator {

    public static boolean isValidSalary(double salary) {
        return salary >= 0;
    }

    public static boolean isValidPartTimeFactor(double partTimeFactor) {
        return partTimeFactor >= 0 && partTimeFactor <= 1;
    }
}
