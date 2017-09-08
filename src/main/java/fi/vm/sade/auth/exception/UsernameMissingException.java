package fi.vm.sade.auth.exception;

public class UsernameMissingException extends RuntimeException {
    public UsernameMissingException(String message) {
        super(message);
    }

    public UsernameMissingException(String message, Throwable cause) {
        super(message, cause);
    }

    public UsernameMissingException(Throwable cause) {
        super(cause);
    }
}
