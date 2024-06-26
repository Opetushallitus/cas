package fi.vm.sade.auth.cas;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.gson.Gson;
import fi.vm.sade.auth.Json;
import fi.vm.sade.javautils.httpclient.OphHttpClient;
import lombok.Data;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.Optional;

import static java.util.Collections.emptyList;

public class HttpAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
    private final OphHttpClient httpClient;
    private final Gson gson;

    public HttpAuthenticationHandler(ServicesManager servicesManager, Integer order, OphHttpClient httpClient) {
        this(servicesManager, new DefaultPrincipalFactory(), order, httpClient, new Gson());
    }

    public HttpAuthenticationHandler(ServicesManager servicesManager, PrincipalFactory principalFactory, Integer order, OphHttpClient httpClient, Gson gson) {
        super("HttpAuthenticationHandler", servicesManager, principalFactory, order);
        this.httpClient = httpClient;
        this.gson = gson;
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(UsernamePasswordCredential credential, String originalPassword) throws GeneralSecurityException, PreventedException {
        Optional<CasUserAttributes> opt;
        try {
            opt = validateUsernamePassword(credential.getUsername(), String.valueOf(credential.getPassword()));
        } catch (Exception e) {
            throw new PreventedException(e);
        }
        var userAttributes = opt.orElseThrow(() -> new FailedLoginException("Invalid credentials"));
        var principal = CasPrincipal.of(principalFactory, userAttributes);
        return createHandlerResult(credential, principal, emptyList());

    }

    private Optional<CasUserAttributes> validateUsernamePassword(String username, String password) {
        return httpClient.post("kayttooikeus-service.user-details")
                .retryOnError(3)
                .dataWriter("application/json", "UTF-8", out
                        -> gson.toJson(new LoginDto(username, password), out))
                .expectStatus(200, 401)
                .execute(handler -> {
                    if (handler.getStatusCode() == 401) {
                        return Optional.empty();
                    }
                    return Optional.of(Json.parse(handler.asText(), CasUserAttributes.class));
                });
    }

    private static class LoginDto {
        private String username;
        private String password;

        public LoginDto() {
        }

        public LoginDto(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    @Data
    private static class KayttajatiedotReadDto {
        private final String username;
        private final String mfaProvider;
        private final String kayttajaTyyppi;
    }
}
