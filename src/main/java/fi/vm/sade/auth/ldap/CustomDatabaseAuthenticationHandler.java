package fi.vm.sade.auth.ldap;

import fi.vm.sade.AuthenticationUtil;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

public class CustomDatabaseAuthenticationHandler extends org.jasig.cas.adaptors.ldap.BindLdapAuthenticationHandler {
   
    private AuthenticationUtil authenticationUtil;

    @Override
    protected boolean preAuthenticate(Credentials credentials) {
        UsernamePasswordCredentials cred = (UsernamePasswordCredentials) credentials;
        log.error("DEPRECATED!! CustomDatabaseAuthenticationHandler.preAuthenticate, returning false by default!!");
        return false;
        // FIXME!! POISTA KOKO LUOKKA, KUN TOIMINNALLISUUS ON VARMISTETTU!!
//        return authenticationUtil.tryAuthenticationWithCustomOphAuthenticationService(cred);
    }  
    
    
    public AuthenticationUtil getAuthenticationUtil() {
        return authenticationUtil;
    }

    public void setAuthenticationUtil(AuthenticationUtil authenticationUtil) {
        this.authenticationUtil = authenticationUtil;
    }

}
