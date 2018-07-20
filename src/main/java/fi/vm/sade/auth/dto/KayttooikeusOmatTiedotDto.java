package fi.vm.sade.auth.dto;


import java.util.HashSet;
import java.util.Set;

public class KayttooikeusOmatTiedotDto {
    private String oidHenkilo;
    private String username;
    private String kayttajaTyyppi;
    private Set<KayttooikeusOrganisaatiotDto> organisaatiot = new HashSet<>();

    public String getOidHenkilo() {
        return this.oidHenkilo;
    }

    public String getUsername() {
        return this.username;
    }

    public String getKayttajaTyyppi() {
        return this.kayttajaTyyppi;
    }

    public Set<KayttooikeusOrganisaatiotDto> getOrganisaatiot() {
        return this.organisaatiot;
    }

    public void setOidHenkilo(final String oidHenkilo) {
        this.oidHenkilo = oidHenkilo;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public void setKayttajaTyyppi(final String kayttajaTyyppi) {
        this.kayttajaTyyppi = kayttajaTyyppi;
    }

    public void setOrganisaatiot(final Set<KayttooikeusOrganisaatiotDto> organisaatiot) {
        this.organisaatiot = organisaatiot;
    }

}
