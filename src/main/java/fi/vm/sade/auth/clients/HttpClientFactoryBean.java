package fi.vm.sade.auth.clients;

import fi.vm.sade.javautils.httpclient.OphHttpClient;
import fi.vm.sade.javautils.httpclient.apache.ApacheOphHttpClient;
import fi.vm.sade.properties.OphProperties;
import org.springframework.beans.factory.FactoryBean;

public class HttpClientFactoryBean implements FactoryBean<OphHttpClient> {

    private static final String CLIENT_SUBSYSTEM_CODE = "cas";
    private final OphProperties properties;

    public HttpClientFactoryBean(OphProperties properties) {
        this.properties = properties;
    }

    @Override
    public OphHttpClient getObject() throws Exception {
        return ApacheOphHttpClient.createDefaultOphClient(CLIENT_SUBSYSTEM_CODE, properties);
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
