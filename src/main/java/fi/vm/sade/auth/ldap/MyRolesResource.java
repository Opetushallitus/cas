package fi.vm.sade.auth.ldap;

import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.web.support.CookieRetrievingCookieGenerator;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.AbstractController;

import fi.vm.sade.AuthenticationUtil;

public class MyRolesResource extends AbstractController {

    private TicketRegistry ticketRegistry;
    private CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;
    private AuthenticationUtil authenticationUtil;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final String ticketGrantingTicketId = ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
        final TicketGrantingTicket ticketGrantingTicket = this.ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);

        final String uid = ticketGrantingTicket != null && ticketGrantingTicket.getAuthentication() != null
                ? ticketGrantingTicket.getAuthentication().getPrincipal().getId()
                : null;
        return new ModelAndView(new View() {
            
            @Override
            public String getContentType() {
                return "text/json";
            }
            
            @Override
            public void render(Map<String, ?> stringMap, HttpServletRequest request, HttpServletResponse response) throws Exception {
                if (uid == null) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                final PrintWriter writer = response.getWriter();
                String userRoles = authenticationUtil.getUserRoles(uid);
                writer.print(userRoles);
            }
        });
    }

    public void setTicketGrantingTicketCookieGenerator(CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator) {
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
    }

    public void setTicketRegistry(TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }

    public void setAuthenticationUtil(AuthenticationUtil authenticationUtil) {
        this.authenticationUtil = authenticationUtil;
    }
}
