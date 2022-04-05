package fi.vm.sade.auth.cas;

import org.apereo.cas.authentication.DefaultAuthentication;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.ticket.*;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.factory.DefaultTicketGrantingTicketFactory;
import org.apereo.cas.ticket.factory.DefaultTransientSessionTicketFactory;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.ZonedDateTime;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

public class JacksonTicketSerializerTest {

    private TicketSerializer ticketSerializer;
    private Service testService;

    @Before
    public void setup() {
        ticketSerializer = new JacksonTicketSerializer();

        ApplicationEventPublisher fakeEventPublisher = event -> {
        };
        testService = new WebApplicationServiceFactory().createService("service123");
    }

    @Test
    public void serializationWorksWithTickets() {
        // ticket granting ticket
        TicketGrantingTicketFactory ticketGrantingTicketFactory = new DefaultTicketGrantingTicketFactory(
                new DefaultUniqueTicketIdGenerator(),
                getNeverExpiringTicketGrantingTicketExpirationPolicyBuilder(),
                CipherExecutor.noOpOfSerializableToString());
        TicketGrantingTicket ticketGrantingTicket =
                ticketGrantingTicketFactory.create(new DefaultAuthentication(ZonedDateTime.now(), new NullPrincipal()
                        , emptyMap(), emptyMap(), emptyList()), TicketGrantingTicket.class);

        String ticketGrantingTicketAsJson = ticketSerializer.toJson(ticketGrantingTicket);
        System.out.println(ticketGrantingTicketAsJson);
        Ticket ticketGrantingTicketFromJson = ticketSerializer.fromJson(ticketGrantingTicketAsJson, null);
        assertThat(ticketGrantingTicketFromJson).isInstanceOf(TicketGrantingTicket.class).isEqualByComparingTo(ticketGrantingTicket);

        ticketGrantingTicket.markTicketExpired();
        String expiredTicketGrantingTicketAsJson = ticketSerializer.toJson(ticketGrantingTicket);

        // transient session ticket
        TransientSessionTicketFactory transientSessionTicketFactory =
                new DefaultTransientSessionTicketFactory(getNeverExpiringTransientSessionTicketExpirationPolicyBuilder());
        TransientSessionTicket transientSessionTicket = transientSessionTicketFactory.create(testService);

        String transientSessionTicketAsJson = ticketSerializer.toJson(transientSessionTicket);
        System.out.println(transientSessionTicketAsJson);
        Ticket transientSessionTicketFromJson = ticketSerializer.fromJson(transientSessionTicketAsJson, null);
        assertThat(transientSessionTicketFromJson).isInstanceOf(TransientSessionTicket.class).isEqualByComparingTo(transientSessionTicket).returns(null, Ticket::getTicketGrantingTicket);
    }

    private ExpirationPolicyBuilder<TicketGrantingTicket> getNeverExpiringTicketGrantingTicketExpirationPolicyBuilder() {
        return new ExpirationPolicyBuilder<>() {
            @Override
            public ExpirationPolicy buildTicketExpirationPolicy() {
                return NeverExpiresExpirationPolicy.INSTANCE;
            }

            @Override
            public Class<TicketGrantingTicket> getTicketType() {
                return TicketGrantingTicket.class;
            }
        };
    }

    private ExpirationPolicyBuilder<TransientSessionTicket> getNeverExpiringTransientSessionTicketExpirationPolicyBuilder() {
        return new ExpirationPolicyBuilder<>() {
            @Override
            public ExpirationPolicy buildTicketExpirationPolicy() {
                return NeverExpiresExpirationPolicy.INSTANCE;
            }

            @Override
            public Class<TransientSessionTicket> getTicketType() {
                return TransientSessionTicket.class;
            }
        };
    }
}
