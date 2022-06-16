package se.fylling.jarl;

public class AssignmentConflictException extends ConflictException {
    public AssignmentConflictException(String message) {
        super(message);
    }

    public AssignmentConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
