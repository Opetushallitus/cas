package fi.vm.sade.auth.ldap;

import fi.vm.sade.AuthenticationUtil;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.web.support.CookieRetrievingCookieGenerator;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * @author Antti Salonen
 */
public class MyRolesResource extends AbstractController {

    private TicketRegistry ticketRegistry;
    private CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;
    private AuthenticationUtil authenticationUtil;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final String ticketGrantingTicketId = ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
        final TicketGrantingTicket ticketGrantingTicket = (TicketGrantingTicket) this.ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);

        final String uid = ticketGrantingTicket!=null && ticketGrantingTicket.getAuthentication() != null ? ticketGrantingTicket
                .getAuthentication().getPrincipal().getId(): null;

        return new ModelAndView(new View() {
            
            @Override
            public String getContentType() {
                return "text/json";
            }
            
            @Override
            public void render(Map<String, ?> stringMap, HttpServletRequest request, HttpServletResponse response) throws Exception {
                if(uid==null) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                final PrintWriter writer = response.getWriter();
                String userRoles = authenticationUtil.getUserRoles(uid);
                if (userRoles != null) {
                    writer.print(userRoles);
                }
                else {
                    // render roles string list as javascript/json array
                    // FIXME!! TÄMÄ KOODI PITÄÄ POISTAA SITTEN, KUN KAIKKIEN HENKILÖIDEN
                    // RYHMÄTIEDOT ON SAATU GENEROITUA HENKILÖN ATTRIBUUTTEIHIN!!!
                    final List<String> roles = authenticationUtil.getRoles(uid);
                    writer.print("[\"USER_");
                    writer.print(uid);
                    writer.print("\"");
                    for (int i = 0; i < roles.size(); i++) {
                        String role = roles.get(i);
                        writer.print(", ");
                        writer.print("\"");
                        writer.print(role);
                        writer.print("\"");
                    }
                    writer.print("]");
                }
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
