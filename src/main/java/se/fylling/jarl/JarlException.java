package se.fylling.jarl;

public class JarlException extends Exception {
    private final String type;

    public JarlException(String type, String message) {
        super(message);
        this.type = type;
    }

    public JarlException(String type, String message, Throwable cause) {
        super(message, cause);
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public String getMessage() {
        if (!"".equals(type)) {
            return type + ": " + super.getMessage();
        }
        return super.getMessage();
    }
}
