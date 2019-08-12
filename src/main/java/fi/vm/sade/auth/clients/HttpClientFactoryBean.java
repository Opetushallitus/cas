package fi.vm.sade.auth.clients;

import fi.vm.sade.javautils.httpclient.OphHttpClient;
import fi.vm.sade.javautils.httpclient.apache.ApacheOphHttpClient;
import fi.vm.sade.properties.OphProperties;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

import static fi.vm.sade.auth.clients.HttpClientUtil.CALLER_ID;

@Component("httpClient")
public class HttpClientFactoryBean implements FactoryBean<OphHttpClient> {

    private final OphProperties properties;

    public HttpClientFactoryBean(OphProperties properties) {
        this.properties = properties;
    }

    @Override
    public OphHttpClient getObject() throws Exception {
        return ApacheOphHttpClient.createDefaultOphClient(CALLER_ID, properties);
    }

    @Override
    public Class<?> getObjectType() {
        return OphHttpClient.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
