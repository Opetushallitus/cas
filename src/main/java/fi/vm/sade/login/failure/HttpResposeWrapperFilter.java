package fi.vm.sade.login.failure;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpResposeWrapperFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpResposeWrapperFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        LOGGER.debug("Filtering response...");
        filterChain.doFilter(servletRequest, new HttpResponseWrapper((HttpServletResponse)servletResponse));
    }

    @Override
    public void destroy() {

    }
}
