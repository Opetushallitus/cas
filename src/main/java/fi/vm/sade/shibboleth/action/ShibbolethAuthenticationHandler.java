package fi.vm.sade.shibboleth.action;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

public class ShibbolethAuthenticationHandler extends HttpServlet {

    private static final long serialVersionUID = -7820101462800860576L;

    private static final String SAML_ID_HEADER = "REMOTE_USER";
    private static final String SAML_HETU_HEADER = "HTTP_HETU";

    private String redirectUrl;
    
    public void init(ServletConfig config) throws ServletException {
        String host = null;
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(config.getInitParameter("propsLocation")));
            host = props.getProperty("host.virkailija");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        redirectUrl = "https://" + host + "/cas/login?service=https%3A%2F%2F" + host + "%2Fregistration-ui%2Fhtml%2Findex.html%23%2Fregister&authToken=";
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, RuntimeException {
        String identity = request.getHeader(SAML_ID_HEADER);
        if (StringUtils.isBlank(identity)) {
            identity = request.getHeader(SAML_HETU_HEADER);
        }
        if (StringUtils.isBlank(identity)) {
            throw new RuntimeException("Invalid message data");
        }

        response.sendRedirect(redirectUrl + identity);
    }
}
