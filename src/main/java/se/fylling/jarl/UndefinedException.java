package se.fylling.jarl;

public class UndefinedException extends JarlException {
    public UndefinedException(String message) {
        super("", message);
    }
}
