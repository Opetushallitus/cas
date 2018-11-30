package fi.vm.sade.auth.exception;

import javax.security.auth.login.LoginException;

public class EmailVerificationException extends LoginException {

    public EmailVerificationException() {}
    public EmailVerificationException(String msg) {
        super(msg);
    }

}
