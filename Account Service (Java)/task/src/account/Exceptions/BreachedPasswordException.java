package account.Exceptions;


public class BreachedPasswordException extends RuntimeException {
    public BreachedPasswordException(String message) {
        super(message);
    }
}
