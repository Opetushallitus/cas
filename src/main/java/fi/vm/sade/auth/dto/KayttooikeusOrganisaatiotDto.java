package fi.vm.sade.auth.dto;

import java.util.HashSet;
import java.util.Set;

public class KayttooikeusOrganisaatiotDto {
    private String organisaatioOid;
    private Set<KayttooikeusOikeudetDto> kayttooikeudet = new HashSet<>();

    public String getOrganisaatioOid() {
        return this.organisaatioOid;
    }

    public Set<KayttooikeusOikeudetDto> getKayttooikeudet() {
        return this.kayttooikeudet;
    }

    public void setOrganisaatioOid(final String organisaatioOid) {
        this.organisaatioOid = organisaatioOid;
    }

    public void setKayttooikeudet(final Set<KayttooikeusOikeudetDto> kayttooikeudet) {
        this.kayttooikeudet = kayttooikeudet;

    }
}