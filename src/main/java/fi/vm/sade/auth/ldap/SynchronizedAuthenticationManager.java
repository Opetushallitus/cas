package fi.vm.sade.auth.ldap;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.jasig.cas.authentication.AbstractAuthenticationManager;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.handler.BadCredentialsAuthenticationException;
import org.jasig.cas.authentication.handler.UnsupportedCredentialsException;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

/**
 * Synhronized authemnticate-metohod, otherwise copypaste from AuthenticationManagerImpl
 *
 * @author Antti Salonen
 */
public final class SynchronizedAuthenticationManager extends AbstractAuthenticationManager {

    /** An array of authentication handlers. */
    @NotNull
    @Size(min=1)
    private List<AuthenticationHandler> authenticationHandlers;

    /** An array of CredentialsToPrincipalResolvers. */
    @NotNull
    @Size(min=1)
    private List<CredentialsToPrincipalResolver> credentialsToPrincipalResolvers;

    @Override
    protected Pair<AuthenticationHandler, Principal> authenticateAndObtainPrincipal(final Credentials credentials) throws AuthenticationException {
        if (credentials instanceof UsernamePasswordCredentials) {
            synchronized (((UsernamePasswordCredentials) credentials).getUsername().intern()) {
                return authenticateAndObtainPrincipalInner(credentials);
            }
        } else {
            return authenticateAndObtainPrincipalInner(credentials);
        }
    }

    protected Pair<AuthenticationHandler, Principal> authenticateAndObtainPrincipalInner(final Credentials credentials) throws AuthenticationException {
        boolean foundSupported = false;
        boolean authenticated = false;
        AuthenticationHandler authenticatedClass = null;
        String handlerName;

        AuthenticationException unAuthSupportedHandlerException = BadCredentialsAuthenticationException.ERROR;

        for (final AuthenticationHandler authenticationHandler : this.authenticationHandlers) {
            if (authenticationHandler.supports(credentials)) {
                foundSupported = true;
                handlerName = authenticationHandler.getClass().getName();
                try {
                    if (!authenticationHandler.authenticate(credentials)) {
                        log.info("{} failed to authenticate {}", handlerName, credentials);
                    } else {
                        log.info("{} successfully authenticated {}", handlerName, credentials);
                        authenticatedClass = authenticationHandler;
                        authenticated = true;
                        break;
                    }
                } catch (AuthenticationException e) {
                    unAuthSupportedHandlerException = e;
                    this.log.info("{} failed authenticating {}", handlerName, credentials);
                } catch (Exception e) {
                    this.log.error("{} threw error authenticating {}", new Object[]{handlerName, credentials, e});
                }
            }
        }

        if (!authenticated) {
            if (foundSupported) {
                throw unAuthSupportedHandlerException;
            }

            throw UnsupportedCredentialsException.ERROR;
        }

        foundSupported = false;

        for (final CredentialsToPrincipalResolver credentialsToPrincipalResolver : this.credentialsToPrincipalResolvers) {
            if (credentialsToPrincipalResolver.supports(credentials)) {
                final Principal principal = credentialsToPrincipalResolver.resolvePrincipal(credentials);
                log.info("Resolved principal " + principal);
                foundSupported = true;
                if (principal != null) {
                    return new Pair<AuthenticationHandler,Principal>(authenticatedClass, principal);
                }
            }
        }

        if (foundSupported) {
            if (log.isDebugEnabled()) {
                log.debug("CredentialsToPrincipalResolver found but no principal returned.");
            }

            throw BadCredentialsAuthenticationException.ERROR;
        }

        log.error("CredentialsToPrincipalResolver not found for " + credentials.getClass().getName());
        throw UnsupportedCredentialsException.ERROR;
    }

    /**
     * @param authenticationHandlers The authenticationHandlers to set.
     */
    public void setAuthenticationHandlers(
            final List<AuthenticationHandler> authenticationHandlers) {
        this.authenticationHandlers = authenticationHandlers;
    }

    /**
     * @param credentialsToPrincipalResolvers The
     * credentialsToPrincipalResolvers to set.
     */
    public void setCredentialsToPrincipalResolvers(
            final List<CredentialsToPrincipalResolver> credentialsToPrincipalResolvers) {
        this.credentialsToPrincipalResolvers = credentialsToPrincipalResolvers;
    }
}
