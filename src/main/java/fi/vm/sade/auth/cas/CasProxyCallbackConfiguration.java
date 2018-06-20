package fi.vm.sade.auth.cas;

import java.net.URL;
import org.jasig.cas.authentication.handler.support.HttpBasedServiceCredentialsAuthenticationHandler;
import org.jasig.cas.ticket.proxy.support.Cas20ProxyHandler;
import org.jasig.cas.util.http.HttpClient;
import org.jasig.cas.util.http.HttpMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration to ignore errors from CAS proxy authentication callback. This is may be added to test environment to
 * allow using CAS from localhost. Note that proxy authentication will not work because CAS cannot provide PGT (proxy
 * granting ticket) to service but at least validating service/proxy tickets still work.
 */
public class CasProxyCallbackConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(CasProxyCallbackConfiguration.class);

    private final boolean casProxyCallbackIgnoreErrors;
    private final HttpClient httpClient;
    private final HttpBasedServiceCredentialsAuthenticationHandler authenticationHandler;
    private final Cas20ProxyHandler proxyHandler;

    public CasProxyCallbackConfiguration(boolean casProxyCallbackIgnoreErrors, HttpClient httpClient,
            HttpBasedServiceCredentialsAuthenticationHandler authenticationHandler, Cas20ProxyHandler proxyHandler) {
        this.casProxyCallbackIgnoreErrors = casProxyCallbackIgnoreErrors;
        this.httpClient = httpClient;
        this.authenticationHandler = authenticationHandler;
        this.proxyHandler = proxyHandler;
    }

    public void initialize() {
        if (casProxyCallbackIgnoreErrors) {
            LOGGER.warn("CAS proxy callback is error-tolerant. This should NOT happen in production environment!");
            authenticationHandler.setHttpClient(new OnErrorReturnTrueHttpClient(httpClient));
            proxyHandler.setHttpClient(new OnErrorReturnTrueHttpClient(httpClient));
        }
    }

    private static class OnErrorReturnTrueHttpClient implements HttpClient {

        private static final Logger LOGGER = LoggerFactory.getLogger(OnErrorReturnTrueHttpClient.class);

        private final HttpClient httpClient;

        public OnErrorReturnTrueHttpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
        }

        @Override
        public boolean sendMessageToEndPoint(HttpMessage message) {
            try {
                httpClient.sendMessageToEndPoint(message);
            } catch (Exception e) {
                LOGGER.error("HttpClient#sendMessageToEndPoint failed, ignoring", e);
            }
            return true;
        }

        @Override
        public boolean isValidEndPoint(String url) {
            try {
                httpClient.isValidEndPoint(url);
            } catch (Exception e) {
                LOGGER.error("HttpClient#isValidEndPoint failed, ignoring", e);
            }
            return true;
        }

        @Override
        public boolean isValidEndPoint(URL url) {
            try {
                httpClient.isValidEndPoint(url);
            } catch (Exception e) {
                LOGGER.error("HttpClient#isValidEndPoint failed, ignoring", e);
            }
            return true;
        }

    }

}
