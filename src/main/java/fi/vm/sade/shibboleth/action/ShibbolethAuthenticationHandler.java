package fi.vm.sade.shibboleth.action;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.vm.sade.generic.rest.CachingRestClient;
import fi.vm.sade.properties.OphProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

public class ShibbolethAuthenticationHandler extends HttpServlet {

    private static final long serialVersionUID = -7820101462800860576L;

    private static final String SAML_ID_HEADER = "REMOTE_USER";
    private static final String SAML_HETU_HEADER = "HETU";

    @Autowired
    private OphProperties ophProperties;

    private CachingRestClient restClient = new CachingRestClient().setClientSubSystemCode("authentication.cas");
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, RuntimeException {
        String redirectUrl = ophProperties.url("omattiedot.vetuma.error");
        String identity = request.getHeader(SAML_ID_HEADER);
        if (StringUtils.isBlank(identity)) {
            identity = request.getHeader(SAML_HETU_HEADER);
        }
        if (StringUtils.isNotBlank(identity)) {
            try {
                String authToken = restClient.get(ophProperties.url("henkilo.cas.hetu", identity), String.class);
                if (authToken != null) {
                    String registerUiUrl = ophProperties.url("henkilo.register-ui");
                    redirectUrl = ophProperties.url("cas.login", registerUiUrl, authToken);
                }
            }
            catch (Exception e) {
                logger.error("Internal error encountered", e);
            }
        }

        response.sendRedirect(redirectUrl);
    }
}
