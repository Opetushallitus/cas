package fi.vm.sade.auth.exception;

import org.jasig.cas.authentication.handler.AuthenticationException;

public class NoStrongIdentificationException extends AuthenticationException {

    public NoStrongIdentificationException(String code) {
        super(code);
    }

    public NoStrongIdentificationException(String code, String message, String type) {
        super(code, message, type);
    }
}
