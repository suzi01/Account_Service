package account.Exceptions;

public class PayrollDoesNotExistException extends RuntimeException {
    public PayrollDoesNotExistException(String message) {
        super(message);
    }
}
