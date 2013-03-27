package fi.vm.sade.saml.action;

import org.jasig.cas.authentication.principal.AbstractPersonDirectoryCredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.Credentials;

/**
 * User: tommiha
 * Date: 3/26/13
 * Time: 4:33 PM
 */
public class SAMLCredentialsToPrincipalResolver extends AbstractPersonDirectoryCredentialsToPrincipalResolver {
    @Override
    protected String extractPrincipalId(Credentials credentials) {
        return ((SAMLCredentials)credentials).getUserDetails().getKayttajatunnus();
    }

    @Override
    public boolean supports(Credentials credentials) {
        return credentials != null & SAMLCredentials.class.equals(credentials.getClass());
    }
}
