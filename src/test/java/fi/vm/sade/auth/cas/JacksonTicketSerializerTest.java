package fi.vm.sade.auth.cas;

import org.apereo.cas.authentication.DefaultAuthentication;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.services.*;
import org.apereo.cas.ticket.*;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.factory.*;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicketFactory;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.ticket.proxy.ProxyTicketFactory;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.ZonedDateTime.*;
import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.assertThat;

public class JacksonTicketSerializerTest {

    private TicketSerializer ticketSerializer;
    private ServicesManager servicesManager;
    private Service service = new WebApplicationServiceFactory().createService("service123");

    private RegisteredService registeredService = new AbstractRegisteredService() {
        @Override
        public boolean matches(Service service) {
            return true;
        }
        @Override
        public boolean matches(String serviceId) {
            return true;
        }
        @Override
        public void setServiceId(String id) {
        }

        @Override
        protected AbstractRegisteredService newInstance() {
            return this;
        }
    };

    @Before
    public void setup() {
        ticketSerializer = new JacksonTicketSerializer();

        ConfigurableApplicationContext fakeApplicationContext = new StaticApplicationContext();

        ServiceRegistry serviceRegistry = new ImmutableInMemoryServiceRegistry(List.of(registeredService), fakeApplicationContext,
                emptySet());
        /*serviceRegistry,fakeApplicationContext, emptySet()*/
        ServicesManagerConfigurationContext configurationContext =
                ServicesManagerConfigurationContext.builder().serviceRegistry(serviceRegistry).applicationContext(fakeApplicationContext).build();
        servicesManager = new DefaultServicesManager(configurationContext);
    }

