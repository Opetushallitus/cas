package fi.vm.sade.login.throttling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class AbstractLoginFailureFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLoginFailureFilter.class);


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;

        if(!isPostRequest(request)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        int loginDelay = getMinutesToAllowLogin(request);

        if(0 != loginDelay ) {
            LOGGER.error("Not allowing login attempt in {} seconds.", loginDelay);
            response.sendError(503);
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);

        notifyLoginAttempt(request, response);

    }

    private void notifyLoginAttempt(HttpServletRequest request, HttpServletResponse response) {
        if( isSuccessLogin(request, response) ) {
            notifySuccessfullLogin(request);
        } else {
            notifyFailedLoginAttempt(request);
        }
    }

    private boolean isPostRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod());
    }

    public abstract int getMinutesToAllowLogin(HttpServletRequest request);

    public abstract void notifySuccessfullLogin(HttpServletRequest request);

    public abstract void notifyFailedLoginAttempt(HttpServletRequest request);

    public abstract boolean isSuccessLogin(HttpServletRequest request, HttpServletResponse response);
}
