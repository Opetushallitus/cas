package fi.vm.sade.auth.clients;

import fi.vm.sade.generic.rest.CachingRestClient;
import fi.vm.sade.properties.OphProperties;

import java.io.IOException;

public class OppijanumerorekisteriRestClient extends CachingRestClient {
    private OphProperties ophProperties;

    public OppijanumerorekisteriRestClient(OphProperties ophProperties) {
        this.setCasService(ophProperties.url("oppijanumerorekisteri.security_check"));
        this.setWebCasUrl(ophProperties.url("cas.base"));
        this.ophProperties = ophProperties;
    }

    public String getAsiointikieli(String henkiloOid) throws IOException {
        String url = this.ophProperties.url("oppijanumerorekisteri.henkilo.kieliKoodi", henkiloOid);
        return this.get(url, String.class);
    }

}
