package fi.vm.sade.auth.cas;

import com.google.gson.Gson;
import fi.vm.sade.javautils.httpclient.OphHttpClient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.DefaultPrincipalFactory;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.PrincipalFactory;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;

import static java.util.Collections.emptyList;

public class HttpAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    private final OphHttpClient httpClient;
    private final Gson gson;
    private final PrincipalFactory principalFactory;

    public HttpAuthenticationHandler(OphHttpClient httpClient) {
        this(httpClient, new Gson(), new DefaultPrincipalFactory());
    }

    public HttpAuthenticationHandler(OphHttpClient httpClient, Gson gson, PrincipalFactory principalFactory) {
        this.httpClient = httpClient;
        this.gson = gson;
        this.principalFactory = principalFactory;
    }

    @Override
    protected HandlerResult authenticateUsernamePasswordInternal(UsernamePasswordCredential credential) throws GeneralSecurityException, PreventedException {
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

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class LoginDto {
        private String username;
        private String password;
    }

}
