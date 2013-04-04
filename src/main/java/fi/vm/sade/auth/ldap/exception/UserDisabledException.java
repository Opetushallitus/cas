package fi.vm.sade.auth.ldap.exception;

/**
 * Will be thrown when user is disabled.
 * User: tommiha
 * Date: 4/4/13
 * Time: 12:32 PM
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
