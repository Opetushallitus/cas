package fi.vm.sade.auth.cas;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import lombok.SneakyThrows;
import lombok.val;
import org.apereo.cas.authentication.DefaultAuthentication;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.authentication.principal.cache.CachingPrincipalAttributesRepository;
import org.apereo.cas.services.*;
import org.apereo.cas.services.support.RegisteredServiceRegexAttributeFilter;
import org.apereo.cas.ticket.*;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.factory.*;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicketFactory;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.ticket.proxy.ProxyTicketFactory;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.time.ZonedDateTime.now;
import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.assertThat;

public class JacksonTicketSerializerTest {

    private TicketSerializer ticketSerializer;
    private ServicesManager servicesManager;
    private final Service service = new WebApplicationServiceFactory().createService("service123");

    private final RegisteredService registeredService = getRegisteredService("regSer123", RegexRegisteredService.class,
            true, new HashMap<>());

    // from cas /RegisteredServiceTestUtils.java
    @SneakyThrows
    public static AbstractRegisteredService getRegisteredService(final String id,
                                                                 final Class<? extends RegisteredService> clazz,
                                                                 final boolean uniq,
                                                                 final Map<String, Set<String>> requiredAttributes) {
        val s = (AbstractRegisteredService) clazz.getDeclaredConstructor().newInstance();
        s.setServiceId(id);
        s.setEvaluationOrder(1);
        if (uniq) {
            val uuid = Iterables.get(Splitter.on('-').split(UUID.randomUUID().toString()), 0);
            s.setName("TestService" + uuid);
        } else {
            s.setName(id);
        }
        s.setDescription("Registered service description");
        s.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy("^https?://.+"));
        s.setId(RandomUtils.nextInt());
        s.setTheme("exampleTheme");
        s.setUsernameAttributeProvider(new PrincipalAttributeRegisteredServiceUsernameProvider("uid"));
        val accessStrategy = new DefaultRegisteredServiceAccessStrategy(true, true);
        accessStrategy.setRequireAllAttributes(true);
        accessStrategy.setRequiredAttributes(requiredAttributes);
        accessStrategy.setUnauthorizedRedirectUrl(new URI("https://www.github.com"));
        s.setAccessStrategy(accessStrategy);
        s.setLogo("https://logo.example.org/logo.png");
        s.setLogoutType(RegisteredServiceLogoutType.BACK_CHANNEL);
        s.setLogoutUrl("https://sys.example.org/logout.png");
        s.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy("^http.+"));

        s.setPublicKey(new RegisteredServicePublicKeyImpl("classpath:RSA1024Public.key", "RSA"));

        val policy = new ReturnAllowedAttributeReleasePolicy();
        policy.setAuthorizedToReleaseCredentialPassword(true);
        policy.setAuthorizedToReleaseProxyGrantingTicket(true);

        val repo = new CachingPrincipalAttributesRepository(TimeUnit.SECONDS.name(), 10);
        policy.setPrincipalAttributesRepository(repo);
        policy.setAttributeFilter(new RegisteredServiceRegexAttributeFilter("https://.+"));
        s.setAttributeReleasePolicy(policy);

        return s;
    }


    @Before
    public void setup() {
        ticketSerializer = new JacksonTicketSerializer();

        ConfigurableApplicationContext fakeApplicationContext = new StaticApplicationContext();

        ServiceRegistry serviceRegistry = new ImmutableInMemoryServiceRegistry(List.of(registeredService),
                fakeApplicationContext,
                emptySet());
        /*serviceRegistry,fakeApplicationContext, emptySet()*/
        ServicesManagerConfigurationContext configurationContext =
                ServicesManagerConfigurationContext.builder()
                        .serviceRegistry(serviceRegistry)
                        .applicationContext(fakeApplicationContext)
                        .environments(emptySet())
                        .servicesCache(Caffeine.newBuilder().build())
                        .build();
        servicesManager = new DefaultServicesManager(configurationContext);
    }

    @Test
    public void serializationWorksWithTickets() {
        // ticket granting ticket
        TicketGrantingTicketFactory ticketGrantingTicketFactory = new DefaultTicketGrantingTicketFactory(
                new DefaultUniqueTicketIdGenerator(),
                getNeverExpiringTicketGrantingTicketExpirationPolicyBuilder(),
                CipherExecutor.noOpOfSerializableToString(), servicesManager);
        TicketGrantingTicket ticketGrantingTicket =
                ticketGrantingTicketFactory.create(new DefaultAuthentication(now(), new NullPrincipal(), emptyMap(),
                                emptyMap(), emptyList()),
                        service, TicketGrantingTicket.class);

        String ticketGrantingTicketAsJson = ticketSerializer.toJson(ticketGrantingTicket);
        System.out.println(ticketGrantingTicketAsJson);
        Ticket ticketGrantingTicketFromJson = ticketSerializer.fromJson(ticketGrantingTicketAsJson, null);
        assertThat(ticketGrantingTicketFromJson)
                .isInstanceOf(TicketGrantingTicket.class)
                .isEqualByComparingTo(ticketGrantingTicket);

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
        assertThat(serviceTicketFromJson)
                .isInstanceOf(ServiceTicket.class)
                .isEqualByComparingTo(serviceTicket)
                .returns(true, ticket -> ticket.getTicketGrantingTicket().isExpired());

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
        TransientSessionTicketFactory<TransientSessionTicket> transientSessionTicketFactory =
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
