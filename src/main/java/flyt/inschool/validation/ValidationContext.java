package flyt.inschool.validation;

import flyt.inschool.api.dto.CalculationMessage;
import flyt.inschool.api.dto.MessageLevel;

import java.util.ArrayList;
import java.util.List;

public class ValidationContext {
    private final List<CalculationMessage> messages = new ArrayList<>();
    private boolean hasCriticalError = false;

    public void addCritical(MessageCode code, String message) {
        int id = messages.size();
        messages.add(new CalculationMessage(id, MessageLevel.CRITICAL, code.name(), message));
        hasCriticalError = true;
    }

    public void addCritical(MessageCode code) {
        addCritical(code, code.getDefaultMessage());
    }

    public void addWarning(MessageCode code, String message) {
        int id = messages.size();
        messages.add(new CalculationMessage(id, MessageLevel.WARNING, code.name(), message));
    }

    public void addWarning(MessageCode code) {
        addWarning(code, code.getDefaultMessage());
    }

    public boolean shouldHalt() {
        return hasCriticalError;
    }

    public boolean hasCriticalError() {
        return hasCriticalError;
    }

    public List<CalculationMessage> getMessages() {
        return messages;
    }

    public int getMessageCount() {
        return messages.size();
    }
}
