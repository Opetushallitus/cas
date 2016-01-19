package fi.vm.sade.login.throttling;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.webflow.engine.RequestControlContext;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;

public abstract class AbstractLoginThrottlingHandlerInterceptorAdapter extends HandlerInterceptorAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLoginThrottlingHandlerInterceptorAdapter.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (/*isPostRequest(request) &&*/ !allowLoginAttempt(request)) {
            LOGGER.error("Not allowing login attempt");
            response.sendRedirect(request.getRequestURI() + "?tooManyLoginAttempts=true");
            //response.setIntHeader("Retry-After", 120);
            //response.sendError(503);
            return false;
        }
        LOGGER.error("Allowing login attempt.");
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        /*LOGGER.error("POST HANDLE");

        if (!isPostRequest(request)) {
            LOGGER.error("NOT POST REQUEST");
            return;
        }*/

        /*LOGGER.error("View name " + modelAndView.getViewName());
        LOGGER.error("model" + modelAndView.getModel());
        LOGGER.error("" + modelAndView.toString());*/

        if( hasSuccessfullAuthenticationEvent(request) ) {
            LOGGER.error("NOTIFY SUCCESSFULL AUTH");
            notifySuccessfullLogin(request);
        } else {
            LOGGER.error("NOTIFY FAILED ATTEMPT");
            notifyFailedLoginAttempt(request);
            LOGGER.error("Redirect {}", request.getRequestURL());
            response.sendRedirect("http://www.siili.fi");
            //response.sendRedirect(request.getRequestURL().toString() + "&tooManyLoginAttempts=true");
        }

    }

    private boolean isPostRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod());
    }

    private boolean hasSuccessfullAuthenticationEvent(HttpServletRequest request) {
        RequestContext context = (RequestContext) request.getAttribute("flowRequestContext");
        if( null == context || null == context.getCurrentEvent() ) {
            return false;
        }
        LOGGER.error("currentFormObject {}", context.getFlowScope().get("currentFormObject"));
        return "success".equals(context.getCurrentEvent().getId());
    }

    public abstract boolean allowLoginAttempt(HttpServletRequest request);

    public abstract void notifySuccessfullLogin(HttpServletRequest request);

    public abstract void notifyFailedLoginAttempt(HttpServletRequest request);

}
