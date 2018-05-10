package fi.vm.sade.saml.action;

import fi.vm.sade.auth.dto.IdentifiedHenkiloType;
import fi.vm.sade.generic.rest.CachingRestClient;
import fi.vm.sade.properties.OphProperties;
import java.security.GeneralSecurityException;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import javax.security.auth.login.FailedLoginException;
import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.BasicCredentialMetaData;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.DefaultHandlerResult;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.principal.DefaultPrincipalFactory;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.PrincipalFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SAMLAuthenticationHandler implements AuthenticationHandler {

    private static final Logger log = LoggerFactory.getLogger(SAMLAuthenticationHandler.class);
    private static final String CLIENT_SUB_SYSTEM_CODE = "authentication.cas";

    private final CachingRestClient restClient = new CachingRestClient().setClientSubSystemCode(CLIENT_SUB_SYSTEM_CODE);
    private final PrincipalFactory principalFactory;
    private final OphProperties ophProperties;

    public SAMLAuthenticationHandler(OphProperties ophProperties) {
        this(new DefaultPrincipalFactory(), ophProperties);
    }

    public SAMLAuthenticationHandler(PrincipalFactory principalFactory, OphProperties ophProperties) {
        this.principalFactory = requireNonNull(principalFactory);
        this.ophProperties = requireNonNull(ophProperties);
    }

    @Override
    public boolean supports(Credential credentials) {
        return credentials != null && SAMLCredentials.class.equals(credentials.getClass());
    }

    @Override
    public HandlerResult authenticate(Credential credential) throws GeneralSecurityException, PreventedException {
        try {
            return doAuthentication(credential);
        } catch (Exception e) {
            log.warn("WARNING - problem with authentication backend, using only ldap.", e);
            FailedLoginException fle = new FailedLoginException();
            fle.initCause(e);
            throw fle;
        }
    }

    private HandlerResult doAuthentication(Credential credential) throws Exception {
        String henkiloByAuthTokenUrl = ophProperties.url("kayttooikeus-service.cas.henkiloByAuthToken", credential.getId());
        IdentifiedHenkiloType henkiloType = restClient.get(henkiloByAuthTokenUrl, IdentifiedHenkiloType.class);
        Principal principal = principalFactory.createPrincipal(henkiloType.getKayttajatiedot().getUsername());
        return new DefaultHandlerResult(this, new BasicCredentialMetaData(credential), principal, emptyList());
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

}
