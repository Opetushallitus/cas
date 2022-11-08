package fi.vm.sade.servlet;

import com.google.common.collect.Sets;
import fi.vm.sade.RemoteAddressFilter;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import java.io.IOException;
import java.util.Set;

@Component
public class StuckServiceTicketRetrievalDebuggingFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(StuckServiceTicketRetrievalDebuggingFilter.class);
    private static final String ST_RETRIEVAL_URL_MATCHER = "cas/v1/tickets/TGT";

    private Set<String> ipsToInspect;
    private StuckServiceTicketRetrievalThreadDumper dumper;

    @Override
    public void init(FilterConfig filterConfig) {
       SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
   }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest) {
            String remoteAddress = RemoteAddressFilter.getRemoteAddress(httpRequest);
            if (ipsToInspect.contains(remoteAddress) && StringUtils.contains(httpRequest.getRequestURL(), ST_RETRIEVAL_URL_MATCHER)) {
                log.info("Got request '" + httpRequest.getRequestURL() + "' from IP address " + remoteAddress + " marked for inspection.");
                dumper.triggerRunsToBackground();
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    @Autowired @Value("${stuck.service.ticket.retrieval.ips.to.inspect}")
    public void setIpsToInspect(String ipsToInspectInSingleString) {
        log.info(String.format("Initialising with IPs to inspect '%s'", ipsToInspectInSingleString));
        ipsToInspect = Sets.newHashSet(ipsToInspectInSingleString.split(","));
        log.info(String.format("Parsed IPs to inspect to be '%s'", ipsToInspect));
    }

    @Autowired
    public void setDumper(StuckServiceTicketRetrievalThreadDumper dumper) {
        this.dumper = dumper;
    }
}
