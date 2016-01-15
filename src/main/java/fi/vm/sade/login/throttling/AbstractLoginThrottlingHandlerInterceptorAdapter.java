package fi.vm.sade.login.throttling;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
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

        if (isPostRequest(request) && !allowLoginAttempt(request)) {
            response.sendError(403);
            return false;
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        LOGGER.error("POST HANDLE");

        for( Enumeration<String> attributeNames = request.getAttributeNames(); ; attributeNames.hasMoreElements() ) {
            String attributeName = attributeNames.nextElement();
            LOGGER.error("Request attribute {}={}", attributeName, request.getAttribute(attributeName));
        }

        /*if (!isPostRequest(request)) {
            LOGGER.error("NOT POST REQUEST");
            return;
        }

        if( hasSuccessfullAuthenticationEvent(request) ) {
            LOGGER.error("NOTIFY SUCCESSFULL AUTH");
            notifySuccessfullLogin(request);
        } else {
            LOGGER.error("NOTIFY FAILED ATTEMPT");
            notifyFailedLoginAttempt(request);
        }*/

    }

    private boolean isPostRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod());
    }

    private boolean hasSuccessfullAuthenticationEvent(HttpServletRequest request) {
        RequestContext context = (RequestContext) request.getAttribute("flowRequestContext");
        if( null == context || null == context.getCurrentEvent() ) {
            return false;
        }
        LOGGER.error("Request context {}", context);
        return "success".equals(context.getCurrentEvent().getId());
    }

    public abstract boolean allowLoginAttempt(HttpServletRequest request);

    public abstract void notifySuccessfullLogin(HttpServletRequest request);

    public abstract void notifyFailedLoginAttempt(HttpServletRequest request);

}
