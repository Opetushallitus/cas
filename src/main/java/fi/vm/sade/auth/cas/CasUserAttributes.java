package fi.vm.sade.auth.cas;

import java.util.List;
import java.util.Optional;

public record CasUserAttributes(
        String oidHenkilo,
        String username,
        Optional<String> mfaProvider,
        Optional<String> kayttajaTyyppi,
        Optional<String> idpEntityId,
        List<String> roles
) {
}
