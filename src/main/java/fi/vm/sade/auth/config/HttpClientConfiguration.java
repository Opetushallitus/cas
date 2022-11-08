package fi.vm.sade.auth.config;

import fi.vm.sade.CasOphProperties;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;
import org.apache.hc.client5.http.socket.LayeredConnectionSocketFactory;
import org.apache.hc.core5.http.impl.DefaultConnectionReuseStrategy;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.HttpClientProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.SimpleHttpClientFactoryBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.HostnameVerifier;
import java.util.Arrays;

import static fi.vm.sade.auth.clients.HttpClientUtil.CALLER_ID;

@Configuration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class HttpClientConfiguration {

    private final CasConfigurationProperties casProperties;
    private final LayeredConnectionSocketFactory trustStoreSslSocketFactory;
    private final HostnameVerifier hostnameVerifier;
    private final CasOphProperties ophProperties;

    public HttpClientConfiguration(CasConfigurationProperties casProperties,
                                   LayeredConnectionSocketFactory trustStoreSslSocketFactory,
                                   HostnameVerifier hostnameVerifier,
                                   CasOphProperties ophProperties) {
        this.casProperties = casProperties;
        this.trustStoreSslSocketFactory = trustStoreSslSocketFactory;
        this.hostnameVerifier = hostnameVerifier;
        this.ophProperties = ophProperties;
    }

    // override cas httpclient to include caller-id header
    @Bean
    public HttpClient noRedirectHttpClient() {
        return getHttpClient(false);
    }

    // override cas httpclient to include caller-id header
    @Bean
    public HttpClient supportsTrustStoreSslSocketFactoryHttpClient() {
        return getHttpClient(true);
    }

    // copy from CasCoreHttpConfiguration
    private HttpClient getHttpClient(final boolean redirectEnabled) {
        SimpleHttpClientFactoryBean c = buildHttpClientFactoryBean();
        c.setRedirectsEnabled(redirectEnabled);
        c.setCircularRedirectsAllowed(redirectEnabled);
        c.setSslSocketFactory(trustStoreSslSocketFactory);
        c.setHostnameVerifier(hostnameVerifier);
        return c.getObject();
    }

    // copy from CasCoreHttpConfiguration (added caller-id header)
    private SimpleHttpClientFactoryBean buildHttpClientFactoryBean() {
        SimpleHttpClientFactoryBean.DefaultHttpClient c = new SimpleHttpClientFactoryBean.DefaultHttpClient();
        HttpClientProperties httpClient = casProperties.getHttpClient();
        c.setConnectionTimeout(Beans.newDuration(httpClient.getConnectionTimeout()).toMillis());
        c.setDefaultHeaders(
                Arrays.asList(
                        new BasicHeader("Caller-Id", CALLER_ID),
                        new BasicHeader("CSRF", CALLER_ID)
                )
        );
        CookieStore cookieStore = new BasicCookieStore();
        cookieStore.addCookie(getCSRFCookie());
        c.setCookieStore(cookieStore);
        c.setConnectionReuseStrategy(DefaultConnectionReuseStrategy.INSTANCE);
        return c;
    }

    private Cookie getCSRFCookie() {
        BasicClientCookie cookie = new BasicClientCookie("CSRF", CALLER_ID);
        cookie.setDomain(ophProperties.require("host.virkailija"));
        return cookie;
    }
}
