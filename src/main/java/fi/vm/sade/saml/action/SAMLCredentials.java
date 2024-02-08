package fi.vm.sade.saml.action;

import org.apereo.cas.authentication.Credential;

public class SAMLCredentials implements Credential {

    private final String token;

    public SAMLCredentials(String token) {
        this.token = token;
    }

    @Override
    public String getId() {
        return token;
    }

}
