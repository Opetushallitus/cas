package fi.vm.sade.auth.clients;

import com.google.gson.Gson;
import fi.vm.sade.auth.dto.HenkiloDto;
import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpRequest;
import fi.vm.sade.javautils.http.auth.CasAuthenticator;
import fi.vm.sade.properties.OphProperties;

import static fi.vm.sade.auth.clients.HttpClientUtil.CLIENT_SUBSYSTEM_CODE;
import static fi.vm.sade.auth.clients.HttpClientUtil.noContentOrNotFoundException;
import static java.util.function.Function.identity;

public class KayttooikeusRestClient {

    private final OphHttpClient httpClient;
    private final OphProperties ophProperties;
    private final Gson gson;

    public KayttooikeusRestClient(OphProperties ophProperties) {
        this(newHttpClient(ophProperties), ophProperties, new Gson());
    }

    public KayttooikeusRestClient(OphHttpClient httpClient, OphProperties ophProperties, Gson gson) {
        this.httpClient = httpClient;
        this.ophProperties = ophProperties;
        this.gson = gson;
    }

    private static OphHttpClient newHttpClient(OphProperties properties) {
        CasAuthenticator authenticator = new CasAuthenticator.Builder()
                .username(properties.require("serviceprovider.app.username.to.usermanagement"))
                .password(properties.require("serviceprovider.app.password.to.usermanagement"))
                .webCasUrl(properties.url("cas.base"))
                .casServiceUrl(properties.url("kayttooikeus-service.security_check"))
                .build();
        return new OphHttpClient.Builder(CLIENT_SUBSYSTEM_CODE).authenticator(authenticator).build();
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
                .mapWith(identity())
                .orElseThrow(() -> noContentOrNotFoundException(url));
    }

    public String getRedirectCodeByUsername(String username) {
        String url = this.ophProperties.url("kayttooikeus-service.cas.login.redirect.username", username);
        return httpClient.<String>execute(OphHttpRequest.Builder.get(url).build())
                .expectedStatus(200)
                .mapWith(identity())
                .orElseThrow(() -> noContentOrNotFoundException(url));
    }

}
