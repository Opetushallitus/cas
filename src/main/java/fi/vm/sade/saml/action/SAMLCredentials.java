package fi.vm.sade.saml.action;

import fi.vm.sade.authentication.service.types.IdentifiedHenkiloType;
import org.jasig.cas.authentication.principal.Credentials;

/**
 * User: tommiha
 * Date: 3/26/13
 * Time: 2:01 PM
 */
public class SAMLCredentials implements Credentials {

    private String token;
    private IdentifiedHenkiloType userDetails;

    public SAMLCredentials(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public IdentifiedHenkiloType getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(IdentifiedHenkiloType userDetails) {
        this.userDetails = userDetails;
    }
}
