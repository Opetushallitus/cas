package fi.vm.sade.auth.config;

import org.apereo.cas.services.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.List;

import static java.util.Collections.singletonList;

@Configuration
public class RegisteredServiceConfiguration {

    @Bean
    public RegisteredServiceProxyPolicy registeredServiceProxyPolicy() {
        return new RegexMatchingRegisteredServiceProxyPolicy("^https?://.*");
    }

    @Bean
    public RegisteredServiceAttributeReleasePolicy attributeReleasePolicy() {
        // we don't currently use attributes (also fi.vm.sade:scala-cas fails to parse response with attributes)
        return new DenyAllAttributeReleasePolicy();
    }

    @Bean
    public List<? extends RegisteredService> inMemoryRegisteredServices(RegisteredServiceProxyPolicy registeredServiceProxyPolicy,
                                                                        RegisteredServiceAttributeReleasePolicy attributeReleasePolicy,
                                                                        Environment environment) {
        RegexRegisteredService regexRegisteredService = new RegexRegisteredService();
        regexRegisteredService.setId(1L);
        regexRegisteredService.setName("Login whitelist");
        regexRegisteredService.setDescription("Allows CAS login on domains in whitelist");
        regexRegisteredService.setServiceId(environment.getRequiredProperty("whitelist.regexp"));
        regexRegisteredService.setEvaluationOrder(0);
        regexRegisteredService.setProxyPolicy(registeredServiceProxyPolicy);
        regexRegisteredService.setAttributeReleasePolicy(attributeReleasePolicy);
        return singletonList(regexRegisteredService);
    }

}
