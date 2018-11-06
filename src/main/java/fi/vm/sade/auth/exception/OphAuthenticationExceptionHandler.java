package fi.vm.sade.auth.exception;

import java.util.ArrayList;
import java.util.List;
import org.jasig.cas.web.flow.AuthenticationExceptionHandler;

public final class OphAuthenticationExceptionHandler extends AuthenticationExceptionHandler {

    public OphAuthenticationExceptionHandler() {
        List<Class<? extends Exception>> errors = new ArrayList<>(getErrors());
        errors.add(NoStrongIdentificationException.class);
        errors.add(EmailVerificationException.class);
        setErrors(errors);
    }

}
