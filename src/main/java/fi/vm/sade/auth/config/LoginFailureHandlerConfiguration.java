package fi.vm.sade.auth.config;

import org.apereo.cas.throttle.AuthenticationThrottlingExecutionPlan;
import org.apereo.cas.throttle.DefaultAuthenticationThrottlingExecutionPlan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoginFailureHandlerConfiguration {

    @Bean
    AuthenticationThrottlingExecutionPlan authenticationThrottlingExecutionPlan() {
        return new DefaultAuthenticationThrottlingExecutionPlan();
    }

}
