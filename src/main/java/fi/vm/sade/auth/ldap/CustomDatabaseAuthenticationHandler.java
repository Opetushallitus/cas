package cas.src.main.java.fi.vm.sade.auth.ldap;

public class CustomDatabaseAuthenticationHandler extends org.jasig.cas.adaptors.ldap.BindLdapAuthenticationHandler {
   
    private AuthenticationUtil authenticationUtil;

    @Override
    protected boolean preAuthenticate(Credentials credentials) {
        UsernamePasswordCredentials cred = (UsernamePasswordCredentials) credentials;
        log.info("CustomDatabaseAuthenticationHandler.preAuthenticate, user: " + cred.getUsername());
        authenticationUtil.tryAuthenticationWithCustomOphAuthenticationService(cred);
        
    }  
    
    
    public AuthenticationUtil getAuthenticationUtil() {
        return authenticationUtil;
    }

    public void setAuthenticationUtil(AuthenticationUtil authenticationUtil) {
        this.authenticationUtil = authenticationUtil;
    }

}
