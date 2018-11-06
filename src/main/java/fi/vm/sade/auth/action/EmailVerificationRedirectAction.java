package fi.vm.sade.auth.action;

import fi.vm.sade.auth.clients.KayttooikeusRestClient;
import fi.vm.sade.properties.OphProperties;
import org.jasig.cas.authentication.Credential;

import javax.validation.constraints.NotNull;


public class EmailVerificationRedirectAction {

    @NotNull
    private KayttooikeusRestClient kayttooikeusRestClient;

    @NotNull
    private OphProperties ophProperties;

    public String createRedirectUrl(Credential credential) throws Exception {
        String oidHenkilo = this.kayttooikeusRestClient.getHenkiloOid(credential.getId());
        String loginToken = this.kayttooikeusRestClient.createLoginToken(oidHenkilo);
        return this.ophProperties.url("kayttooikeus-service.cas.emailverification", loginToken);
    }

    public KayttooikeusRestClient getKayttooikeusClient() {
        return kayttooikeusRestClient;
    }

    public void setKayttooikeusClient(KayttooikeusRestClient kayttooikeusClient) {
        this.kayttooikeusRestClient = kayttooikeusClient;
    }

    public OphProperties getOphProperties() { return ophProperties; }

    public void setOphProperties(OphProperties ophProperties) {
        this.ophProperties = ophProperties;
    }
}
