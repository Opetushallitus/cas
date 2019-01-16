package fi.vm.sade.auth.cas;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.handler.support.HttpBasedServiceCredentialsAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.proxy.support.Cas20ProxyHandler;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.HttpMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.net.URL;

/**
 * Configuration to ignore errors from CAS proxy authentication callback. This is may be added to test environment to
 * allow using CAS from localhost. Note that proxy authentication will not work because CAS cannot provide PGT (proxy
 * granting ticket) to service but at least validating service/proxy tickets still work.
 */
@Configuration
public class CasProxyCallbackConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(CasProxyCallbackConfiguration.class);

    @Autowired
    @Qualifier("supportsTrustStoreSslSocketFactoryHttpClient")
    private ObjectProvider<HttpClient> supportsTrustStoreSslSocketFactoryHttpClient;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    private PrincipalFactory proxyPrincipalFactory;

    @Autowired
    @Qualifier("proxy20TicketUniqueIdGenerator")
    private UniqueTicketIdGenerator uniqueTicketIdGenerator;

    @ConditionalOnProperty(prefix = "proxy.callback", name = "ignore-errors", havingValue = "true")
    @Bean
    @Primary
    public AuthenticationEventExecutionPlanConfigurer proxyAuthenticationEventExecutionPlanConfigurer(PrincipalResolver proxyPrincipalResolver) {
        LOGGER.warn("CAS proxy callback is error-tolerant. This should NOT happen in production environment! (1)");
        OnErrorReturnTrueHttpClient onErrorReturnTrueHttpClient = new OnErrorReturnTrueHttpClient(supportsTrustStoreSslSocketFactoryHttpClient.getIfAvailable());
        HttpBasedServiceCredentialsAuthenticationHandler proxyAuthenticationHandler = new HttpBasedServiceCredentialsAuthenticationHandler(
                null, servicesManager.getIfAvailable(), proxyPrincipalFactory, Integer.MIN_VALUE, onErrorReturnTrueHttpClient);
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(proxyAuthenticationHandler, proxyPrincipalResolver);
    }

    @ConditionalOnProperty(prefix = "proxy.callback", name = "ignore-errors", havingValue = "true")
    @Bean
    @Primary
    public Cas20ProxyHandler cas20ProxyHandler() {
        LOGGER.warn("CAS proxy callback is error-tolerant. This should NOT happen in production environment! (2)");
        OnErrorReturnTrueHttpClient onErrorReturnTrueHttpClient = new OnErrorReturnTrueHttpClient(supportsTrustStoreSslSocketFactoryHttpClient.getIfAvailable());
        return new Cas20ProxyHandler(onErrorReturnTrueHttpClient, uniqueTicketIdGenerator);
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
        public HttpMessage sendMessageToEndPoint(URL url) {
            return httpClient.sendMessageToEndPoint(url);
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

        @Override
        public org.apache.http.client.HttpClient getWrappedHttpClient() {
            return httpClient.getWrappedHttpClient();
        }

    }

}
