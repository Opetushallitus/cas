package fi.vm.sade.auth.config;

import org.apereo.cas.services.RegexMatchingRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceProxyPolicy;
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
    public List<? extends RegisteredService> inMemoryRegisteredServices(RegisteredServiceProxyPolicy registeredServiceProxyPolicy, Environment environment) {
        RegexRegisteredService regexRegisteredService = new RegexRegisteredService();
        regexRegisteredService.setId(1L);
        regexRegisteredService.setName("Login whitelist");
        regexRegisteredService.setDescription("Allows CAS login on domains in whitelist");
        regexRegisteredService.setServiceId(environment.getRequiredProperty("whitelist.regexp"));
        regexRegisteredService.setEvaluationOrder(0);
        regexRegisteredService.setProxyPolicy(registeredServiceProxyPolicy);
        return singletonList(regexRegisteredService);
    }

}
