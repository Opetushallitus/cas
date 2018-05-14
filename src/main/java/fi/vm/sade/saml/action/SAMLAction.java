package fi.vm.sade.saml.action;

import javax.servlet.http.HttpServletRequest;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.web.flow.AbstractNonInteractiveCredentialsAction;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.webflow.execution.RequestContext;

public class SAMLAction extends AbstractNonInteractiveCredentialsAction {

    @Override
    protected Credential constructCredentialsFromRequest(RequestContext requestContext) {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(requestContext);
        String authToken = request.getParameter("authToken");
        if (authToken != null) {
            return new SAMLCredentials(authToken);
        }
        return null;
    }

}
