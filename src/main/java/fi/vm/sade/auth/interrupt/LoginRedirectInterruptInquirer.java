package fi.vm.sade.auth.interrupt;

import fi.vm.sade.auth.action.LoginRedirectAction;
import fi.vm.sade.auth.clients.KayttooikeusRestClient;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.interrupt.InterruptInquirer;
import org.apereo.cas.interrupt.InterruptResponse;
import org.apereo.cas.services.RegisteredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.RequestContext;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;

@Component
@ConditionalOnProperty("login.redirect.interrupt.enabled")
public class LoginRedirectInterruptInquirer implements InterruptInquirer {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginRedirectInterruptInquirer.class);

    private final KayttooikeusRestClient kayttooikeusRestClient;
    private final LoginRedirectAction loginRedirectAction;
    private boolean requireStrongIdentification;
    private List<String> requireStrongIdentificationUsernameList = new ArrayList<>();
    private boolean emailVerificationEnabled;
    private List<String> emailVerificationUsernameList = new ArrayList<>();

    public LoginRedirectInterruptInquirer(KayttooikeusRestClient kayttooikeusRestClient, LoginRedirectAction loginRedirectAction) {
        this.kayttooikeusRestClient = kayttooikeusRestClient;
        this.loginRedirectAction = loginRedirectAction;
    }

    @PostConstruct
    public void log() {
        LOGGER.info("Using configuration: requireStrongIdentification={}, requireStrongIdentificationUsernameList={} (size)," +
                        " emailVerificationEnabled={}, emailVerificationUsernameList={} (size)",
                requireStrongIdentification, requireStrongIdentificationUsernameList.size(),
                emailVerificationEnabled, emailVerificationUsernameList.size());
    }

    @Override
    public InterruptResponse inquire(Authentication authentication, RegisteredService registeredService, Service service, Credential credential, RequestContext requestContext) {
        String username = authentication.getPrincipal().getId();
        Optional<String> idpEntityId = getIdpEntityId(authentication);
        return kayttooikeusRestClient.getRedirectCodeByUsername(username)
                .flatMap(redirectCode -> getInterruptResponseByCode(redirectCode, username, idpEntityId))
                .orElseGet(InterruptResponse::none);
    }

    private Optional<String> getIdpEntityId(Authentication authentication) {
        List<Object> idpEntityIdAttr = authentication.getPrincipal().getAttributes().get("idpEntityId");
        if (idpEntityIdAttr != null && idpEntityIdAttr.size() > 0) {
            return Optional.of((String) idpEntityIdAttr.get(0));
        } else {
            return Optional.empty();
        }
    }

    private Optional<InterruptResponse> getInterruptResponseByCode(String redirectCode, String username, Optional<String> idpEntityId) {
        return getRedirectUrl(redirectCode, username, idpEntityId).map(this::getInterruptResponseByUrl);
    }

    private Optional<String> getRedirectUrl(String redirectCode, String username, Optional<String> idpEntityId) {
        switch (redirectCode) {
            case "STRONG_IDENTIFICATION":
                if (requireStrongIdentification || requireStrongIdentificationUsernameList.contains(username)) {
                    return Optional.of(loginRedirectAction.createRedirectUrl(username, "henkilo-ui.strong-identification"));
                }
                break;
            case "EMAIL_VERIFICATION":
                if (emailVerificationEnabled || emailVerificationUsernameList.contains(username)) {
                    return Optional.of(loginRedirectAction.createRedirectUrl(username, "henkilo-ui.email-verification"));
                }
                break;
            case "PASSWORD_CHANGE":
                if (!idpEntityId.orElse("").equals("vetuma")) {
                    return Optional.of(loginRedirectAction.createRedirectUrl(username, "henkilo-ui.password-change"));
                }
                break;
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

    public boolean isRequireStrongIdentification() {
        return requireStrongIdentification;
    }

    @Value("${require-strong-identification}")
    public void setRequireStrongIdentification(boolean requireStrongIdentification) {
        this.requireStrongIdentification = requireStrongIdentification;
    }

    public List<String> getRequireStrongIdentificationUsernameList() {
        return requireStrongIdentificationUsernameList;
    }

    @Value("#{'${require-strong-identification.usernamelist}'.split(',')}")
    public void setRequireStrongIdentificationUsernameList(List<String> requireStrongIdentificationUsernameList) {
        this.requireStrongIdentificationUsernameList = requireStrongIdentificationUsernameList.stream()
                .filter(not(String::isEmpty))
                .collect(toList());
    }

    public boolean isEmailVerificationEnabled() {
        return emailVerificationEnabled;
    }

    @Value("${email-verification-enabled}")
    public void setEmailVerificationEnabled(boolean emailVerificationEnabled) {
        this.emailVerificationEnabled = emailVerificationEnabled;
    }

    public List<String> getEmailVerificationUsernameList() {
        return emailVerificationUsernameList;
    }

    @Value("#{'${email-verification-enabled.usernamelist}'.split(',')}")
    public void setEmailVerificationUsernameList(List<String> emailVerificationUsernameList) {
        this.emailVerificationUsernameList = emailVerificationUsernameList.stream()
                .filter(not(String::isEmpty))
                .collect(toList());
    }

}