    @Test
    public void serializationWorksWithTickets() {
        // ticket granting ticket
        TicketGrantingTicketFactory ticketGrantingTicketFactory = new DefaultTicketGrantingTicketFactory(
                new DefaultUniqueTicketIdGenerator(),
                getNeverExpiringTicketGrantingTicketExpirationPolicyBuilder(),
                CipherExecutor.noOpOfSerializableToString(),servicesManager);
        TicketGrantingTicket ticketGrantingTicket =
                ticketGrantingTicketFactory.create(new DefaultAuthentication(), service, TicketGrantingTicket.class);

        String ticketGrantingTicketAsJson = ticketSerializer.toJson(ticketGrantingTicket);
        System.out.println(ticketGrantingTicketAsJson);
        Ticket ticketGrantingTicketFromJson = ticketSerializer.fromJson(ticketGrantingTicketAsJson, null);
        assertThat(ticketGrantingTicketFromJson).isInstanceOf(TicketGrantingTicket.class).isEqualByComparingTo(ticketGrantingTicket);

        ticketGrantingTicket.markTicketExpired();
        String expiredTicketGrantingTicketAsJson = ticketSerializer.toJson(ticketGrantingTicket);

        // service ticket
        ServiceTicketFactory ticketFactory = new DefaultServiceTicketFactory(
                getNeverExpiringServiceTicketExpirationPolicyBuilder(),
                emptyMap(),
                true,
                CipherExecutor.noOpOfStringToString(),
                servicesManager
        );

        ServiceTicket serviceTicket = ticketFactory.create((TicketGrantingTicket) ticketGrantingTicketFromJson,
                service, true, ServiceTicket.class);

        String serviceTicketAsJson = ticketSerializer.toJson(serviceTicket);
        System.out.println(serviceTicketAsJson);
        Ticket serviceTicketFromJson = ticketSerializer.fromJson(serviceTicketAsJson,
                expiredTicketGrantingTicketAsJson);
        assertThat(serviceTicketFromJson).isInstanceOf(ServiceTicket.class).isEqualByComparingTo(serviceTicket).returns(true, ticket -> ticket.getTicketGrantingTicket().isExpired());

        // proxy granting ticket
        ProxyGrantingTicketFactory proxyGrantingTicketFactory =
                new DefaultProxyGrantingTicketFactory(new DefaultUniqueTicketIdGenerator(),
                        getNeverExpiringProxyGrantingTicketExpirationPolicyBuilder(),
                        CipherExecutor.noOpOfStringToString(), servicesManager);
        ProxyGrantingTicket proxyGrantingTicket =
                proxyGrantingTicketFactory.create((ServiceTicket) serviceTicketFromJson,
                        new DefaultAuthentication(now(), new NullPrincipal(), emptyMap(), emptyMap(),
                                emptyList()), ProxyGrantingTicket.class);

        String proxyGrantingTicketAsJson = ticketSerializer.toJson(proxyGrantingTicket);
        System.out.println(proxyGrantingTicketAsJson);
        Ticket proxyGrantingTicketFromJson = ticketSerializer.fromJson(proxyGrantingTicketAsJson,
                expiredTicketGrantingTicketAsJson);
        assertThat(proxyGrantingTicketFromJson).isInstanceOf(ProxyGrantingTicket.class).isEqualByComparingTo(proxyGrantingTicket).returns(true, ticket -> ticket.getTicketGrantingTicket().isExpired());

        // proxy ticket
        ProxyTicketFactory proxyTicketFactory = new DefaultProxyTicketFactory(
                getNeverExpiringProxyTicketExpirationPolicyBuilder(),
                emptyMap(),
                CipherExecutor.noOpOfStringToString(),
                true,
                servicesManager
        );
        ProxyTicket proxyTicket = proxyTicketFactory.create((ProxyGrantingTicket) proxyGrantingTicketFromJson,
                service, ProxyTicket.class);

        String proxyTicketAsJson = ticketSerializer.toJson(proxyTicket);
        System.out.println(proxyTicketAsJson);
        Ticket proxyTicketFromJson = ticketSerializer.fromJson(proxyTicketAsJson, expiredTicketGrantingTicketAsJson);
        assertThat(proxyTicketFromJson).isInstanceOf(ProxyTicket.class).isEqualByComparingTo(proxyTicket).returns(true, ticket -> ticket.getTicketGrantingTicket().isExpired());

        // transient session ticket
        TransientSessionTicketFactory transientSessionTicketFactory =
                new DefaultTransientSessionTicketFactory(getNeverExpiringTransientSessionTicketExpirationPolicyBuilder());
        TransientSessionTicket transientSessionTicket = transientSessionTicketFactory.create(service);

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


    private ExpirationPolicyBuilder<ProxyGrantingTicket> getNeverExpiringProxyGrantingTicketExpirationPolicyBuilder() {
        return new ExpirationPolicyBuilder<>() {
            @Override
            public ExpirationPolicy buildTicketExpirationPolicy() {
                return NeverExpiresExpirationPolicy.INSTANCE;
            }

            @Override
            public Class<ProxyGrantingTicket> getTicketType() {
                return ProxyGrantingTicket.class;
            }
        };
    }

    private ExpirationPolicyBuilder<ProxyTicket> getNeverExpiringProxyTicketExpirationPolicyBuilder() {
        return new ExpirationPolicyBuilder<>() {
            @Override
            public ExpirationPolicy buildTicketExpirationPolicy() {
                return NeverExpiresExpirationPolicy.INSTANCE;
            }

            @Override
            public Class<ProxyTicket> getTicketType() {
                return ProxyTicket.class;
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

    private ExpirationPolicyBuilder<ServiceTicket> getNeverExpiringServiceTicketExpirationPolicyBuilder() {
        return new ExpirationPolicyBuilder<>() {
            @Override
            public ExpirationPolicy buildTicketExpirationPolicy() {
                return NeverExpiresExpirationPolicy.INSTANCE;
            }

            @Override
            public Class<ServiceTicket> getTicketType() {
                return ServiceTicket.class;
            }
        };
    }

}
