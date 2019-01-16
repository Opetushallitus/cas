package fi.vm.sade.saml.action;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.web.flow.actions.AbstractNonInteractiveCredentialsAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;

@Component
public class SAMLAction extends AbstractNonInteractiveCredentialsAction {

    public SAMLAction(CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
                      CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
                      AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy) {
        super(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy);
    }

    @Override
    protected Credential constructCredentialsFromRequest(RequestContext requestContext) {
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        return new SAMLCredentials(request.getParameter("authToken"));
    }

}
