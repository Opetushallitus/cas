package org.jasig.cas;

import fi.vm.sade.auth.clients.KayttooikeusRestClient;
import fi.vm.sade.properties.OphProperties;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.AuthenticationManager;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.logout.LogoutManager;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.util.UniqueTicketIdGenerator;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class StrongIdentificationRequiringCentralAuthenticationServiceImplTest {

    private StrongIdentificationRequiringCentralAuthenticationServiceImpl strongIdentificationRequiringCentralAuthenticationService;

    private Set<UsernamePasswordCredential> usernamePasswordCredentials = new LinkedHashSet<>();

    private TicketRegistry ticketRegistry;

    private AuthenticationManager authenticationManager;

    private UniqueTicketIdGenerator ticketGrantingTicketUniqueTicketIdGenerator;

    private Map<String, UniqueTicketIdGenerator> uniqueTicketIdGeneratorsForService;

    private ExpirationPolicy ticketGrantingTicketExpirationPolicy;

    private ExpirationPolicy serviceTicketExpirationPolicy;

    private ServicesManager servicesManager;

    private LogoutManager logoutManager;

    private KayttooikeusRestClient kayttooikeusRestClient;

    private OphProperties ophProperties;

    @Before
    public void setup() {
        this.ticketRegistry = mock(TicketRegistry.class);
        this.authenticationManager = mock(AuthenticationManager.class);
        this.ticketGrantingTicketUniqueTicketIdGenerator = mock(UniqueTicketIdGenerator.class);
        this.uniqueTicketIdGeneratorsForService = new LinkedHashMap<>();
        this.ticketGrantingTicketExpirationPolicy = mock(ExpirationPolicy.class);
        this.serviceTicketExpirationPolicy = mock(ExpirationPolicy.class);
        this.servicesManager = mock(ServicesManager.class);
        this.logoutManager = mock(LogoutManager.class);
        this.kayttooikeusRestClient = mock(KayttooikeusRestClient.class);
        this.ophProperties = mock(OphProperties.class);

        UsernamePasswordCredential usernamePasswordCredential = new UsernamePasswordCredential();
        usernamePasswordCredential.setUsername("username");
        usernamePasswordCredential.setPassword("password");
        this.usernamePasswordCredentials.add(usernamePasswordCredential);

        this.strongIdentificationRequiringCentralAuthenticationService = new StrongIdentificationRequiringCentralAuthenticationServiceImpl(
                ticketRegistry, authenticationManager,
                ticketGrantingTicketUniqueTicketIdGenerator,
                uniqueTicketIdGeneratorsForService,
                ticketGrantingTicketExpirationPolicy,
                serviceTicketExpirationPolicy, servicesManager, logoutManager);
        this.strongIdentificationRequiringCentralAuthenticationService.setKayttooikeusClient(this.kayttooikeusRestClient);
        this.strongIdentificationRequiringCentralAuthenticationService.setOphProperties(this.ophProperties);
        this.strongIdentificationRequiringCentralAuthenticationService.setRequireStrongIdentification(true);
        this.strongIdentificationRequiringCentralAuthenticationService.setCasRequireStrongIdentificationListAsString("");
    }

    @Test
    public void emptyIsStronglyIdentified() throws Exception {
        when(this.kayttooikeusRestClient.get(anyString(), eq(Boolean.class))).thenReturn(true);
        this.strongIdentificationRequiringCentralAuthenticationService.checkStrongIdentificationHook(this.usernamePasswordCredentials);
        verify(this.kayttooikeusRestClient, times(1)).get(anyString(), eq(Boolean.class));
    }

    @Test(expected = AuthenticationException.class)
    public void emptyIsNotStronglyIdentified() throws Exception {
        when(this.kayttooikeusRestClient.get(anyString(), eq(Boolean.class))).thenReturn(false);
        this.strongIdentificationRequiringCentralAuthenticationService.checkStrongIdentificationHook(this.usernamePasswordCredentials);
        verify(this.kayttooikeusRestClient, times(1)).get(anyString(), eq(Boolean.class));
    }

    @Test
    public void defaultValueIsStronglyIdentified() throws Exception {
        when(this.kayttooikeusRestClient.get(anyString(), eq(Boolean.class))).thenReturn(true);
        this.strongIdentificationRequiringCentralAuthenticationService.checkStrongIdentificationHook(this.usernamePasswordCredentials);
        verify(this.kayttooikeusRestClient, times(1)).get(anyString(), eq(Boolean.class));
    }

    @Test(expected = AuthenticationException.class)
    public void defaultValueIsNotStronglyIdentified() throws Exception {
        when(this.kayttooikeusRestClient.get(anyString(), eq(Boolean.class))).thenReturn(false);
        this.strongIdentificationRequiringCentralAuthenticationService.checkStrongIdentificationHook(this.usernamePasswordCredentials);
        verify(this.kayttooikeusRestClient, times(1)).get(anyString(), eq(Boolean.class));
    }

    @Test
    public void usernameNotFound() throws Exception {
        this.strongIdentificationRequiringCentralAuthenticationService.setRequireStrongIdentification(false);
        this.strongIdentificationRequiringCentralAuthenticationService.setCasRequireStrongIdentificationListAsString("username1,username2");
        this.strongIdentificationRequiringCentralAuthenticationService.checkStrongIdentificationHook(this.usernamePasswordCredentials);
        verifyZeroInteractions(this.kayttooikeusRestClient);
    }

    @Test
    public void usernameFoundIsStronglyIdentified() throws Exception {
        when(this.kayttooikeusRestClient.get(anyString(), eq(Boolean.class))).thenReturn(true);
        this.strongIdentificationRequiringCentralAuthenticationService.setCasRequireStrongIdentificationListAsString("username,username2");
        this.strongIdentificationRequiringCentralAuthenticationService.checkStrongIdentificationHook(this.usernamePasswordCredentials);
        verify(this.kayttooikeusRestClient, times(1)).get(anyString(), eq(Boolean.class));
    }

    @Test(expected = AuthenticationException.class)
    public void usernameFoundIsNotStronglyIdentified() throws Exception {
        when(this.kayttooikeusRestClient.get(anyString(), eq(Boolean.class))).thenReturn(false);
        this.strongIdentificationRequiringCentralAuthenticationService.setCasRequireStrongIdentificationListAsString("username,username2");
        this.strongIdentificationRequiringCentralAuthenticationService.checkStrongIdentificationHook(this.usernamePasswordCredentials);
        verify(this.kayttooikeusRestClient, times(1)).get(anyString(), eq(Boolean.class));
    }
}
