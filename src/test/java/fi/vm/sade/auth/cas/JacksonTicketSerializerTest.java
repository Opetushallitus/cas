package fi.vm.sade.auth.cas;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.DefaultAuthentication;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.ticket.*;
import org.apereo.cas.ticket.factory.*;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicketFactory;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.ticket.proxy.ProxyTicketFactory;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.junit.Before;
import org.junit.Test;

import java.time.ZonedDateTime;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

public class JacksonTicketSerializerTest {

    private TicketSerializer ticketSerializer;

    @Before
    public void setup() {
        ticketSerializer = new JacksonTicketSerializer();
    }

    @Test
    public void serializationWorksWithTickets() {
        // ticket granting ticket
        TicketGrantingTicketFactory ticketGrantingTicketFactory = new DefaultTicketGrantingTicketFactory(
                new DefaultUniqueTicketIdGenerator(), new NeverExpiresExpirationPolicy(), CipherExecutor.noOpOfSerializableToString());
        TicketGrantingTicket ticketGrantingTicket = ticketGrantingTicketFactory.create(
                new DefaultAuthentication(ZonedDateTime.now(), new NullPrincipal(), emptyMap(), emptyMap()), TicketGrantingTicket.class);

        String ticketGrantingTicketAsJson = ticketSerializer.toJson(ticketGrantingTicket);
        System.out.println(ticketGrantingTicketAsJson);
        Ticket ticketGrantingTicketFromJson = ticketSerializer.fromJson(ticketGrantingTicketAsJson, Ticket.class);
        assertThat(ticketGrantingTicketFromJson).isInstanceOf(TicketGrantingTicket.class).isEqualByComparingTo(ticketGrantingTicket);

        // service ticket
        ServiceTicketFactory ticketFactory = new DefaultServiceTicketFactory(
                new NeverExpiresExpirationPolicy(), emptyMap(), true, CipherExecutor.noOpOfStringToString());
        Service service = new WebApplicationServiceFactory().createService("service123");
        ServiceTicket serviceTicket = ticketFactory.create((TicketGrantingTicket) ticketGrantingTicketFromJson, service, true, ServiceTicket.class);

        String serviceTicketAsJson = ticketSerializer.toJson(serviceTicket);
        System.out.println(serviceTicketAsJson);
        Ticket serviceTicketFromJson = ticketSerializer.fromJson(serviceTicketAsJson, Ticket.class);
        assertThat(serviceTicketFromJson).isInstanceOf(ServiceTicket.class).isEqualByComparingTo(serviceTicket);

        // proxy granting ticket
        ProxyGrantingTicketFactory proxyGrantingTicketFactory = new DefaultProxyGrantingTicketFactory(
                new DefaultUniqueTicketIdGenerator(), new NeverExpiresExpirationPolicy(), CipherExecutor.noOpOfStringToString());
        ProxyGrantingTicket proxyGrantingTicket = proxyGrantingTicketFactory.create((ServiceTicket) serviceTicketFromJson, new DefaultAuthentication(ZonedDateTime.now(), new NullPrincipal(), emptyMap(), emptyMap()), ProxyGrantingTicket.class);

        String proxyGrantingTicketAsJson = ticketSerializer.toJson(proxyGrantingTicket);
        System.out.println(proxyGrantingTicketAsJson);
        Ticket proxyGrantingTicketFromJson = ticketSerializer.fromJson(proxyGrantingTicketAsJson, Ticket.class);
        assertThat(proxyGrantingTicketFromJson).isInstanceOf(ProxyGrantingTicket.class).isEqualByComparingTo(proxyGrantingTicket);

        // proxy ticket
        ProxyTicketFactory proxyTicketFactory = new DefaultProxyTicketFactory(
                new NeverExpiresExpirationPolicy(), emptyMap(), CipherExecutor.noOpOfStringToString(), true);
        ProxyTicket proxyTicket = proxyTicketFactory.create((ProxyGrantingTicket) proxyGrantingTicketFromJson, service, ProxyTicket.class);

        String proxyTicketAsJson = ticketSerializer.toJson(proxyTicket);
        System.out.println(proxyTicketAsJson);
        Ticket proxyTicketFromJson = ticketSerializer.fromJson(proxyTicketAsJson, Ticket.class);
        assertThat(proxyTicketFromJson).isInstanceOf(ProxyTicket.class).isEqualByComparingTo(proxyTicket);

        // transient session ticket
        TransientSessionTicketFactory transientSessionTicketFactory = new DefaultTransientSessionTicketFactory(new NeverExpiresExpirationPolicy());
        TransientSessionTicket transientSessionTicket = transientSessionTicketFactory.create(service);

        String transientSessionTicketAsJson = ticketSerializer.toJson(transientSessionTicket);
        System.out.println(transientSessionTicketAsJson);
        Ticket transientSessionTicketFromJson = ticketSerializer.fromJson(transientSessionTicketAsJson, Ticket.class);
        assertThat(transientSessionTicketFromJson).isInstanceOf(TransientSessionTicket.class).isEqualByComparingTo(transientSessionTicket);
    }

}
