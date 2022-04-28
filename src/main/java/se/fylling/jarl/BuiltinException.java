package se.fylling.jarl;

public class BuiltinException extends Exception {
    public BuiltinException(String message) {
        super(message);
    }

    public BuiltinException(String message, Throwable cause) {
        super(message, cause);
    }
}
