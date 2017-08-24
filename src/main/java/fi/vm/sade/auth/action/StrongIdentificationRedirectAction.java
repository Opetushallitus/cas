package fi.vm.sade.auth.action;

import fi.vm.sade.auth.clients.KayttooikeusRestClient;
import fi.vm.sade.auth.clients.OppijanumerorekisteriRestClient;
import fi.vm.sade.properties.OphProperties;
import org.jasig.cas.authentication.principal.Credentials;
import org.springframework.webflow.execution.RequestContext;

import javax.validation.constraints.NotNull;

public class StrongIdentificationRedirectAction {

    @NotNull
    private KayttooikeusRestClient kayttooikeusClient;

    @NotNull
    private OppijanumerorekisteriRestClient oppijanumerorekisteriClient;

    @NotNull
    private OphProperties ophProperties;

    public String createRedirectUrl(Credentials credentials) throws Exception {
        String oidHenkilo = this.kayttooikeusClient.getHenkiloOid(credentials);
        String loginToken = this.kayttooikeusClient.createLoginToken(oidHenkilo);
        String asiointiKieli = this.oppijanumerorekisteriClient.getAsiointikieli(oidHenkilo);

        return this.ophProperties.url("henkilo-ui.strong-identification", asiointiKieli, loginToken);
    }

    public KayttooikeusRestClient getKayttooikeusClient() {
        return kayttooikeusClient;
    }

    public void setKayttooikeusClient(KayttooikeusRestClient kayttooikeusClient) {
        this.kayttooikeusClient = kayttooikeusClient;
    }

    public OppijanumerorekisteriRestClient getOppijanumerorekisteriClient() {
        return oppijanumerorekisteriClient;
    }

    public void setOppijanumerorekisteriClient(OppijanumerorekisteriRestClient oppijanumerorekisteriClient) {
        this.oppijanumerorekisteriClient = oppijanumerorekisteriClient;
    }

    public OphProperties getOphProperties() {
        return ophProperties;
    }

    public void setOphProperties(OphProperties ophProperties) {
        this.ophProperties = ophProperties;
    }
}
