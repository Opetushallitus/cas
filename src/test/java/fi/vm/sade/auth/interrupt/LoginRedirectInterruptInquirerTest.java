package fi.vm.sade.auth.interrupt;

import fi.vm.sade.CasOphProperties;
import fi.vm.sade.auth.action.LoginRedirectAction;
import fi.vm.sade.auth.clients.KayttooikeusRestClient;
import fi.vm.sade.auth.clients.OppijanumerorekisteriRestClient;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthentication;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.interrupt.InterruptResponse;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.env.Environment;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class LoginRedirectInterruptInquirerTest {

    private LoginRedirectInterruptInquirer inquirer;

    private final PrincipalFactory principalFactory = PrincipalFactoryUtils.newPrincipalFactory();
    private KayttooikeusRestClient kayttooikeusRestClientMock;

    @Before
    public void setup() {
        kayttooikeusRestClientMock = mock(KayttooikeusRestClient.class);
        when(kayttooikeusRestClientMock.createLoginToken(any())).thenReturn("loginToken1");
        OppijanumerorekisteriRestClient oppijanumerorekisteriRestClientMock =
                mock(OppijanumerorekisteriRestClient.class);
        when(oppijanumerorekisteriRestClientMock.getAsiointikieli(any())).thenReturn("fi");
        Environment environmentMock = mock(Environment.class);
        when(environmentMock.getRequiredProperty("host.cas")).thenReturn("localhost:8081");
        when(environmentMock.getRequiredProperty("host.virkailija")).thenReturn("localhost:8082");
        when(environmentMock.getRequiredProperty("host.alb")).thenReturn("localhost:8083");
        CasOphProperties properties = new CasOphProperties(environmentMock);
        LoginRedirectAction loginRedirectAction = new LoginRedirectAction(kayttooikeusRestClientMock, oppijanumerorekisteriRestClientMock, properties);
        inquirer = new LoginRedirectInterruptInquirer(kayttooikeusRestClientMock, loginRedirectAction);
    }

    @Test
    public void onRedirectCodeNullShouldNotRedirect() {
        when(kayttooikeusRestClientMock.getRedirectCodeByUsername(any())).thenReturn(Optional.empty());
        Authentication authentication = new DefaultAuthentication(ZonedDateTime.now(), principalFactory.createPrincipal("user1"), emptyList(), emptyList(),emptyMap(),emptyMap(),emptyMap());
        Credential credential = new UsernamePasswordCredential("user1", "pass1");
        inquirer.setRequireStrongIdentification(true);
        inquirer.setEmailVerificationEnabled(true);

        InterruptResponse response = inquirer.inquire(authentication, null, null, credential, null);

        assertThat(response).returns(false, InterruptResponse::isInterrupt);
        verify(kayttooikeusRestClientMock).getRedirectCodeByUsername("user1");
        verifyNoMoreInteractions(kayttooikeusRestClientMock);
    }

    @Test
    public void onRequireStrongIdentificationAndStrongIdentificationRedirectCodeShouldRedirectToStrongIdentification() {
        when(kayttooikeusRestClientMock.getRedirectCodeByUsername(any())).thenReturn(Optional.of("STRONG_IDENTIFICATION"));
        Authentication authentication = new DefaultAuthentication(ZonedDateTime.now(), principalFactory.createPrincipal("user1"), emptyList(), emptyList(),emptyMap(),emptyMap(),emptyMap());
        Credential credential = new UsernamePasswordCredential("user1", "pass1");
        inquirer.setRequireStrongIdentification(true);

        InterruptResponse response = inquirer.inquire(authentication, null, null, credential, null);

        assertThat(response)
                .returns(true, InterruptResponse::isInterrupt)
                .returns("https://localhost:8082/henkilo-ui/kayttaja/vahvatunnistusinfo/fi/loginToken1", t -> String.join(",",
                        t.getLinks().values()));
        verify(kayttooikeusRestClientMock).getRedirectCodeByUsername("user1");
    }

    @Test
    public void onUsernameInCasRequireStrongidentificationListShouldRedirectToStrongAuthentication() {
        when(kayttooikeusRestClientMock.getRedirectCodeByUsername(any())).thenReturn(Optional.of("STRONG_IDENTIFICATION"));
        Authentication authentication = new DefaultAuthentication(ZonedDateTime.now(), principalFactory.createPrincipal("user1"), emptyList(), emptyList(),emptyMap(),emptyMap(),emptyMap());
        Credential credential = new UsernamePasswordCredential("user1", "pass1");
        inquirer.setRequireStrongIdentificationUsernameList(asList("user1", "user2"));

        InterruptResponse response = inquirer.inquire(authentication, null, null, credential, null);

        assertThat(response)
                .returns(true, InterruptResponse::isInterrupt)
                .returns("https://localhost:8082/henkilo-ui/kayttaja/vahvatunnistusinfo/fi/loginToken1", t -> String.join(",",
                        t.getLinks().values()));
        verify(kayttooikeusRestClientMock).getRedirectCodeByUsername("user1");
    }

    @Test
    public void onEmailVerificationEnabledAndEmailVerificationRedirectCodeShouldRedirectToEmailVerification() {
        when(kayttooikeusRestClientMock.getRedirectCodeByUsername(any())).thenReturn(Optional.of("EMAIL_VERIFICATION"));
        Authentication authentication = new DefaultAuthentication(ZonedDateTime.now(), principalFactory.createPrincipal("user1"), emptyList(), emptyList(),emptyMap(),emptyMap(),emptyMap());
        Credential credential = new UsernamePasswordCredential("user1", "pass1");
        inquirer.setEmailVerificationEnabled(true);

        InterruptResponse response = inquirer.inquire(authentication, null, null, credential, null);

        assertThat(response)
                .returns(true, InterruptResponse::isInterrupt)
                .returns("https://localhost:8082/henkilo-ui/kayttaja/sahkopostivarmistus/fi/loginToken1", t -> String.join(","
                        , t.getLinks().values()));
        verify(kayttooikeusRestClientMock).getRedirectCodeByUsername("user1");
    }

    @Test
    public void onEmailVerificationDisabledAndUsernameInEmailVerificationListShouldRedirectToEmailVerification() {
        when(kayttooikeusRestClientMock.getRedirectCodeByUsername(any())).thenReturn(Optional.of("EMAIL_VERIFICATION"));
        Authentication authentication = new DefaultAuthentication(ZonedDateTime.now(), principalFactory.createPrincipal("user1"), emptyList(), emptyList(),emptyMap(),emptyMap(),emptyMap());
        Credential credential = new UsernamePasswordCredential("user1", "pass1");
        inquirer.setEmailVerificationUsernameList(asList("user1", "user2"));

        InterruptResponse response = inquirer.inquire(authentication, null, null, credential, null);

        assertThat(response)
                .returns(true, InterruptResponse::isInterrupt)
                .returns("https://localhost:8082/henkilo-ui/kayttaja/sahkopostivarmistus/fi/loginToken1", t -> String.join(","
                        , t.getLinks().values()));
        verify(kayttooikeusRestClientMock).getRedirectCodeByUsername("user1");
    }

    @Test
    public void onStrongIdentificationAndEmailVerificationDisabledShouldNotRedirectWithStrongIdentification() {
        when(kayttooikeusRestClientMock.getRedirectCodeByUsername(any())).thenReturn(Optional.of("STRONG_IDENTIFICATION"));
        Authentication authentication = new DefaultAuthentication(ZonedDateTime.now(), principalFactory.createPrincipal("user1"), emptyList(), emptyList(),emptyMap(),emptyMap(),emptyMap());
        Credential credential = new UsernamePasswordCredential("user1", "pass1");

        InterruptResponse response = inquirer.inquire(authentication, null, null, credential, null);

        assertThat(response).returns(false, InterruptResponse::isInterrupt);
        verify(kayttooikeusRestClientMock).getRedirectCodeByUsername("user1");
        verifyNoMoreInteractions(kayttooikeusRestClientMock);
    }

    @Test
    public void onStrongIdentificationAndEmailVerificationDisabledShouldNotRedirectWithEmailVerification() {
        when(kayttooikeusRestClientMock.getRedirectCodeByUsername(any())).thenReturn(Optional.of("EMAIL_VERIFICATION"));
        Authentication authentication = new DefaultAuthentication(ZonedDateTime.now(), principalFactory.createPrincipal("user1"), emptyList(), emptyList(),emptyMap(),emptyMap(),emptyMap());
        Credential credential = new UsernamePasswordCredential("user1", "pass1");

        InterruptResponse response = inquirer.inquire(authentication, null, null, credential, null);

        assertThat(response).returns(false, InterruptResponse::isInterrupt);
        verify(kayttooikeusRestClientMock).getRedirectCodeByUsername("user1");
        verifyNoMoreInteractions(kayttooikeusRestClientMock);
    }

    @Test
    public void onPasswordChangeInterrupt() {
        when(kayttooikeusRestClientMock.getRedirectCodeByUsername(any())).thenReturn(Optional.of("PASSWORD_CHANGE"));
        Authentication authentication = new DefaultAuthentication(ZonedDateTime.now(), principalFactory.createPrincipal("user1"), emptyList(), emptyList(), emptyMap(), emptyMap(), emptyMap());
        Credential credential = new UsernamePasswordCredential("user1", "pass1");
        InterruptResponse response = inquirer.inquire(authentication, null, null, credential, null);

        assertThat(response)
                .returns(true, InterruptResponse::isInterrupt)
                .returns("https://localhost:8082/henkilo-ui/kayttaja/salasananvaihto/fi/loginToken1", t -> String.join(",", t.getLinks().values()));
        verify(kayttooikeusRestClientMock).getRedirectCodeByUsername("user1");
    }

    @Test
    public void onPasswordChangeInterruptWithSuomiFi() {
        Map<String, List<Object>> suomiFiIdpAttributes = Map.of("idpEntityId", List.of("vetuma"));
        when(kayttooikeusRestClientMock.getRedirectCodeByUsername(any())).thenReturn(Optional.of("PASSWORD_CHANGE"));
        Authentication authentication = new DefaultAuthentication(ZonedDateTime.now(), principalFactory.createPrincipal("user1", suomiFiIdpAttributes), emptyList(), emptyList(), emptyMap(), emptyMap(), emptyMap());
        Credential credential = new UsernamePasswordCredential("user1", "pass1");
        InterruptResponse response = inquirer.inquire(authentication, null, null, credential, null);

        assertThat(response).returns(false, InterruptResponse::isInterrupt);
        verify(kayttooikeusRestClientMock).getRedirectCodeByUsername("user1");
        verifyNoMoreInteractions(kayttooikeusRestClientMock);
    }

}
