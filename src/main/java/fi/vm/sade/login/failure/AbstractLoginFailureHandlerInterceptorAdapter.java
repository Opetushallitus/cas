package fi.vm.sade.login.failure;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;

public abstract class AbstractLoginFailureHandlerInterceptorAdapter extends HandlerInterceptorAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLoginFailureHandlerInterceptorAdapter.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if(!isPostRequest(request)) {
            return true;
        }

        int loginDelay = getMinutesToAllowLogin(request);

        if(0 != loginDelay ) {
            response.sendError(503);
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (!isPostRequest(request)) {
            return;
        }
        if( hasSuccessfullAuthenticationEvent(request) ) {
            notifySuccessfullLogin(request);
        } else {

            LOGGER.debug("service param {}", request.getParameter("service"));
            Enumeration e = request.getHeaderNames();
            while(e.hasMoreElements()){
                Object name  = e.nextElement();
                LOGGER.debug("Request header {}={}", name, request.getHeader((String)name));
            }
            LOGGER.debug("Requested URI {}", request.getRequestURI());
            LOGGER.debug("Response contains Location header {}", response.containsHeader("Location"));

            notifyFailedLoginAttempt(request);
        }

        LOGGER.debug("Handler {}", handler);
    }

    private boolean isPostRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod());
    }

    private boolean hasSuccessfullAuthenticationEvent(HttpServletRequest request) {
        /*LOGGER.debug("Request has following attributes:");
        Enumeration e = request.getAttributeNames();
        while(e.hasMoreElements()) {
            Object name = e.nextElement();
            LOGGER.debug("{}={}", name, request.getAttribute((String)name));
        }

        WebApplicationContext wacontext = (WebApplicationContext)request.getAttribute("org.springframework.web.servlet.DispatcherServlet.CONTEXT");

        if(null != wacontext.getServletContext()) {
            e = wacontext.getServletContext().getAttributeNames();

            LOGGER.debug("Servlet context has following attributes:");
            while(e.hasMoreElements()) {
                Object name = e.nextElement();
                LOGGER.debug("{}={}", name, request.getAttribute((String)name));
            }
        } else {
            LOGGER.debug("Servlet context is null.");
        }*/


        RequestContext context = (RequestContext) request.getAttribute("flowRequestContext");
        if( null == context || null == context.getCurrentEvent() ) {
            LOGGER.debug("Either context {} or current event {} is null!", context, null == context ? null : context.getCurrentEvent());


            return false;
        }

        LOGGER.debug("Current event ID is {}", context.getCurrentEvent().getId());
        return "success".equals(context.getCurrentEvent().getId());
    }

    public abstract int getMinutesToAllowLogin(HttpServletRequest request);

    public abstract void notifySuccessfullLogin(HttpServletRequest request);

    public abstract void notifyFailedLoginAttempt(HttpServletRequest request);

}
