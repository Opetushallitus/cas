package fi.vm.sade.auth.clients;

public final class HttpClientUtil {

    private HttpClientUtil() {
    }

    public static final String CLIENT_SUBSYSTEM_CODE = "cas";

    public static RuntimeException noContentOrNotFoundException(String url) {
        return new RuntimeException(String.format("Service %s returned status 204 or 404", url));
    }

}
