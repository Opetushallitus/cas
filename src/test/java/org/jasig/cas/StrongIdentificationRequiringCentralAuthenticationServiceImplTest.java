package org.jasig.cas;

import fi.vm.sade.auth.clients.KayttooikeusRestClient;
import fi.vm.sade.properties.OphProperties;

import java.io.IOException;
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
import static org.assertj.core.api.Assertions.assertThat;

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
        this.strongIdentificationRequiringCentralAuthenticationService.setEmailVerificationEnabled(true);
        this.strongIdentificationRequiringCentralAuthenticationService.setEmailVerificationUsernamesAsString("");
    }

    @Test
    public void onRedirectCodeNullShouldNotRedirect() throws Exception {
        when(this.kayttooikeusRestClient.get(anyString(), eq(String.class))).thenReturn(null);
        this.strongIdentificationRequiringCentralAuthenticationService.checkStrongIdentificationHook(this.credentials);
        verify(this.kayttooikeusRestClient, times(1)).get(anyString(), eq(String.class));
    }

    @Test
    public void onUsernameInCasRequireStrongidentificationListShouldRedirectToStrongAuthentication() throws Exception {
        this.strongIdentificationRequiringCentralAuthenticationService.setRequireStrongIdentification(false);
        this.strongIdentificationRequiringCentralAuthenticationService.setCasRequireStrongIdentificationListAsString("username,username2");
        when(this.kayttooikeusRestClient.get(anyString(), eq(String.class))).thenReturn(this.strongIdentificationRequiringCentralAuthenticationService.STRONG_IDENTIFICATION);
        String thrownExceptionSimpleName = null;
        try {
            this.strongIdentificationRequiringCentralAuthenticationService.checkStrongIdentificationHook(this.credentials);
        } catch(AuthenticationException e) {
            thrownExceptionSimpleName = e.getHandlerErrors().get("org.jasig.cas.StrongIdentificationRequiringCentralAuthenticationServiceImpl")
                    .getSimpleName();
            assertThat(thrownExceptionSimpleName).isEqualTo("NoStrongIdentificationException");
        }
        assertThat(thrownExceptionSimpleName).isNotNull();
    }

    @Test
    public void onRequireStrongIdentificationAndStrongIdentificationRedirectCodeShouldRedirectToStrongIdentification() throws Exception {
        when(this.kayttooikeusRestClient.get(any(), eq(String.class))).thenReturn(this.strongIdentificationRequiringCentralAuthenticationService.STRONG_IDENTIFICATION);
        String thrownExceptionSimpleName = null;
        try {
            this.strongIdentificationRequiringCentralAuthenticationService.checkStrongIdentificationHook(this.credentials);
        } catch(AuthenticationException e) {
            thrownExceptionSimpleName = e.getHandlerErrors().get("org.jasig.cas.StrongIdentificationRequiringCentralAuthenticationServiceImpl")
                    .getSimpleName();
            assertThat(thrownExceptionSimpleName).isEqualTo("NoStrongIdentificationException");
        }
        assertThat(thrownExceptionSimpleName).isNotNull();
        verify(this.kayttooikeusRestClient, times(1)).get(anyString(), eq(String.class));
    }

    @Test
    public void onFailedRedirectCodeCallShouldThrowFailedLoginException() throws Exception {
        when(this.kayttooikeusRestClient.get(any(), eq(String.class))).thenThrow(new IOException("failed fetch"));
        String thrownExceptionSimpleName = null;
        try {
            this.strongIdentificationRequiringCentralAuthenticationService.checkStrongIdentificationHook(this.credentials);
        } catch (AuthenticationException e) {
            thrownExceptionSimpleName = e.getHandlerErrors().get("org.jasig.cas.StrongIdentificationRequiringCentralAuthenticationServiceImpl")
                    .getSimpleName();
            assertThat(thrownExceptionSimpleName).isEqualTo("FailedLoginException");
        }
        assertThat(thrownExceptionSimpleName).isNotNull();
        verify(this.kayttooikeusRestClient, times(1)).get(anyString(), eq(String.class));
    }

    @Test
    public void onEmailVerificationEnabledAndEmailVerificationRedirectCodeShouldRedirectToEmailVerification() throws Exception {
        this.strongIdentificationRequiringCentralAuthenticationService.setEmailVerificationEnabled(true);
        when(this.kayttooikeusRestClient.get(any(), eq(String.class))).thenReturn(this.strongIdentificationRequiringCentralAuthenticationService.EMAIL_VERIFICATION);
        String thrownExceptionSimpleName = null;
        try {
            this.strongIdentificationRequiringCentralAuthenticationService.checkStrongIdentificationHook(this.credentials);
        } catch (AuthenticationException e) {
            thrownExceptionSimpleName = e.getHandlerErrors().get("org.jasig.cas.StrongIdentificationRequiringCentralAuthenticationServiceImpl")
                    .getSimpleName();
            assertThat(thrownExceptionSimpleName).isEqualTo("EmailVerificationException");
        }
        assertThat(thrownExceptionSimpleName).isNotNull();
        verify(this.kayttooikeusRestClient, times(1)).get(anyString(), eq(String.class));
    }

    @Test
    public void onEmailVerificationDisabledAndUsernameInEmailVerificationListShouldRedirectToEmailVerification() throws Exception {
        this.strongIdentificationRequiringCentralAuthenticationService.setEmailVerificationUsernamesAsString("username,username2");
        when(this.kayttooikeusRestClient.get(any(), eq(String.class))).thenReturn(this.strongIdentificationRequiringCentralAuthenticationService.EMAIL_VERIFICATION);
        String throwExceptionSimpleName = null;
        try {
            this.strongIdentificationRequiringCentralAuthenticationService.checkStrongIdentificationHook(this.credentials);
        } catch(AuthenticationException e) {
            throwExceptionSimpleName = e.getHandlerErrors().get("org.jasig.cas.StrongIdentificationRequiringCentralAuthenticationServiceImpl")
                    .getSimpleName();
            assertThat(throwExceptionSimpleName).isEqualTo("EmailVerificationException");
        }
    }

    @Test
    public void onStrongIdentificationAndEmailVerificationDisabledShouldNotRedirect() throws Exception {
        this.strongIdentificationRequiringCentralAuthenticationService.setRequireStrongIdentification(false);
        this.strongIdentificationRequiringCentralAuthenticationService.setEmailVerificationEnabled(false);
        try {
            this.strongIdentificationRequiringCentralAuthenticationService.checkStrongIdentificationHook(this.credentials);
        } catch(AuthenticationException e) {
            throw new Exception("This shouldn't execute");
        }
    }




}
