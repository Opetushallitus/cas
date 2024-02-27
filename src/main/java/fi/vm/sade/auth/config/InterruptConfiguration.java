package fi.vm.sade.auth.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.interrupt.InterruptInquirer;
import org.apereo.cas.interrupt.InterruptInquiryExecutionPlanConfigurer;
import org.apereo.cas.interrupt.InterruptTrackingEngine;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import fi.vm.sade.auth.action.LoginRedirectAction;
import fi.vm.sade.auth.clients.KayttooikeusRestClient;
import fi.vm.sade.auth.interrupt.CustomInterruptTrackingEngine;
import fi.vm.sade.auth.interrupt.LoginRedirectInterruptInquirer;

@AutoConfiguration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class InterruptConfiguration {
    @Bean
    InterruptTrackingEngine customInterruptTrackingEngine(
            @Qualifier("interruptCookieGenerator")
            final CasCookieBuilder interruptCookieGenerator,
            final CasConfigurationProperties casProperties) {
        return new CustomInterruptTrackingEngine(interruptCookieGenerator, casProperties);
    }

    @Bean
    InterruptInquirer loginRedirectInterruptInquirer(
            KayttooikeusRestClient kayttooikeusRestClient, LoginRedirectAction loginRedirectAction) {
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
