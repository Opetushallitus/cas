package fi.vm.sade.auth.interrupt;

import fi.vm.sade.CasOphProperties;
import fi.vm.sade.auth.action.EmailVerificationRedirectAction;
import fi.vm.sade.auth.action.StrongIdentificationRedirectAction;
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
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class LoginRedirectInterruptInquirerTest {

    private LoginRedirectInterruptInquirer inquirer;

    private final PrincipalFactory principalFactory = PrincipalFactoryUtils.newPrincipalFactory();
    private KayttooikeusRestClient kayttooikeusRestClientMock;
    private OppijanumerorekisteriRestClient oppijanumerorekisteriRestClientMock;
    private StrongIdentificationRedirectAction strongIdentificationRedirectAction;
    private EmailVerificationRedirectAction emailVerificationRedirectAction;

    @Before
    public void setup() {
        kayttooikeusRestClientMock = mock(KayttooikeusRestClient.class);
        when(kayttooikeusRestClientMock.createLoginToken(any())).thenReturn("loginToken1");
        oppijanumerorekisteriRestClientMock = mock(OppijanumerorekisteriRestClient.class);
        when(oppijanumerorekisteriRestClientMock.getAsiointikieli(any())).thenReturn("fi");
        Environment environmentMock = mock(Environment.class);
        when(environmentMock.getRequiredProperty("host.cas")).thenReturn("localhost:8081");
        when(environmentMock.getRequiredProperty("host.virkailija")).thenReturn("localhost:8082");
        when(environmentMock.getRequiredProperty("host.alb")).thenReturn("localhost:8083");
        CasOphProperties properties = new CasOphProperties(environmentMock);
        strongIdentificationRedirectAction = new StrongIdentificationRedirectAction(kayttooikeusRestClientMock, oppijanumerorekisteriRestClientMock, properties);
        emailVerificationRedirectAction = new EmailVerificationRedirectAction(kayttooikeusRestClientMock, oppijanumerorekisteriRestClientMock, properties);
        inquirer = new LoginRedirectInterruptInquirer(kayttooikeusRestClientMock, strongIdentificationRedirectAction, emailVerificationRedirectAction);
    }

    @Test
    public void onRedirectCodeNullShouldNotRedirect() {
        when(kayttooikeusRestClientMock.getRedirectCodeByUsername(any())).thenReturn(Optional.empty());
        Authentication authentication = new DefaultAuthentication(ZonedDateTime.now(), principalFactory.createPrincipal("user1"), emptyMap(), emptyMap());
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
        Authentication authentication = new DefaultAuthentication(ZonedDateTime.now(), principalFactory.createPrincipal("user1"), emptyMap(), emptyMap());
        Credential credential = new UsernamePasswordCredential("user1", "pass1");
        inquirer.setRequireStrongIdentification(true);

        InterruptResponse response = inquirer.inquire(authentication, null, null, credential, null);

        assertThat(response)
                .returns(true, InterruptResponse::isInterrupt)
                .returns("https://localhost:8082/henkilo-ui/vahvatunnistusinfo/fi/loginToken1", t -> t.getLinks().values().stream().collect(joining(",")));
        verify(kayttooikeusRestClientMock).getRedirectCodeByUsername("user1");
    }

    @Test
    public void onUsernameInCasRequireStrongidentificationListShouldRedirectToStrongAuthentication() {
        when(kayttooikeusRestClientMock.getRedirectCodeByUsername(any())).thenReturn(Optional.of("STRONG_IDENTIFICATION"));
        Authentication authentication = new DefaultAuthentication(ZonedDateTime.now(), principalFactory.createPrincipal("user1"), emptyMap(), emptyMap());
        Credential credential = new UsernamePasswordCredential("user1", "pass1");
        inquirer.setRequireStrongIdentificationUsernameList(asList("user1", "user2"));

        InterruptResponse response = inquirer.inquire(authentication, null, null, credential, null);

        assertThat(response)
                .returns(true, InterruptResponse::isInterrupt)
                .returns("https://localhost:8082/henkilo-ui/vahvatunnistusinfo/fi/loginToken1", t -> t.getLinks().values().stream().collect(joining(",")));
        verify(kayttooikeusRestClientMock).getRedirectCodeByUsername("user1");
    }

    @Test
    public void onEmailVerificationEnabledAndEmailVerificationRedirectCodeShouldRedirectToEmailVerification() {
        when(kayttooikeusRestClientMock.getRedirectCodeByUsername(any())).thenReturn(Optional.of("EMAIL_VERIFICATION"));
        Authentication authentication = new DefaultAuthentication(ZonedDateTime.now(), principalFactory.createPrincipal("user1"), emptyMap(), emptyMap());
        Credential credential = new UsernamePasswordCredential("user1", "pass1");
        inquirer.setEmailVerificationEnabled(true);

        InterruptResponse response = inquirer.inquire(authentication, null, null, credential, null);

        assertThat(response)
                .returns(true, InterruptResponse::isInterrupt)
                .returns("https://localhost:8082/henkilo-ui/sahkopostivarmistus/fi/loginToken1", t -> t.getLinks().values().stream().collect(joining(",")));
        verify(kayttooikeusRestClientMock).getRedirectCodeByUsername("user1");
    }

    @Test
    public void onEmailVerificationDisabledAndUsernameInEmailVerificationListShouldRedirectToEmailVerification() {
        when(kayttooikeusRestClientMock.getRedirectCodeByUsername(any())).thenReturn(Optional.of("EMAIL_VERIFICATION"));
        Authentication authentication = new DefaultAuthentication(ZonedDateTime.now(), principalFactory.createPrincipal("user1"), emptyMap(), emptyMap());
        Credential credential = new UsernamePasswordCredential("user1", "pass1");
        inquirer.setEmailVerificationUsernameList(asList("user1", "user2"));

        InterruptResponse response = inquirer.inquire(authentication, null, null, credential, null);

        assertThat(response)
                .returns(true, InterruptResponse::isInterrupt)
                .returns("https://localhost:8082/henkilo-ui/sahkopostivarmistus/fi/loginToken1", t -> t.getLinks().values().stream().collect(joining(",")));
        verify(kayttooikeusRestClientMock).getRedirectCodeByUsername("user1");
    }

    @Test
    public void onStrongIdentificationAndEmailVerificationDisabledShouldNotRedirectWithStrongIdentification() {
        when(kayttooikeusRestClientMock.getRedirectCodeByUsername(any())).thenReturn(Optional.of("STRONG_IDENTIFICATION"));
        Authentication authentication = new DefaultAuthentication(ZonedDateTime.now(), principalFactory.createPrincipal("user1"), emptyMap(), emptyMap());
        Credential credential = new UsernamePasswordCredential("user1", "pass1");

        InterruptResponse response = inquirer.inquire(authentication, null, null, credential, null);

        assertThat(response).returns(false, InterruptResponse::isInterrupt);
        verify(kayttooikeusRestClientMock).getRedirectCodeByUsername("user1");
        verifyNoMoreInteractions(kayttooikeusRestClientMock);
    }

    @Test
    public void onStrongIdentificationAndEmailVerificationDisabledShouldNotRedirectWithEmailVerification() {
        when(kayttooikeusRestClientMock.getRedirectCodeByUsername(any())).thenReturn(Optional.of("EMAIL_VERIFICATION"));
        Authentication authentication = new DefaultAuthentication(ZonedDateTime.now(), principalFactory.createPrincipal("user1"), emptyMap(), emptyMap());
        Credential credential = new UsernamePasswordCredential("user1", "pass1");

        InterruptResponse response = inquirer.inquire(authentication, null, null, credential, null);

        assertThat(response).returns(false, InterruptResponse::isInterrupt);
        verify(kayttooikeusRestClientMock).getRedirectCodeByUsername("user1");
        verifyNoMoreInteractions(kayttooikeusRestClientMock);
    }

}
