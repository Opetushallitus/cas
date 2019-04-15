package fi.vm.sade;

import fi.vm.sade.properties.OphProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class CasOphProperties extends OphProperties {

    public CasOphProperties(Environment environment) {
        addFiles("/cas-oph.properties");
        addDefault("host.cas", environment.getRequiredProperty("host.cas"));
        addDefault("host.virkailija", environment.getRequiredProperty("host.virkailija"));
        addDefault("host.alb", environment.getRequiredProperty("host.alb"));
    }

}
