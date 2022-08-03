package fi.vm.sade.auth.config;

import fi.vm.sade.CasOphProperties;
import org.apache.http.client.CookieStore;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHeader;
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
        c.setReadTimeout((int) Beans.newDuration(httpClient.getReadTimeout()).toMillis());
        c.setDefaultHeaders(
                Arrays.asList(
                        new BasicHeader("Caller-Id", CALLER_ID),
                        new BasicHeader("CSRF", CALLER_ID)
                )
        );
        CookieStore cookieStore = new BasicCookieStore();
        cookieStore.addCookie(getCSRFCookie());
        c.setCookieStore(cookieStore);
        c.setConnectionReuseStrategy(NoConnectionReuseStrategy.INSTANCE);
        return c;
    }

    private ClientCookie getCSRFCookie() {
        BasicClientCookie cookie = new BasicClientCookie("CSRF", CALLER_ID);
        cookie.setDomain(ophProperties.require("host.virkailija"));
        return cookie;
    }
}
