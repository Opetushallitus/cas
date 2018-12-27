package fi.vm.sade.saml.action;

import com.google.gson.Gson;
import fi.vm.sade.auth.dto.IdentifiedHenkiloType;
import fi.vm.sade.javautils.httpclient.OphHttpClient;
import org.jasig.cas.authentication.*;
import org.jasig.cas.authentication.principal.DefaultPrincipalFactory;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.PrincipalFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

public class SAMLAuthenticationHandler implements AuthenticationHandler {

    private static final Logger log = LoggerFactory.getLogger(SAMLAuthenticationHandler.class);

    private final PrincipalFactory principalFactory;
    private final OphHttpClient httpClient;
    private final Gson gson;

    public SAMLAuthenticationHandler(OphHttpClient httpClient) {
        this(new DefaultPrincipalFactory(), httpClient, new Gson());
    }

    public SAMLAuthenticationHandler(PrincipalFactory principalFactory, OphHttpClient httpClient, Gson gson) {
        this.principalFactory = requireNonNull(principalFactory);
        this.httpClient = requireNonNull(httpClient);
        this.gson = requireNonNull(gson);
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

    private HandlerResult doAuthentication(Credential credential) {
        IdentifiedHenkiloType henkiloType = httpClient.get("kayttooikeus-service.cas.henkiloByAuthToken", credential.getId())
                .expectStatus(200)
                .execute(response -> gson.fromJson(response.asText(), IdentifiedHenkiloType.class));
        Principal principal = principalFactory.createPrincipal(henkiloType.getKayttajatiedot().getUsername());
        return new DefaultHandlerResult(this, new BasicCredentialMetaData(credential), principal, emptyList());
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

}
