package fi.vm.sade.auth.exception;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;

public class OphAuthenticationExceptionHandlerTest {

    private OphAuthenticationExceptionHandler handler;

    @Before
    public void setup() {
        handler = new OphAuthenticationExceptionHandler();
    }

    @Test
    public void getErrorsShouldIncludeNoStrongIdentificationException() {
        assertThat(handler.getErrors())
                .contains(NoStrongIdentificationException.class)
                .anyMatch(error -> !error.isAssignableFrom(NoStrongIdentificationException.class));
    }

}
