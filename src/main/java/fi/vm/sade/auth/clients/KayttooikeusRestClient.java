package fi.vm.sade.auth.clients;

import fi.vm.sade.auth.dto.HenkiloDto;
import fi.vm.sade.generic.rest.CachingRestClient;
import fi.vm.sade.properties.OphProperties;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

import java.io.IOException;

public class KayttooikeusRestClient extends CachingRestClient {
    private OphProperties ophProperties;

    public KayttooikeusRestClient(OphProperties ophProperties) {
        this.setCasService(ophProperties.url("kayttooikeus-service.security_check"));
        this.setWebCasUrl(ophProperties.url("cas.base"));
        this.ophProperties = ophProperties;
    }

    public String getHenkiloOid(Credentials credentials) throws IOException {
        String username = ((UsernamePasswordCredentials) credentials).getUsername();
        String url = this.ophProperties.url("kayttooikeus-service.cas.get-oid", username);
        return this.get(url, HenkiloDto.class).getOid();
    }

    public String createLoginToken(String henkiloOid) throws IOException {
        String url = this.ophProperties.url("kayttooikeus-service.cas.create-login-token", henkiloOid);
        return this.get(url, String.class);
    }

}
