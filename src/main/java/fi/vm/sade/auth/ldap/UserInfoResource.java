package fi.vm.sade.auth.ldap;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.web.support.CookieRetrievingCookieGenerator;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.AbstractController;

import com.google.gson.Gson;

import fi.vm.sade.AuthenticationUtil;

/**
 * Rest resurssi joka tarjoaa tietoa käyttäjästä urlissa /cas/me JSON olion muodossa:
 * 
 * {"uid":"uid","oid":"1.2.246.512.24.67912964565","firstName":"etu",
 * "lastName"
 * :"suku","groups":["APP_ANOMUSTENHALLINTA","APP_ANOMUSTENHALLINTA_READ"
 * ,"APP_ORGANISAATIOHALLINTA"
 * ,"APP_ORGANISAATIOHALLINTA_READ_UPDATE","APP_HENKILONHALLINTA"
 * ,"APP_HENKILONHALLINTA_READ"
 * ,"APP_KOODISTO","APP_KOODISTO_READ","APP_KOOSTEROOLIENHALLINTA"
 * ,"APP_KOOSTEROOLIENHALLINTA_READ"
 * ,"APP_OID","APP_OID_READ","APP_OMATTIEDOT","APP_OMATTIEDOT_READ_UPDATE"
 * ,"APP_TARJONTA","APP_TARJONTA_CRUD","VIRKAILIJA","LANG_sv",
 * "APP_OMATTIEDOT_READ_UPDATE_1.2.246.562.10.51053050251"
 * ,"APP_KOOSTEROOLIENHALLINTA_READ_1.2.246.562.10.51053050251"
 * ,"APP_TARJONTA_CRUD_1.2.246.562.10.51053050251"
 * ,"APP_OID_READ_1.2.246.562.10.51053050251"
 * ,"APP_ANOMUSTENHALLINTA_READ_1.2.246.562.10.51053050251"
 * ,"APP_KOODISTO_READ_1.2.246.562.10.51053050251"
 * ,"APP_ORGANISAATIOHALLINTA_READ_UPDATE_1.2.246.562.10.51053050251"
 * ,"APP_HENKILONHALLINTA_READ_1.2.246.562.10.51053050251"],"lang":"sv"}
 */
public class UserInfoResource extends AbstractController {

    private TicketRegistry ticketRegistry;
    private CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;
    private AuthenticationUtil authenticationUtil;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        String ticketGrantingTicketId = ticketGrantingTicketCookieGenerator
                .retrieveCookieValue(request);
        TicketGrantingTicket ticketGrantingTicket = (TicketGrantingTicket) this.ticketRegistry
                .getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);
        final String uid = ticketGrantingTicket != null
                && ticketGrantingTicket.getAuthentication() != null ? ticketGrantingTicket
                .getAuthentication().getPrincipal().getId()
                : null;

        return new ModelAndView(new View() {
            @Override
            public String getContentType() {
                return "text/json";
            }

            @Override
            public void render(Map<String, ?> stringMap,
                    HttpServletRequest request, HttpServletResponse response)
                    throws Exception {
                if (uid == null) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                LdapUser user = authenticationUtil.getUser(uid);
                final List<String> roles = authenticationUtil.getRoles(uid);
                user.setGroups(roles.toArray(new String[roles.size()]));
                user.setUid(uid);
                Gson gson = new Gson();
                gson.toJson(user, response.getWriter());
            }
        });
    }

    public void setTicketGrantingTicketCookieGenerator(
            CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator) {
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
    }

    public void setTicketRegistry(TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }

    public void setAuthenticationUtil(AuthenticationUtil authenticationUtil) {
        this.authenticationUtil = authenticationUtil;
    }
}
