package fi.vm.sade.login.failure;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        if( hasSuccessfulAuthentication(request, response) ) {
            notifySuccessfullLogin(request);
        } else {
            notifyFailedLoginAttempt(request);
        }
    }

    private boolean isPostRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod());
    }

    private boolean hasSuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response) {
        Event currentEvent = getCurrentRequestFlowEvent(request);
        if(null == currentEvent) {
            LOGGER.debug("There is no current flow event in request. Checking redirect...");
            return hasRedirectToService(request, response);
        } else {
            LOGGER.debug("Current event ID is {}", currentEvent.getId());
            return "success".equals(currentEvent.getId());
        }

    }

    private boolean hasRedirectToService(HttpServletRequest request, HttpServletResponse response) {

        String serviceUri = request.getParameter("service");
        if(null == serviceUri || "".equals(serviceUri)) {
            LOGGER.debug("There is no service parameter in HttpServletRequest.");
            return false;
        }

        if(!(response instanceof RedirectHttpServletResponseWrapper)) {
            LOGGER.warn("HttpServletResponse {} is not instance of RedirectHttpServletResponseWrapper. " +
              "Filter configuration might be missing or wrong!", response.getClass().getName());
            return false;
        }

        RedirectHttpServletResponseWrapper responseWrapper = (RedirectHttpServletResponseWrapper)response;
        if( !responseWrapper.redirectSent() ) {
            return false;
        }

        LOGGER.debug("There is redirect to {} and service parameter is {}.", responseWrapper.getRedirectLocation(), serviceUri);
        return responseWrapper.getRedirectLocation().startsWith(serviceUri);
    }

    private Event getCurrentRequestFlowEvent(HttpServletRequest request) {
        RequestContext context = (RequestContext) request.getAttribute("flowRequestContext");
        return null == context ? null : context.getCurrentEvent();
    }

    public abstract int getMinutesToAllowLogin(HttpServletRequest request);

    public abstract void notifySuccessfullLogin(HttpServletRequest request);

    public abstract void notifyFailedLoginAttempt(HttpServletRequest request);

}
