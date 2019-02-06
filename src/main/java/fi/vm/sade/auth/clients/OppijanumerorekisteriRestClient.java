package fi.vm.sade.auth.clients;

import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpRequest;
import fi.vm.sade.javautils.http.auth.CasAuthenticator;
import fi.vm.sade.properties.OphProperties;

import static fi.vm.sade.auth.clients.HttpClientUtil.CLIENT_SUBSYSTEM_CODE;
import static fi.vm.sade.auth.clients.HttpClientUtil.noContentOrNotFoundException;

public class OppijanumerorekisteriRestClient {

    private final OphHttpClient httpClient;
    private final OphProperties ophProperties;

    public OppijanumerorekisteriRestClient(OphProperties ophProperties) {
        this(newHttpClient(ophProperties), ophProperties);
    }

    public OppijanumerorekisteriRestClient(OphHttpClient httpClient, OphProperties ophProperties) {
        this.httpClient = httpClient;
        this.ophProperties = ophProperties;
    }

    private static OphHttpClient newHttpClient(OphProperties properties) {
        CasAuthenticator authenticator = new CasAuthenticator.Builder()
                .username(properties.require("serviceprovider.app.username.to.usermanagement"))
                .password(properties.require("serviceprovider.app.password.to.usermanagement"))
                .webCasUrl(properties.url("cas.base"))
                .casServiceUrl(properties.url("oppijanumerorekisteri.security_check"))
                .build();
        return new OphHttpClient.Builder(CLIENT_SUBSYSTEM_CODE).authenticator(authenticator).build();
    }

    private String jsonString(String json) {
        if (json == null) {
            return "";
        }
        return json.replaceAll("\"", "");
    }

    public String getAsiointikieli(String henkiloOid) {
        String url = this.ophProperties.url("oppijanumerorekisteri.henkilo.kieliKoodi", henkiloOid);
        return httpClient.<String>execute(OphHttpRequest.Builder.get(url).build())
                .expectedStatus(200)
                .mapWith(this::jsonString)
                .orElseThrow(() -> noContentOrNotFoundException(url));
    }

}
