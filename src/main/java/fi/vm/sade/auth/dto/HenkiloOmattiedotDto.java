package fi.vm.sade.auth.dto;

public class HenkiloOmattiedotDto {
    private String asiointikieli;

    private String kutsumanimi;

    private String sukunimi;

    public String getAsiointikieli() {
        return asiointikieli;
    }

    public void setAsiointikieli(String asiointikieli) {
        this.asiointikieli = asiointikieli;
    }

    public String getKutsumanimi() {
        return kutsumanimi;
    }

    public void setKutsumanimi(String kutsumanimi) {
        this.kutsumanimi = kutsumanimi;
    }

    public String getSukunimi() {
        return sukunimi;
    }

    public void setSukunimi(String sukunimi) {
        this.sukunimi = sukunimi;
    }
}
