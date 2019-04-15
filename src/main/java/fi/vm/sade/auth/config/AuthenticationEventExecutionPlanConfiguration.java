package fi.vm.sade.auth.config;

import fi.vm.sade.auth.cas.HttpAuthenticationHandler;
import fi.vm.sade.javautils.httpclient.OphHttpClient;
import fi.vm.sade.saml.action.SAMLAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.services.ServicesManager;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthenticationEventExecutionPlanConfiguration implements AuthenticationEventExecutionPlanConfigurer {

    private final ServicesManager servicesManager;
    private final OphHttpClient httpClient;

    public AuthenticationEventExecutionPlanConfiguration(ServicesManager servicesManager, OphHttpClient httpClient) {
        this.servicesManager = servicesManager;
        this.httpClient = httpClient;
    }

    @Override
    public void configureAuthenticationExecutionPlan(AuthenticationEventExecutionPlan plan) {
        plan.registerAuthenticationHandler(new HttpAuthenticationHandler(servicesManager, 1, httpClient));
        plan.registerAuthenticationHandler(new SAMLAuthenticationHandler(2, httpClient));
    }

}
