package Ecommerce.customExceptions;

public class SessionExpiredException extends Exception {
    public SessionExpiredException(String message) {
        super(message);
    }
}