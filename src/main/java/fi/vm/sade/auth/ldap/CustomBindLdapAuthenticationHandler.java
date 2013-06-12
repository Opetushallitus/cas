package fi.vm.sade.auth.ldap;

//*

import fi.vm.sade.AuthenticationUtil;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

import java.util.List;

//*/

/**
 * Extend BindLdapAuthenticationHandler to try to import user data from custom authenticationservice to ldap before
 * authenticating against ldap.
 *
 * @author Antti Salonen
 */
public class CustomBindLdapAuthenticationHandler extends org.jasig.cas.adaptors.ldap.BindLdapAuthenticationHandler {

    private AuthenticationUtil authenticationUtil;

    @Override
    protected boolean preAuthenticate(Credentials credentials) {
        log.info("CustomBindLdapAuthenticationHandler.preAuthenticate, credentials: " + credentials.toString());
        UsernamePasswordCredentials cred = (UsernamePasswordCredentials) credentials;
        log.info("CustomBindLdapAuthenticationHandler.preAuthenticate, user: " + cred.getUsername() + ", pass: " + cred.getPassword());
        return authenticationUtil.tryToImportUserFromCustomOphAuthenticationService(cred);

    }

    public AuthenticationUtil getAuthenticationUtil() {
        return authenticationUtil;
    }

    public void setAuthenticationUtil(AuthenticationUtil authenticationUtil) {
        this.authenticationUtil = authenticationUtil;
    }
}
