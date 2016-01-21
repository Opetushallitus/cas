package fi.vm.sade.login.throttling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.*;
import java.io.IOException;

public class LoginThrottlingFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginThrottlingFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        LOGGER.error("Filter called");
        filterChain.doFilter(servletRequest, servletResponse);
        LOGGER.error("Filter after chain execution");
        RequestContext context = (RequestContext) servletRequest.getAttribute("flowRequestContext");
        if( null == context || null == context.getCurrentEvent() ) {
            LOGGER.error("Current event null");
            return;
        }
        LOGGER.error("Current event {}", context.getCurrentEvent().getId());
    }

    @Override
    public void destroy() {
    }
}
