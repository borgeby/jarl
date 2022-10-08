package by.borge.jarl.internal;

public class BuiltinException extends EvalException {
    public static final String TYPE = "eval_builtin_error";

    public BuiltinException(String message) {
        super(TYPE, message);
    }

    public BuiltinException(String message, Throwable cause) {
        super(TYPE, message, cause);
    }
}
