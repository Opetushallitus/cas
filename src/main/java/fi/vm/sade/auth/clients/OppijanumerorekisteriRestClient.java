package fi.vm.sade.auth.clients;

import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpRequest;
import fi.vm.sade.javautils.http.auth.CasAuthenticator;
import fi.vm.sade.properties.OphProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import static fi.vm.sade.auth.clients.HttpClientUtil.CALLER_ID;
import static fi.vm.sade.auth.clients.HttpClientUtil.noContentOrNotFoundException;

@Component
public class OppijanumerorekisteriRestClient {

    private final OphHttpClient httpClient;
    private final OphProperties ophProperties;

    @Autowired
    public OppijanumerorekisteriRestClient(OphProperties ophProperties, Environment environment) {
        this(newHttpClient(ophProperties, environment), ophProperties);
    }

    public OppijanumerorekisteriRestClient(OphHttpClient httpClient, OphProperties ophProperties) {
        this.httpClient = httpClient;
        this.ophProperties = ophProperties;
    }

    private static OphHttpClient newHttpClient(OphProperties properties, Environment environment) {
        CasAuthenticator authenticator = new CasAuthenticator.Builder()
                .username(environment.getRequiredProperty("serviceprovider.app.username.to.usermanagement"))
                .password(environment.getRequiredProperty("serviceprovider.app.password.to.usermanagement"))
                .webCasUrl(properties.url("cas.base"))
                .casServiceUrl(properties.url("oppijanumerorekisteri.security_check"))
                .build();
        return new OphHttpClient.Builder(CALLER_ID).authenticator(authenticator).build();
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
