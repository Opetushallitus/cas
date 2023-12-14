package fi.vm.sade.auth.dto;

public class IdentifiedHenkiloType {

    private KayttajatiedotType kayttajatiedot;
    private String idpEntityId;
    private String henkiloTyyppi;

    public KayttajatiedotType getKayttajatiedot() {
        return kayttajatiedot;
    }

    public void setKayttajatiedot(KayttajatiedotType kayttajatiedot) {
        this.kayttajatiedot = kayttajatiedot;
    }

    public String getIdpEntityId() {
        return idpEntityId;
    }

    public void setIdpEntityId(String idpEntityId) {
        this.idpEntityId = idpEntityId;
    }

    public String getHenkiloTyyppi() {
        return henkiloTyyppi;
    }

    public void setHenkiloTyyppi(String henkiloTyyppi) {
        this.henkiloTyyppi = henkiloTyyppi;
    }

}
