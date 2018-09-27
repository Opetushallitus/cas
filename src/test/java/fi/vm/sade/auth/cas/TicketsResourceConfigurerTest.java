package fi.vm.sade.auth.cas;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.registry.TicketRegistrySupport;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TicketsResourceConfigurerTest {

    private TicketsResourceConfigurer.TicketRegistrySupportWrapper wrapper;

    private TicketRegistrySupport ticketRegistrySupportMock;

    @Before
    public void setup() {
        ticketRegistrySupportMock = mock(TicketRegistrySupport.class);
        wrapper = new TicketsResourceConfigurer.TicketRegistrySupportWrapper(ticketRegistrySupportMock);
    }

    @Test
    public void wrapperGetAuthenticationFromThrowsInvalidTicketException() {
        when(ticketRegistrySupportMock.getAuthenticationFrom(any())).thenReturn(null);

        Throwable throwable = catchThrowable(() -> wrapper.getAuthenticationFrom("tgt123"));

        assertThat(throwable).isInstanceOf(InvalidTicketException.class);
    }

    @Test
    public void wrapperGetAuthenticationFromReturnsAuthentication() {
        Authentication authenticationMock = mock(Authentication.class);
        when(ticketRegistrySupportMock.getAuthenticationFrom(any())).thenReturn(authenticationMock);

        Authentication unknown = wrapper.getAuthenticationFrom("tgt123");

        assertThat(unknown).isSameAs(authenticationMock);
    }

}
