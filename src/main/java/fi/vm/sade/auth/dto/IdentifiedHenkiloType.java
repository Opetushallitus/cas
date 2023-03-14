package fi.vm.sade.auth.dto;

public class IdentifiedHenkiloType {

    private KayttajatiedotType kayttajatiedot;
    private String idpEntityId;

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

}
