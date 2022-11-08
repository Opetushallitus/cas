package fi.vm.sade;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
public class CasOphConfiguration {
    @Bean
    public ObservationRegistry observationRegistry() {
        // Disable all observations
        return ObservationRegistry.NOOP;
    }
}
