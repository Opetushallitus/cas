package fi.vm.sade.auth.exception;

import javax.security.auth.login.LoginException;

public class NoStrongIdentificationException extends LoginException {

    public NoStrongIdentificationException() {
    }

    public NoStrongIdentificationException(String msg) {
        super(msg);
    }

}
