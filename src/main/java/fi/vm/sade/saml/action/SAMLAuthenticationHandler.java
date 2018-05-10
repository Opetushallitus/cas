package fi.vm.sade.saml.action;

import org.jasig.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;

import fi.vm.sade.AuthenticationUtil;
import static java.util.Collections.emptyList;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.principal.Principal;

public class SAMLAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    private AuthenticationUtil authenticationUtil;

    @Override
    public boolean supports(Credential credentials) {
        return credentials != null && SAMLCredentials.class.equals(credentials.getClass());
    }

    @Override
    protected HandlerResult doAuthentication(Credential credential) {
        Principal principal = null;
        return createHandlerResult(credential, principal, emptyList());
    }

    @Override
    protected boolean preAuthenticate(Credential credential) {
        return authenticationUtil.tryToImportUserFromCustomOphAuthenticationService((SAMLCredentials) credential);
    }

    public AuthenticationUtil getAuthenticationUtil() {
        return authenticationUtil;
    }

    public void setAuthenticationUtil(AuthenticationUtil authenticationUtil) {
        this.authenticationUtil = authenticationUtil;
    }
}
