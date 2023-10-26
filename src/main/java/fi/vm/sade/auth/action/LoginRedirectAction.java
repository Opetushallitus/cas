package fi.vm.sade.auth.action;

import fi.vm.sade.auth.clients.KayttooikeusRestClient;
import fi.vm.sade.auth.clients.OppijanumerorekisteriRestClient;
import fi.vm.sade.properties.OphProperties;
import org.springframework.stereotype.Component;

@Component
public class LoginRedirectAction {
    private final KayttooikeusRestClient kayttooikeusRestClient;
    private final OppijanumerorekisteriRestClient oppijanumerorekisteriRestClient;
    private final OphProperties ophProperties;

    public LoginRedirectAction(KayttooikeusRestClient kayttooikeusRestClient,
                               OppijanumerorekisteriRestClient oppijanumerorekisteriRestClient,
                               OphProperties ophProperties) {
        this.kayttooikeusRestClient = kayttooikeusRestClient;
        this.oppijanumerorekisteriRestClient = oppijanumerorekisteriRestClient;
        this.ophProperties = ophProperties;
    }

    public String createRedirectUrl(String username, String urlProperty) {
        String oidHenkilo = this.kayttooikeusRestClient.getHenkiloOid(username);
        String loginToken = this.kayttooikeusRestClient.createLoginToken(oidHenkilo);
        String asiointiKieli = this.oppijanumerorekisteriRestClient.getAsiointikieli(oidHenkilo);
        return this.ophProperties.url(urlProperty, asiointiKieli, loginToken);
    }
}
