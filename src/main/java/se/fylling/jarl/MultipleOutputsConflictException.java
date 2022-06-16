package se.fylling.jarl;

public class MultipleOutputsConflictException extends AssignmentConflictException {
    public MultipleOutputsConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
