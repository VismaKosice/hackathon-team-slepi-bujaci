package flyt.inschool.util;

public class PolicyIdGenerator {

    public static String generate(String dossierId, int policyCount) {
        return dossierId + "-" + (policyCount + 1);
    }
}
