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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.vm.sade.generic.rest.CachingRestClient;

public class ShibbolethAuthenticationHandler extends HttpServlet {

    private static final long serialVersionUID = -7820101462800860576L;

    private static final String SAML_ID_HEADER = "REMOTE_USER";
    private static final String SAML_HETU_HEADER = "HETU";

    private String successRedirectUrl;
    private String failureRedirectUrl;
    private String authenticationServiceRestUrl;
    private CachingRestClient restClient = new CachingRestClient();
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    public void init(ServletConfig config) throws ServletException {
        String host = null;
        String hostOppija = null;
        String vetumaErrorPage = null;
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(config.getInitParameter("propsLocation")));
            host = props.getProperty("host.virkailija");
            hostOppija = props.getProperty("host.oppija");
            vetumaErrorPage = props.getProperty("vetuma.error.page");
            authenticationServiceRestUrl = "https://" + host + "/authentication-service/resources/cas/hetu/";
        }
        catch (Exception e) {
            logger.error("Error instantiating ShibbolethAuthenticationHandler", e);
        }
        successRedirectUrl = "https://" + host + "/cas/login?service=https%3A%2F%2F" + host + "%2Fregistration-ui%2Fhtml%2Findex.html%23%2Fregister&authToken=";
        failureRedirectUrl = "https://" + hostOppija + vetumaErrorPage;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, RuntimeException {
        String redirectUrl = failureRedirectUrl;
        String identity = request.getHeader(SAML_ID_HEADER);
        if (StringUtils.isBlank(identity)) {
            identity = request.getHeader(SAML_HETU_HEADER);
        }
        if (StringUtils.isNotBlank(identity)) {
            try {
                String authToken = restClient.get(authenticationServiceRestUrl + identity, String.class);
                if (authToken != null) {
                    redirectUrl = successRedirectUrl + authToken;
                }
            }
            catch (Exception e) {
                logger.error("Internal error encountered", e);
            }
        }

        response.sendRedirect(redirectUrl);
    }
}
