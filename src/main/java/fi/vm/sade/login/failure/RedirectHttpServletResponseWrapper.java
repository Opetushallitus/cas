package fi.vm.sade.login.failure;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

/**
 * This wrapper is needed, because javax.servlet.servlet-api version 2.5 is in use.
 * Once it's updated to 3.0, this wrapper is no longed needed and location
 * information can be fetched directly from HttpServletResponse itself.
 */
public class RedirectHttpServletResponseWrapper extends HttpServletResponseWrapper {

    private String location;

    public RedirectHttpServletResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        super.sendRedirect(location);
        this.location = location;
    }

    public String getRedirectLocation() {
        return this.location;
    }

    public boolean redirectSent() {
        return null != this.location;
    }

}
