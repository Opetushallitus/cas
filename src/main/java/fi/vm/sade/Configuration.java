package fi.vm.sade;

public class Configuration {

    private String hakaUrl;
    private String suomifiUrl;
    private String suomifiUrlTarget;
    private String loginTietosuojaselosteUrl;

    public String getHakaUrl() {
        return hakaUrl;
    }

    public void setHakaUrl(String hakaUrl) {
        this.hakaUrl = hakaUrl;
    }

    public String getLoginTietosuojaselosteUrl() {
        return loginTietosuojaselosteUrl;
    }

    public void setLoginTietosuojaselosteUrl(String loginTietosuojaselosteUrl) {
        this.loginTietosuojaselosteUrl = loginTietosuojaselosteUrl;
    }

    public String getSuomifiUrl() {
        return suomifiUrl;
    }

    public void setSuomifiUrl(String suomifiUrl) {
        this.suomifiUrl = suomifiUrl;
    }

    public String getSuomifiUrlTarget() {
        return suomifiUrlTarget;
    }

    public void setSuomifiUrlTarget(String suomifiUrlTarget) {
        this.suomifiUrlTarget = suomifiUrlTarget;
    }
}
