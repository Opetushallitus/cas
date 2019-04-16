package fi.vm.sade.auth.config;

import fi.vm.sade.login.failure.AbstractInMemoryLoginFailureHandlerInterceptorAdapter;
import fi.vm.sade.login.failure.ByIpAndUserInMemoryLoginFailureHandlerInterceptorAdapter;
import fi.vm.sade.login.failure.JdbcLoginFailureStore;
import fi.vm.sade.login.failure.LoginFailureStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class ThrottleConfiguration {

    private final JdbcTemplate jdbcTemplate;

    public ThrottleConfiguration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Bean
    public LoginFailureStore loginFailureStore() {
        return new JdbcLoginFailureStore(jdbcTemplate);
    }

    @Bean
    public AbstractInMemoryLoginFailureHandlerInterceptorAdapter authenticationThrottle() {
        return new ByIpAndUserInMemoryLoginFailureHandlerInterceptorAdapter(loginFailureStore());
    }

}
