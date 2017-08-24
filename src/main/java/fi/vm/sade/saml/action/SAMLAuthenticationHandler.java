package fi.vm.sade.saml.action;

import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;

import fi.vm.sade.AuthenticationUtil;

public class SAMLAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    private AuthenticationUtil authenticationUtil;

    @Override
    public boolean supports(Credentials credentials) {
        return credentials != null && SAMLCredentials.class.equals(credentials.getClass());
    }

    @Override
    protected boolean doAuthentication(Credentials credentials) throws AuthenticationException {
        return true;
    }

    @Override
    protected boolean preAuthenticate(Credentials credentials) {
        
        return authenticationUtil.tryToImportUserFromCustomOphAuthenticationService((SAMLCredentials) credentials);
    }

    public AuthenticationUtil getAuthenticationUtil() {
        return authenticationUtil;
    }

    public void setAuthenticationUtil(AuthenticationUtil authenticationUtil) {
        this.authenticationUtil = authenticationUtil;
    }
}
