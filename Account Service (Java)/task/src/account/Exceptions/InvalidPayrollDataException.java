package account.Exceptions;

public class InvalidPayrollDataException extends RuntimeException{
    public InvalidPayrollDataException(String message) {
        super(message);
    }
}
