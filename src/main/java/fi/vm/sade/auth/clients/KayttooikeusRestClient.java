package fi.vm.sade.auth.clients;

import com.google.gson.Gson;
import fi.vm.sade.auth.dto.HenkiloDto;
import fi.vm.sade.auth.dto.IdentifiedHenkiloType;
import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpRequest;
import fi.vm.sade.javautils.http.auth.CasAuthenticator;
import fi.vm.sade.properties.OphProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static fi.vm.sade.auth.clients.HttpClientUtil.CLIENT_SUBSYSTEM_CODE;
import static fi.vm.sade.auth.clients.HttpClientUtil.noContentOrNotFoundException;
import static java.util.function.Predicate.not;

@Component
public class KayttooikeusRestClient {

    private final OphHttpClient httpClient;
    private final OphProperties ophProperties;
    private final Gson gson;

    @Autowired
    public KayttooikeusRestClient(OphProperties ophProperties, Environment environment) {
        this(newHttpClient(ophProperties, environment), ophProperties, new Gson());
    }

    public KayttooikeusRestClient(OphHttpClient httpClient, OphProperties ophProperties, Gson gson) {
        this.httpClient = httpClient;
        this.ophProperties = ophProperties;
        this.gson = gson;
    }

    private static OphHttpClient newHttpClient(OphProperties properties, Environment environment) {
        CasAuthenticator authenticator = new CasAuthenticator.Builder()
                .username(environment.getRequiredProperty("serviceprovider.app.username.to.usermanagement"))
                .password(environment.getRequiredProperty("serviceprovider.app.password.to.usermanagement"))
                .webCasUrl(properties.url("cas.base"))
                .casServiceUrl(properties.url("kayttooikeus-service.security_check"))
                .build();
        return new OphHttpClient.Builder(CLIENT_SUBSYSTEM_CODE).authenticator(authenticator).build();
    }

    private String jsonString(String json) {
        if (json == null) {
            return "";
        }
        return json.replaceAll("\"", "");
    }

    public String getHenkiloOid(String username) {
        String url = this.ophProperties.url("kayttooikeus-service.cas.get-oid", username);
        return httpClient.<String>execute(OphHttpRequest.Builder.get(url).build())
                .expectedStatus(200)
                .mapWith(json -> gson.fromJson(json, HenkiloDto.class).getOid())
                .orElseThrow(() -> noContentOrNotFoundException(url));
    }

    public String createLoginToken(String henkiloOid) {
        String url = this.ophProperties.url("kayttooikeus-service.cas.create-login-token", henkiloOid);
        return httpClient.<String>execute(OphHttpRequest.Builder.get(url).build())
                .expectedStatus(200)
                .mapWith(this::jsonString)
                .orElseThrow(() -> noContentOrNotFoundException(url));
    }

    public Optional<String> getRedirectCodeByUsername(String username) {
        String url = this.ophProperties.url("kayttooikeus-service.cas.login.redirect.username", username);
        String redirectCode = httpClient.<String>execute(OphHttpRequest.Builder.get(url).build())
                .expectedStatus(200)
                .mapWith(this::jsonString)
                .orElseThrow(() -> noContentOrNotFoundException(url));
        return Optional.ofNullable(redirectCode).map(String::trim).filter(not(String::isEmpty));
    }

    public IdentifiedHenkiloType getHenkiloByAuthToken(String authToken) {
        String url = ophProperties.url("kayttooikeus-service.cas.henkiloByAuthToken", authToken);
        return httpClient.<IdentifiedHenkiloType>execute(OphHttpRequest.Builder.get(url).build())
                .expectedStatus(200)
                .mapWith(json -> gson.fromJson(json, IdentifiedHenkiloType.class))
                .orElseThrow(() -> noContentOrNotFoundException(url));
    }

}
