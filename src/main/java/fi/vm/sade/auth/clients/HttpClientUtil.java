package fi.vm.sade.auth.clients;

public final class HttpClientUtil {

    private HttpClientUtil() {
    }

    public static final String CALLER_ID = "1.2.246.562.10.00000000001.cas";

    public static RuntimeException noContentOrNotFoundException(String url) {
        return new RuntimeException(String.format("Service %s returned status 204 or 404", url));
    }

}
