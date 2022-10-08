package by.borge.jarl.internal;

public class TypeException extends EvalException {
    public static final String TYPE = "eval_type_error";

    public TypeException(String message) {
        super(TYPE, message);
    }

    public TypeException(String message, Throwable cause) {
        super(TYPE, message, cause);
    }
}
