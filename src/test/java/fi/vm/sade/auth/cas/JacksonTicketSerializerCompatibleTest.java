package fi.vm.sade.auth.cas;

import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static fi.vm.sade.auth.ResourceHelper.loadAsString;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class JacksonTicketSerializerCompatibleTest {

    @Parameterized.Parameters
    public static Collection<String> directories() {
        return asList("v6.0.4");
    }

    private final TicketSerializer ticketSerializer;
    private final String directory;

    public JacksonTicketSerializerCompatibleTest(String directory) {
        this.ticketSerializer = new JacksonTicketSerializer();
        this.directory = directory;
    }

    @Test
    public void test() {
        String resourceBase = String.format("compatibility/%s", this.directory);

        String ticketGrantingTicketAsJson = loadAsString(resourceBase + "/tgt.json");
        Ticket ticketGrantingTicket = ticketSerializer.fromJson(ticketGrantingTicketAsJson, null);
        assertThat(ticketGrantingTicket).isInstanceOf(TicketGrantingTicketImpl.class);

        String serviceTicketAsJson = loadAsString(resourceBase + "/st.json");
        Ticket serviceTicket = ticketSerializer.fromJson(serviceTicketAsJson, ticketGrantingTicketAsJson);
        assertThat(serviceTicket).isInstanceOf(ServiceTicket.class);

        String proxyGrantingTicketAsJson = loadAsString(resourceBase + "/pgt.json");
        Ticket proxyGrantingTicket = ticketSerializer.fromJson(proxyGrantingTicketAsJson, ticketGrantingTicketAsJson);
        assertThat(proxyGrantingTicket).isInstanceOf(ProxyGrantingTicket.class);

        String proxyTicketAsJson = loadAsString(resourceBase + "/pt.json");
        Ticket proxyTicket = ticketSerializer.fromJson(proxyTicketAsJson, proxyGrantingTicketAsJson);
        assertThat(proxyTicket).isInstanceOf(ProxyTicket.class);

        String transientSessionTicketAsJson = loadAsString(resourceBase + "/tst.json");
        Ticket transientSessionTicket = ticketSerializer.fromJson(transientSessionTicketAsJson, null);
        assertThat(transientSessionTicket).isInstanceOf(TransientSessionTicket.class);
    }

}
