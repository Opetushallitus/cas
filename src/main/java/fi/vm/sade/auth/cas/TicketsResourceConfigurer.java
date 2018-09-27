package fi.vm.sade.auth.cas;

import lombok.SneakyThrows;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.support.rest.TicketsResource;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.registry.TicketRegistrySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class TicketsResourceConfigurer implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketsResourceConfigurer.class);

    private final TicketsResource ticketsResource;

    @Autowired
    public TicketsResourceConfigurer(TicketsResource ticketsResource) {
        this.ticketsResource = ticketsResource;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        LOGGER.info("Configuring TicketsResource");
        ticketsResource.setTicketRegistrySupport(new TicketRegistrySupportWrapper(ticketsResource.getTicketRegistrySupport()));
    }

    protected static class TicketRegistrySupportWrapper implements TicketRegistrySupport {

        private final TicketRegistrySupport ticketRegistrySupport;

        public TicketRegistrySupportWrapper(TicketRegistrySupport ticketRegistrySupport) {
            this.ticketRegistrySupport = ticketRegistrySupport;
        }

        @Override
        @SneakyThrows(InvalidTicketException.class)
        public Authentication getAuthenticationFrom(String ticketGrantingTicketId) {
            Authentication authentication = ticketRegistrySupport.getAuthenticationFrom(ticketGrantingTicketId);
            if (authentication == null) {
                throw new InvalidTicketException(ticketGrantingTicketId);
            }
            return authentication;
        }

        @Override
        public Principal getAuthenticatedPrincipalFrom(String ticketGrantingTicketId) {
            return ticketRegistrySupport.getAuthenticatedPrincipalFrom(ticketGrantingTicketId);
        }

        @Override
        public Map<String, Object> getPrincipalAttributesFrom(String ticketGrantingTicketId) {
            return ticketRegistrySupport.getPrincipalAttributesFrom(ticketGrantingTicketId);
        }

    }

}
