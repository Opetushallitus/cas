package fi.vm.sade.auth.cas;

import fi.vm.sade.CasOphProperties;
import fi.vm.sade.javautils.httpclient.*;
import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.saml.action.SAMLCredentials;
import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.junit.Before;
import org.junit.Test;

import javax.security.auth.login.FailedLoginException;
import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HttpAuthenticationHandlerTest {

    private AuthenticationHandler authenticationHandler;

    private OphHttpClientProxy httpClientProxyMock;
    private OphHttpResponse httpResponseMock;

    @Before
    public void setup() throws IOException {
        OphHttpClientProxyRequest httpClientProxyRequestMock = mock(OphHttpClientProxyRequest.class);
        when(httpClientProxyRequestMock.execute(any())).thenAnswer(invocation
                -> ((OphHttpResponseHandler<Object>) invocation.getArguments()[0]).handleResponse(httpResponseMock));
        when(httpClientProxyRequestMock.handleManually()).thenReturn(httpResponseMock);
        httpClientProxyMock = mock(OphHttpClientProxy.class);
        when(httpClientProxyMock.createRequest(any())).thenReturn(httpClientProxyRequestMock);
        httpResponseMock = mock(OphHttpResponse.class);
        when(httpResponseMock.getStatusCode()).thenReturn(200);
        OphProperties properties = new CasOphProperties();
        properties.addOverride("host.alb", "localhost");
        OphHttpClient httpClient = new OphHttpClient(httpClientProxyMock, "cas", properties);

        authenticationHandler = new HttpAuthenticationHandler(httpClient);
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
    public void authenticate() throws GeneralSecurityException, PreventedException {
        when(httpResponseMock.getStatusCode()).thenReturn(200);
        when(httpResponseMock.asText()).thenReturn("{\"username\":\"USER1\"}");

        HandlerResult authenticate = authenticationHandler.authenticate(new UsernamePasswordCredential("user1", "pass1"));

        assertThat(authenticate.getPrincipal().getId()).isEqualTo("USER1");
    }

    @Test
    public void authenticateShouldThrowFailedLoginException() {
        when(httpResponseMock.getStatusCode()).thenReturn(401);

        Throwable throwable = catchThrowable(() -> authenticationHandler.authenticate(new UsernamePasswordCredential("user1", "pass1")));

        assertThat(throwable).isInstanceOf(FailedLoginException.class);
    }

    @Test
    public void authenticateShouldThrowPreventedException() {
        when(httpResponseMock.getStatusCode()).thenReturn(500);

        Throwable throwable = catchThrowable(() -> authenticationHandler.authenticate(new UsernamePasswordCredential("user1", "pass1")));

        assertThat(throwable).isInstanceOf(PreventedException.class);
    }

}
