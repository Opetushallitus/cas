package fi.vm.sade.auth.ldap;

import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.support.oauth.OAuthConfiguration;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.jasig.cas.support.oauth.authentication.handler.support.OAuthAuthenticationHandler;
import org.jasig.cas.support.oauth.authentication.principal.OAuthCredentials;
import org.scribe.up.profile.UserProfile;
import org.scribe.up.provider.OAuthProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * "Extend" final OAuthAuthenticationHandler to do custom things (import user to
 * ldap) after authentication
 * 
 * @author Antti Salonen
 */
public class CustomOAuthAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    private LdapUserImporter ldapUserImporter;

    @Override
    protected boolean postAuthenticate(Credentials credentials, boolean authenticated) {
        // WARN! credentialssin tulostaminen paljastaa salasanan!!!
        // log.debug("CustomOAuthAuthenticationHandler.postAuthenticate, cred: "+credentials);

        OAuthCredentials cred = (OAuthCredentials) credentials;
        Map<String, Object> attrs = cred.getUserProfile().getAttributes();

        log.info("CustomOAuthAuthenticationHandler.postAuthenticate, token : " + cred.getUserProfile().getAccessToken());
        log.info("CustomOAuthAuthenticationHandler.postAuthenticate, id    : " + cred.getUserProfile().getId());
        log.info("CustomOAuthAuthenticationHandler.postAuthenticate, typeid: " + cred.getUserProfile().getTypedId());
        log.info("CustomOAuthAuthenticationHandler.postAuthenticate, attrs : " + attrs);

        // add user to ldap
        LdapUser user = new LdapUser();
        // user.setUid(attrs.get("username").toString()+"@facebook.com");
        user.setUid(cred.getUserProfile().getTypedId());
        user.setFirstName(attrs.get("first_name").toString());
        user.setLastName(attrs.get("last_name").toString());
        user.setEmail(attrs.get("username").toString() + "@facebook.com");
        // user.setPassword();

        log.info("CustomOAuthAuthenticationHandler.postAuthenticate, save user, uid: " + user.getUid());

        ldapUserImporter.save(user);

        return true;
    }

    public void setLdapUserImporter(LdapUserImporter ldapUserImporter) {
        this.ldapUserImporter = ldapUserImporter;
    }

    // REST IS COPYPASTE FROM CustomOAuthAuthenticationHandler

    private static final Logger logger = LoggerFactory.getLogger(OAuthAuthenticationHandler.class);

    @NotNull
    private OAuthConfiguration configuration;

    @Override
    public boolean supports(final Credentials credentials) {
        return credentials != null && (OAuthCredentials.class.isAssignableFrom(credentials.getClass()));
    }

    @Override
    protected boolean doAuthentication(final Credentials credentials) throws AuthenticationException {
        final OAuthCredentials oauthCredentials = (OAuthCredentials) credentials;
        logger.debug("credential : {}", oauthCredentials);

        final String providerType = oauthCredentials.getCredential().getProviderType();
        logger.debug("providerType : {}", providerType);

        // get provider
        final OAuthProvider provider = OAuthUtils.getProviderByType(this.configuration.getProviders(), providerType);
        logger.debug("provider : {}", provider);

        // get user profile
        final UserProfile userProfile = provider.getUserProfile(oauthCredentials.getCredential());
        logger.debug("userProfile : {}", userProfile);

        if (userProfile != null && StringUtils.isNotBlank(userProfile.getId())) {
            oauthCredentials.setUserProfile(userProfile);
            return true;
        } else {
            return false;
        }
    }

    public void setConfiguration(final OAuthConfiguration configuration) {
        this.configuration = configuration;
    }

}
