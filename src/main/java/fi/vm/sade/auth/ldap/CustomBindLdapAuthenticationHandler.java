package fi.vm.sade.auth.ldap;

//*

import fi.vm.sade.AuthenticationUtil;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//*/

/**
 * Extend BindLdapAuthenticationHandler to try to import user data from custom authenticationservice to ldap before
 * authenticating against ldap.
 *
 * @author Antti Salonen
 */
public class CustomBindLdapAuthenticationHandler extends org.jasig.cas.adaptors.ldap.BindLdapAuthenticationHandler {

    private static final long MIN_MS_BETWEEN_USER_IMPORTS = 10 * 1000; // don't import user if it has been imported less than 10 secs ago
    private static Map<String, Long> userImportedTimestamps = SimpleCache.buildCache(1000); // timestamps when user has been imported, max 1000 should be enuf
    private AuthenticationUtil authenticationUtil;

    @Override
    protected boolean preAuthenticate(Credentials credentials) {
        UsernamePasswordCredentials cred = (UsernamePasswordCredentials) credentials;
        Long prevImport = userImportedTimestamps.get(cred.getUsername());
        boolean needsImport = prevImport == null || System.currentTimeMillis() - prevImport > MIN_MS_BETWEEN_USER_IMPORTS;
        log.info("CustomBindLdapAuthenticationHandler.preAuthenticate, user: " + cred.getUsername()+", prevImport: "+prevImport+", needsImport: "+needsImport);
        boolean imported = authenticationUtil.tryToImportUserFromCustomOphAuthenticationService(cred);
        if (imported) { // set previously imported timestamp
            userImportedTimestamps.put(((UsernamePasswordCredentials) credentials).getUsername(), System.currentTimeMillis());
        }
        return imported;
    }

    public AuthenticationUtil getAuthenticationUtil() {
        return authenticationUtil;
    }

    public void setAuthenticationUtil(AuthenticationUtil authenticationUtil) {
        this.authenticationUtil = authenticationUtil;
    }
}
