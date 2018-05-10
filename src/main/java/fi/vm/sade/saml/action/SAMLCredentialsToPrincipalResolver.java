package fi.vm.sade.saml.action;

import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.principal.DefaultPrincipalFactory;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.PrincipalFactory;
import org.jasig.cas.authentication.principal.PrincipalResolver;

public class SAMLCredentialsToPrincipalResolver implements PrincipalResolver {

    private final PrincipalFactory principalFactory;

    public SAMLCredentialsToPrincipalResolver() {
        this(new DefaultPrincipalFactory());
    }

    public SAMLCredentialsToPrincipalResolver(PrincipalFactory principalFactory) {
        this.principalFactory = principalFactory;
    }

    @Override
    public Principal resolve(Credential credentials) {
        return principalFactory.createPrincipal(((SAMLCredentials)credentials).getUserDetails().getKayttajatiedot().getUsername());
    }

    @Override
    public boolean supports(Credential credentials) {
        return credentials != null & SAMLCredentials.class.equals(credentials.getClass());
    }
}
