package fi.vm.sade.auth.cas;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;

import java.util.List;
import java.util.Map;

public class CasPrincipal {
    public static Principal of(PrincipalFactory principalFactory, CasUserAttributes userAttributes) {
        try {
            Map<String, List<Object>> attributes = Map.of(
                    "oidHenkilo", List.of(userAttributes.oidHenkilo()),
                    "idpEntityId", List.of(userAttributes.idpEntityId().orElse("usernamePassword")),
                    "kayttajaTyyppi", List.of(userAttributes.kayttajaTyyppi().orElse("VIRKAILIJA")),
                    "roles", List.copyOf(userAttributes.roles())
            );
            return principalFactory.createPrincipal(userAttributes.username(), attributes);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
