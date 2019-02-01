package fi.vm.sade.auth.interrupt;

import fi.vm.sade.auth.action.EmailVerificationRedirectAction;
import fi.vm.sade.auth.action.StrongIdentificationRedirectAction;
import fi.vm.sade.auth.clients.KayttooikeusRestClient;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.interrupt.InterruptInquirer;
import org.apereo.cas.interrupt.InterruptResponse;
import org.apereo.cas.services.RegisteredService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.RequestContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class LoginRedirectInterruptInquirer implements InterruptInquirer {

    private final KayttooikeusRestClient kayttooikeusRestClient;
    private final StrongIdentificationRedirectAction strongIdentificationRedirectAction;
    private final EmailVerificationRedirectAction emailVerificationRedirectAction;
    private final boolean requireStrongIdentification;
    private final List<String> requireStrongIdentificationUsernameList;
    private final boolean emailVerificationEnabled;
    private final List<String> emailVerificationUsernameList;

    public LoginRedirectInterruptInquirer(KayttooikeusRestClient kayttooikeusRestClient,
                                          StrongIdentificationRedirectAction strongIdentificationRedirectAction,
                                          EmailVerificationRedirectAction emailVerificationRedirectAction,
                                          @Value("${require-strong-identification}") boolean requireStrongIdentification,
                                          @Value("#{'${require-strong-identification.usernamelist}'.split(',')}")
                                                  List<String> requireStrongIdentificationUsernameList,
                                          @Value("${email-verification-enabled}") boolean emailVerificationEnabled,
                                          @Value("#{'${email-verification-enabled.usernamelist}'.split(',')}")
                                                  List<String> emailVerificationUsernameList) {
        this.kayttooikeusRestClient = kayttooikeusRestClient;
        this.strongIdentificationRedirectAction = strongIdentificationRedirectAction;
        this.emailVerificationRedirectAction = emailVerificationRedirectAction;
        this.requireStrongIdentification = requireStrongIdentification;
        this.requireStrongIdentificationUsernameList = requireStrongIdentificationUsernameList;
        this.emailVerificationEnabled = emailVerificationEnabled;
        this.emailVerificationUsernameList = emailVerificationUsernameList;
    }

    @Override
    public InterruptResponse inquire(Authentication authentication, RegisteredService registeredService, Service service, Credential credential, RequestContext requestContext) {
        String username = authentication.getPrincipal().getId();
        return kayttooikeusRestClient.getRedirectCodeByUsername(username)
                .flatMap(redirectCode -> getInterruptResponseByCode(redirectCode, username))
                .orElseGet(InterruptResponse::none);
    }

    private Optional<InterruptResponse> getInterruptResponseByCode(String redirectCode, String username) {
        return getRedirectUrl(redirectCode, username).map(this::getInterruptResponseByUrl);
    }

    private Optional<String> getRedirectUrl(String redirectCode, String username) {
        switch (redirectCode) {
            case "STRONG_IDENTIFICATION":
                if (requireStrongIdentification || requireStrongIdentificationUsernameList.contains(username)) {
                    return Optional.of(strongIdentificationRedirectAction.createRedirectUrl(username));
                }
                break;
            case "EMAIL_VERIFICATION":
                if (emailVerificationEnabled || emailVerificationUsernameList.contains(username)) {
                    return Optional.of(emailVerificationRedirectAction.createRedirectUrl(username));
                }
            default:
                throw new IllegalArgumentException("Unknown redirectCode: " + redirectCode);
        }
        return Optional.empty();
    }

    private InterruptResponse getInterruptResponseByUrl(String url) {
        InterruptResponse interruptResponse = new InterruptResponse();
        interruptResponse.setLinks(Map.of("redirect", url));
        interruptResponse.setInterrupt(true);
        interruptResponse.setBlock(true);
        interruptResponse.setAutoRedirect(true);
        return interruptResponse;
    }

}
