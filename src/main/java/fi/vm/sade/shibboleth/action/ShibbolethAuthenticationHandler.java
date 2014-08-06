package fi.vm.sade.shibboleth.action;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

public class ShibbolethAuthenticationHandler extends HttpServlet {

    private static final long serialVersionUID = -7820101462800860576L;

    private static final String SAML_ID_HEADER = "HTTP_REMOTE_USER";
    private static final String SAML_HETU_HEADER = "HTTP_HETU";

//    private String redirectUrl;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, RuntimeException {
        String identity = request.getHeader(SAML_ID_HEADER);
        if (StringUtils.isBlank(identity)) {
            identity = request.getHeader(SAML_HETU_HEADER);
        }
        if (StringUtils.isBlank(identity)) {
            throw new RuntimeException("Invalid message data");
        }

        response.sendRedirect(/*redirectUrl*/"https://testi.virkailija.opintopolku.fi/cas/login?service=https%3A%2F%2Ftesti.virkailija.opintopolku.fi%2Fregistration-ui%2Fhtml%2Findex.html%23%2Fregister" + "&authToken=" + identity);
    }
/*
    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
    */
}
