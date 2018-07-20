package fi.vm.sade.auth.clients;

import fi.vm.sade.auth.dto.HenkiloDto;
import fi.vm.sade.auth.dto.KayttooikeusOmatTiedotDto;
import fi.vm.sade.generic.rest.CachingRestClient;
import fi.vm.sade.properties.OphProperties;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class KayttooikeusRestClient extends CachingRestClient {
    private OphProperties ophProperties;

    public KayttooikeusRestClient(OphProperties ophProperties) {
        this.setCasService(ophProperties.url("kayttooikeus-service.security_check"));
        this.setWebCasUrl(ophProperties.url("cas.base"));
        this.ophProperties = ophProperties;
    }

    public String getHenkiloOid(String username) throws IOException {
        String url = this.ophProperties.url("kayttooikeus-service.cas.get-oid", username);
        return this.get(url, HenkiloDto.class).getOid();
    }

    public String createLoginToken(String henkiloOid) throws IOException {
        String url = this.ophProperties.url("kayttooikeus-service.cas.create-login-token", henkiloOid);
        return this.get(url, String.class);
    }

    public Optional<KayttooikeusOmatTiedotDto> getOmattiedot(String username) {
        String url = this.ophProperties.url("kayttooikeus-service.kayttooikeus.omattiedot-by-username", username);
        try {
            return Arrays.stream(this.get(url, KayttooikeusOmatTiedotDto[].class)).findFirst();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
