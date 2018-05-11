package fi.vm.sade.login.failure;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import javax.servlet.annotation.WebFilter;

@WebFilter(urlPatterns = "/login")
public class RedirectHttpServletResponseWrapperFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        filterChain.doFilter(servletRequest, new RedirectHttpServletResponseWrapper((HttpServletResponse)servletResponse));
    }

    @Override
    public void destroy() {

    }
}
