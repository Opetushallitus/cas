package fi.vm.sade.auth.config;

import org.apache.catalina.valves.AccessLogValve;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty("server.tomcat.accesslog.max-days")
public class TomcatAccessLogCustomizer implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    private final int maxDays;

    public TomcatAccessLogCustomizer(@Value("${server.tomcat.accesslog.max-days}") int maxDays) {
        this.maxDays = maxDays;
    }

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        factory.getEngineValves()
                .stream()
                .filter(AccessLogValve.class::isInstance)
                .map(AccessLogValve.class::cast)
                .forEach(accessLogValve -> accessLogValve.setMaxDays(maxDays));
    }

}
