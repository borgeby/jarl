package se.fylling.jarl;

public class MultipleOutputsConflictException extends ConflictException {
    public MultipleOutputsConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
