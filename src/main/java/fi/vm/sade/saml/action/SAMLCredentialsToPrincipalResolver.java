package fi.vm.sade.saml.action;

import org.jasig.cas.authentication.principal.AbstractPersonDirectoryCredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.Credentials;

public class SAMLCredentialsToPrincipalResolver extends AbstractPersonDirectoryCredentialsToPrincipalResolver {
    @Override
    protected String extractPrincipalId(Credentials credentials) {
        return ((SAMLCredentials)credentials).getUserDetails().getKayttajatiedot().getUsername();
    }

    @Override
    public boolean supports(Credentials credentials) {
        return credentials != null & SAMLCredentials.class.equals(credentials.getClass());
    }
}
