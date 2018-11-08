package fi.vm.sade.auth.action;

import fi.vm.sade.auth.clients.KayttooikeusRestClient;
import fi.vm.sade.auth.clients.OppijanumerorekisteriRestClient;
import fi.vm.sade.properties.OphProperties;
import org.jasig.cas.authentication.Credential;

import javax.validation.constraints.NotNull;


public class EmailVerificationRedirectAction {

    @NotNull
    private KayttooikeusRestClient kayttooikeusRestClient;

    @NotNull
    private OppijanumerorekisteriRestClient oppijanumerorekisteriRestClient;

    @NotNull
    private OphProperties ophProperties;

    public String createRedirectUrl(Credential credential) throws Exception {
        String oidHenkilo = this.kayttooikeusRestClient.getHenkiloOid(credential.getId());
        String loginToken = this.kayttooikeusRestClient.createLoginToken(oidHenkilo);
        String asiointiKieli = this.oppijanumerorekisteriRestClient.getAsiointikieli(oidHenkilo);
        return this.ophProperties.url("henkilo-ui.email-verification", asiointiKieli, loginToken);
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

    public OppijanumerorekisteriRestClient getOppijanumerorekisteriClient() {
        return oppijanumerorekisteriRestClient;
    }

    public void setOppijanumerorekisteriClient(OppijanumerorekisteriRestClient oppijanumerorekisteriClient) {
        this.oppijanumerorekisteriRestClient = oppijanumerorekisteriClient;
    }
}
