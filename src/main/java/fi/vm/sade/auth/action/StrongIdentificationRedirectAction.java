package fi.vm.sade.auth.action;

import fi.vm.sade.auth.clients.KayttooikeusRestClient;
import fi.vm.sade.auth.clients.OppijanumerorekisteriRestClient;
import fi.vm.sade.properties.OphProperties;
import org.springframework.stereotype.Component;

@Component
public class StrongIdentificationRedirectAction {

    private final KayttooikeusRestClient kayttooikeusClient;
    private final OppijanumerorekisteriRestClient oppijanumerorekisteriClient;
    private final OphProperties ophProperties;

    public StrongIdentificationRedirectAction(KayttooikeusRestClient kayttooikeusClient,
                                              OppijanumerorekisteriRestClient oppijanumerorekisteriClient,
                                              OphProperties ophProperties) {
        this.kayttooikeusClient = kayttooikeusClient;
        this.oppijanumerorekisteriClient = oppijanumerorekisteriClient;
        this.ophProperties = ophProperties;
    }

    public String createRedirectUrl(String username) {
        String oidHenkilo = this.kayttooikeusClient.getHenkiloOid(username);
        String loginToken = this.kayttooikeusClient.createLoginToken(oidHenkilo);
        String asiointiKieli = this.oppijanumerorekisteriClient.getAsiointikieli(oidHenkilo);

        return this.ophProperties.url("henkilo-ui.strong-identification", asiointiKieli, loginToken);
    }

}
