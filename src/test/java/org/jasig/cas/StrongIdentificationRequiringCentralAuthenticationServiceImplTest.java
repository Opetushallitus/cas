package org.jasig.cas;

import fi.vm.sade.auth.clients.KayttooikeusRestClient;
import fi.vm.sade.properties.OphProperties;
import java.util.ArrayList;
import java.util.Collection;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.BasicCredentialMetaData;
import org.jasig.cas.authentication.CredentialMetaData;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.logout.LogoutManager;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.ticket.TicketFactory;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class StrongIdentificationRequiringCentralAuthenticationServiceImplTest {

    private StrongIdentificationRequiringCentralAuthenticationServiceImpl strongIdentificationRequiringCentralAuthenticationService;

    private Collection<CredentialMetaData> credentials = new ArrayList<>();

    private TicketRegistry ticketRegistry;

    private TicketFactory ticketFactory;

    private ServicesManager servicesManager;

    private LogoutManager logoutManager;

    private KayttooikeusRestClient kayttooikeusRestClient;

    private OphProperties ophProperties;

    @Before
    public void setup() {
        this.ticketRegistry = mock(TicketRegistry.class);
        this.ticketFactory = mock(TicketFactory.class);
        this.servicesManager = mock(ServicesManager.class);
        this.logoutManager = mock(LogoutManager.class);
        this.kayttooikeusRestClient = mock(KayttooikeusRestClient.class);
        this.ophProperties = mock(OphProperties.class);

        UsernamePasswordCredential usernamePasswordCredential = new UsernamePasswordCredential();
        usernamePasswordCredential.setUsername("username");
        usernamePasswordCredential.setPassword("password");
        this.credentials.add(new BasicCredentialMetaData(usernamePasswordCredential));

        this.strongIdentificationRequiringCentralAuthenticationService = new StrongIdentificationRequiringCentralAuthenticationServiceImpl(
                ticketRegistry, ticketFactory, servicesManager, logoutManager);
        this.strongIdentificationRequiringCentralAuthenticationService.setKayttooikeusClient(this.kayttooikeusRestClient);
        this.strongIdentificationRequiringCentralAuthenticationService.setOphProperties(this.ophProperties);
        this.strongIdentificationRequiringCentralAuthenticationService.setRequireStrongIdentification(true);
        this.strongIdentificationRequiringCentralAuthenticationService.setCasRequireStrongIdentificationListAsString("");
    }

    @Test
    public void emptyIsStronglyIdentified() throws Exception {
        when(this.kayttooikeusRestClient.get(anyString(), eq(Boolean.class))).thenReturn(true);
        this.strongIdentificationRequiringCentralAuthenticationService.checkStrongIdentificationHook(this.credentials);
        verify(this.kayttooikeusRestClient, times(1)).get(anyString(), eq(Boolean.class));
    }

    @Test(expected = AuthenticationException.class)
    public void emptyIsNotStronglyIdentified() throws Exception {
        when(this.kayttooikeusRestClient.get(anyString(), eq(Boolean.class))).thenReturn(false);
        this.strongIdentificationRequiringCentralAuthenticationService.checkStrongIdentificationHook(this.credentials);
        verify(this.kayttooikeusRestClient, times(1)).get(anyString(), eq(Boolean.class));
    }

    @Test
    public void defaultValueIsStronglyIdentified() throws Exception {
        when(this.kayttooikeusRestClient.get(anyString(), eq(Boolean.class))).thenReturn(true);
        this.strongIdentificationRequiringCentralAuthenticationService.checkStrongIdentificationHook(this.credentials);
        verify(this.kayttooikeusRestClient, times(1)).get(anyString(), eq(Boolean.class));
    }

    @Test(expected = AuthenticationException.class)
    public void defaultValueIsNotStronglyIdentified() throws Exception {
        when(this.kayttooikeusRestClient.get(anyString(), eq(Boolean.class))).thenReturn(false);
        this.strongIdentificationRequiringCentralAuthenticationService.checkStrongIdentificationHook(this.credentials);
        verify(this.kayttooikeusRestClient, times(1)).get(anyString(), eq(Boolean.class));
    }

    @Test
    public void usernameNotFound() throws Exception {
        this.strongIdentificationRequiringCentralAuthenticationService.setRequireStrongIdentification(false);
        this.strongIdentificationRequiringCentralAuthenticationService.setCasRequireStrongIdentificationListAsString("username1,username2");
        this.strongIdentificationRequiringCentralAuthenticationService.checkStrongIdentificationHook(this.credentials);
        verifyZeroInteractions(this.kayttooikeusRestClient);
    }

    @Test
    public void usernameFoundIsStronglyIdentified() throws Exception {
        when(this.kayttooikeusRestClient.get(anyString(), eq(Boolean.class))).thenReturn(true);
        this.strongIdentificationRequiringCentralAuthenticationService.setCasRequireStrongIdentificationListAsString("username,username2");
        this.strongIdentificationRequiringCentralAuthenticationService.checkStrongIdentificationHook(this.credentials);
        verify(this.kayttooikeusRestClient, times(1)).get(anyString(), eq(Boolean.class));
    }

    @Test(expected = AuthenticationException.class)
    public void usernameFoundIsNotStronglyIdentified() throws Exception {
        when(this.kayttooikeusRestClient.get(anyString(), eq(Boolean.class))).thenReturn(false);
        this.strongIdentificationRequiringCentralAuthenticationService.setCasRequireStrongIdentificationListAsString("username,username2");
        this.strongIdentificationRequiringCentralAuthenticationService.checkStrongIdentificationHook(this.credentials);
        verify(this.kayttooikeusRestClient, times(1)).get(anyString(), eq(Boolean.class));
    }
}
