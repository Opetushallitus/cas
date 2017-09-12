package fi.vm.sade;

public class Configuration {

    private String hakaUrl;
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
}
