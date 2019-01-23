package fi.vm.sade.auth.config;

import fi.vm.sade.login.failure.ByIpAndUserInMemoryLoginFailureHandlerInterceptorAdapter;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThrottleConfiguration {

    @Bean
    public ThrottledSubmissionHandlerInterceptor authenticationThrottle() {
        return new ByIpAndUserInMemoryLoginFailureHandlerInterceptorAdapter();
    }

}
