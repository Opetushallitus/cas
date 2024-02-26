package fi.vm.sade.auth.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.interrupt.InterruptInquirer;
import org.apereo.cas.interrupt.InterruptInquiryExecutionPlanConfigurer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import fi.vm.sade.auth.action.LoginRedirectAction;
import fi.vm.sade.auth.clients.KayttooikeusRestClient;
import fi.vm.sade.auth.interrupt.LoginRedirectInterruptInquirer;

@AutoConfiguration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class InterruptConfiguration {
    private final KayttooikeusRestClient kayttooikeusRestClient;
    private final LoginRedirectAction loginRedirectAction;

    public InterruptConfiguration(KayttooikeusRestClient kayttooikeusRestClient, LoginRedirectAction loginRedirectAction) {
        this.kayttooikeusRestClient = kayttooikeusRestClient;
        this.loginRedirectAction = loginRedirectAction;
    }

    @Bean
    InterruptInquirer loginRedirectInterruptInquirer() {
        return new LoginRedirectInterruptInquirer(kayttooikeusRestClient, loginRedirectAction);
    }

    @Bean
    InterruptInquiryExecutionPlanConfigurer myInterruptInquiryExecutionPlanConfigurer(
        @Qualifier("loginRedirectInterruptInquirer")
        InterruptInquirer loginRedirectInterruptInquirer) {
        return plan -> {
            plan.registerInterruptInquirer(loginRedirectInterruptInquirer);
        };
    }
}
