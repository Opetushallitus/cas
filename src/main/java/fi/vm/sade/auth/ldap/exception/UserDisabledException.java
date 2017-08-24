package fi.vm.sade.auth.ldap.exception;

/**
 * Will be thrown when user is disabled.
 */
public class UserDisabledException extends RuntimeException {
    public UserDisabledException() {
    }

    public UserDisabledException(String message) {
        super(message);
    }

    public UserDisabledException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserDisabledException(Throwable cause) {
        super(cause);
    }
}
