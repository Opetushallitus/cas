package fi.vm.sade.saml.action;

import fi.vm.sade.authentication.service.AuthenticationService;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

/**
 * User: tommiha
 * Date: 3/26/13
 * Time: 11:45 AM
 */
public class SAMLAction extends AbstractAction {

    @NotNull
    private AuthenticationService authenticationService;

    @NotNull
    private CentralAuthenticationService centralAuthenticationService;

    @Override
    protected Event doExecute(RequestContext requestContext) throws Exception {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(requestContext);
        String authToken = request.getParameter("authToken");
        if(authToken != null) {
            Credentials credentials = new SAMLCredentials(authToken);

            try {
                WebUtils.putTicketGrantingTicketInRequestScope(requestContext, this.centralAuthenticationService
                        .createTicketGrantingTicket(credentials));
                return success();
            } catch (final TicketException e) {
                return error();
            }
        }
        return error();
    }

    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public CentralAuthenticationService getCentralAuthenticationService() {
        return centralAuthenticationService;
    }

    public void setCentralAuthenticationService(CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }
}
