package se.fylling.jarl;

public class ConflictException extends EvalException {
    public static final String TYPE = "eval_conflict_error";

    public ConflictException(String message) {
        super(TYPE, message);
    }

    public ConflictException(String message, Throwable cause) {
        super(TYPE, message, cause);
    }
}
