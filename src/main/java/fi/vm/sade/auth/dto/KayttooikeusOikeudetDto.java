package fi.vm.sade.auth.dto;

public class KayttooikeusOikeudetDto {
    private String palvelu;
    private String oikeus;

    public String getPalvelu() {
        return this.palvelu;
    }

    public String getOikeus() {
        return this.oikeus;
    }

    public void setPalvelu(final String palvelu) {
        this.palvelu = palvelu;
    }

    public void setOikeus(final String oikeus) {
        this.oikeus = oikeus;
    }

}
