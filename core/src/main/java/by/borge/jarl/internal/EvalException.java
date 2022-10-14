package by.borge.jarl.internal;

public class EvalException extends JarlException {
    public EvalException(String type, String message) {
        super(type, message);
    }

    public EvalException(String type, String message, Throwable cause) {
        super(type, message, cause);
    }
}
