package fi.vm.sade.auth.ldap;

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
 * HUOM!! Tämä luokka pitää suunnitella uudestaan sitten kun/jos OAuthia halutaan alkaa käyttämään!!
 * 
 * @author Antti Salonen
 */
public class CustomOAuthAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    /**
     * Tämä metodi pitää miettiä kokonaan uusiksi, jos OAuthia aletaan käyttämään! Tämä toteutus
     * pitää muokata sellaiseksi, että se ottaa huomioon LDAPin päivittämisen poistumisen CASilta!
     */
    @Override
    protected boolean postAuthenticate(Credentials credentials, boolean authenticated) {
        return true;
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
        }
        else {
            return false;
        }
    }

    public void setConfiguration(final OAuthConfiguration configuration) {
        this.configuration = configuration;
    }

}
