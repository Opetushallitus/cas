package fi.vm.sade.login.failure;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

public class HttpResponseWrapper extends HttpServletResponseWrapper {

    private String location;
    private int status = -1;

    public HttpResponseWrapper(HttpServletResponse response) {
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

    @Override
    public void setStatus(int sc) {
        super.setStatus(sc);
        this.status = sc;
    }

    @Override
    public void setStatus(int sc, String sm) {
        super.setStatus(sc, sm);
        this.status = sc;
    }

    public boolean isStatusSet() {
        return -1 != this.status;
    }

    public int getStatus() {
        return status;
    }
}
