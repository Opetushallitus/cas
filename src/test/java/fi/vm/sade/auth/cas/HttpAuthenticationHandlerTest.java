package fi.vm.sade.auth.cas;

import fi.vm.sade.CasOphProperties;
import fi.vm.sade.javautils.httpclient.*;
import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.saml.action.SAMLCredentials;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.AbstractWebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.services.ServicesManager;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.security.auth.login.FailedLoginException;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class HttpAuthenticationHandlerTest {
    private AuthenticationHandler authenticationHandler;

    private OphHttpResponse httpResponseMock;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() throws IOException {
        ServicesManager servicesManagerMock = mock(ServicesManager.class);
        OphHttpClientProxyRequest httpClientProxyRequestMock = mock(OphHttpClientProxyRequest.class);
        when(httpClientProxyRequestMock.execute(any())).thenAnswer(invocation
                -> ((OphHttpResponseHandler<Object>) invocation.getArguments()[0]).handleResponse(httpResponseMock));
        when(httpClientProxyRequestMock.handleManually()).thenReturn(httpResponseMock);
        OphHttpClientProxy httpClientProxyMock = mock(OphHttpClientProxy.class);
        when(httpClientProxyMock.createRequest(any())).thenReturn(httpClientProxyRequestMock);
        httpResponseMock = mock(OphHttpResponse.class);
        when(httpResponseMock.getStatusCode()).thenReturn(200);
        Environment environmentMock = mock(Environment.class);
        when(environmentMock.getRequiredProperty(any())).thenReturn("localhost");
        OphProperties properties = new CasOphProperties(environmentMock);
        OphHttpClient httpClient = new OphHttpClient(httpClientProxyMock, "cas", properties);

        authenticationHandler = new HttpAuthenticationHandler(servicesManagerMock, 0, httpClient);
    }

    @Test
    public void handlerShouldHaveName() {
        assertThat(authenticationHandler.getName()).isNotNull();
    }

    @Test
    public void handlerShouldSupportUsernamePasswordCredential() {
        assertThat(authenticationHandler.supports(new UsernamePasswordCredential("user1", "pass1"))).isTrue();
    }

    @Test
    public void handlerShouldNotSupportSamlCredential() {
        assertThat(authenticationHandler.supports(new SAMLCredentials("token1"))).isFalse();
    }

    @Test
    public void authenticate() throws Throwable {
        when(httpResponseMock.getStatusCode()).thenReturn(200);
        when(httpResponseMock.asText()).thenReturn("{\"username\":\"USER1\",\"kayttajaTyyppi\":\"VIRKAILIJA\"}");

        AuthenticationHandlerExecutionResult authenticate = authenticationHandler.authenticate(new UsernamePasswordCredential("user1", "pass1"), getService());

        assertThat(authenticate.getPrincipal().getId()).isEqualTo("USER1");
        List<Object> idpEntityIdList = authenticate.getPrincipal().getAttributes().get("idpEntityId");
        assertThat(idpEntityIdList).containsOnly("usernamePassword");
    }

    @Test
    public void defaultKayttajaTyyppiToVirkailijaIfMissing() throws Throwable {
        when(httpResponseMock.getStatusCode()).thenReturn(200);
        when(httpResponseMock.asText()).thenReturn("{\"username\":\"USER1\"}");

        AuthenticationHandlerExecutionResult authenticate = authenticationHandler.authenticate(new UsernamePasswordCredential("user1", "pass1"), getService());

        assertThat(authenticate.getPrincipal().getId()).isEqualTo("USER1");
        List<Object> idpEntityIdList = authenticate.getPrincipal().getAttributes().get("idpEntityId");
        assertThat(authenticate.getPrincipal().getAttributes().get("kayttajaTyyppi").get(0)).isEqualTo("VIRKAILIJA");
        assertThat(idpEntityIdList).containsOnly("usernamePassword");
    }

    @Test
    public void authenticateShouldThrowFailedLoginException() {
        when(httpResponseMock.getStatusCode()).thenReturn(401);

        Throwable throwable = catchThrowable(() -> authenticationHandler.authenticate(new UsernamePasswordCredential("user1", "pass1"), getService()));

        assertThat(throwable).isInstanceOf(FailedLoginException.class);
    }

    @Test
    public void authenticateShouldThrowPreventedException() {
        when(httpResponseMock.getStatusCode()).thenReturn(500);

        Throwable throwable = catchThrowable(() -> authenticationHandler.authenticate(new UsernamePasswordCredential("user1", "pass1"), getService()));

        assertThat(throwable).isInstanceOf(PreventedException.class);
    }

    private static AbstractWebApplicationService getService() {
        String serviceName = "http://example.com";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, serviceName);
        return (AbstractWebApplicationService) new WebApplicationServiceFactory().createService(request);
    }
}
