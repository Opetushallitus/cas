package fi.vm.sade.saml.action;

import org.apereo.cas.authentication.credential.AbstractCredential;

public class SAMLCredentials extends AbstractCredential {
    private final String token;

    public SAMLCredentials(String token) {
        this.token = token;
    }

    @Override
    public String getId() {
        return token;
    }
}
