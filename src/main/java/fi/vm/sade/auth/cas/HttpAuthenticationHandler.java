package fi.vm.sade.auth.cas;

import com.google.gson.Gson;
import fi.vm.sade.javautils.httpclient.OphHttpClient;
import org.apache.http.HttpHeaders;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.springframework.core.env.Environment;

import javax.security.auth.login.FailedLoginException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

import static java.util.Collections.emptyList;

public class HttpAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    private final OphHttpClient httpClient;
    private final Gson gson;
    private final String authorizationHeader;

    public HttpAuthenticationHandler(ServicesManager servicesManager, Integer order, OphHttpClient httpClient, Environment environment) {
        this(servicesManager, new DefaultPrincipalFactory(), order, httpClient, environment, new Gson());
    }

    public HttpAuthenticationHandler(ServicesManager servicesManager, PrincipalFactory principalFactory, Integer order, OphHttpClient httpClient, Environment environment, Gson gson) {
        super("HttpAuthenticationHandler", servicesManager, principalFactory, order);
        this.httpClient = httpClient;
        this.gson = gson;
        this.authorizationHeader = authorizationHeader(environment);
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(UsernamePasswordCredential credential, String originalPassword) throws GeneralSecurityException, PreventedException {
        String username;
        try {
            username = validateUsernamePassword(credential.getUsername(), credential.getPassword());
        } catch (Exception e) {
            throw new PreventedException("Unexpected HTTP error", e);
        }
        if (username == null) {
            throw new FailedLoginException("Invalid credentials");
        }
        Principal principal = principalFactory.createPrincipal(username);
        return createHandlerResult(credential, principal, emptyList());
    }

    private String validateUsernamePassword(String username, String password) {
        return httpClient.post("kayttooikeus-service.user-details")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .retryOnError(3)
                .dataWriter("application/json", "UTF-8", out
                        -> gson.toJson(new LoginDto(username, password), out))
                .expectStatus(200, 401)
                .execute(handler -> {
                    if (handler.getStatusCode() == 401) {
                        return null;
                    }
                    return gson.fromJson(handler.asText(), LoginDto.class).getUsername();
                });
    }

    private static String authorizationHeader(Environment environment) {
        String authValue = String.join(":",
                environment.getRequiredProperty("kayttooikeus.user-details.username"),
                environment.getRequiredProperty("kayttooikeus.user-details.password"));
        byte[] valueEncoded = Base64.getEncoder().encode(authValue.getBytes(StandardCharsets.ISO_8859_1));
        return "Basic " + new String(valueEncoded);
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

}
