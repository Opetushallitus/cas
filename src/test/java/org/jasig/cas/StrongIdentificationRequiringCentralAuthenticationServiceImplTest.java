package org.jasig.cas;

import fi.vm.sade.auth.clients.KayttooikeusRestClient;
import fi.vm.sade.auth.exception.NoStrongIdentificationException;
import fi.vm.sade.properties.OphProperties;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = StrongIdentificationRequiringCentralAuthenticationServiceImpl.class)
public class StrongIdentificationRequiringCentralAuthenticationServiceImplTest {
    @Autowired
    StrongIdentificationRequiringCentralAuthenticationServiceImpl strongIdentificationRequiringCentralAuthenticationService;

    private UsernamePasswordCredentials usernamePasswordCredentials;

    private KayttooikeusRestClient kayttooikeusRestClient;

    private OphProperties ophProperties;

    @Before
    public void setup() {
        this.kayttooikeusRestClient = mock(KayttooikeusRestClient.class);
        this.ophProperties = mock(OphProperties.class);

        this.usernamePasswordCredentials = new UsernamePasswordCredentials();
        this.usernamePasswordCredentials.setUsername("username");
        this.usernamePasswordCredentials.setPassword("password");

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

    @Test(expected = NoStrongIdentificationException.class)
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

    @Test(expected = NoStrongIdentificationException.class)
    public void defaultValueIsNotStronglyIdentified() throws Exception {
        when(this.kayttooikeusRestClient.get(anyString(), eq(Boolean.class))).thenReturn(false);
        this.strongIdentificationRequiringCentralAuthenticationService.checkStrongIdentificationHook(this.usernamePasswordCredentials);
        verify(this.kayttooikeusRestClient, times(1)).get(anyString(), eq(Boolean.class));
    }

    @Test
    public void usernameNotFound() throws Exception {
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

    @Test(expected = NoStrongIdentificationException.class)
    public void usernameFoundIsNotStronglyIdentified() throws Exception {
        when(this.kayttooikeusRestClient.get(anyString(), eq(Boolean.class))).thenReturn(false);
        this.strongIdentificationRequiringCentralAuthenticationService.setCasRequireStrongIdentificationListAsString("username,username2");
        this.strongIdentificationRequiringCentralAuthenticationService.checkStrongIdentificationHook(this.usernamePasswordCredentials);
        verify(this.kayttooikeusRestClient, times(1)).get(anyString(), eq(Boolean.class));
    }
}
