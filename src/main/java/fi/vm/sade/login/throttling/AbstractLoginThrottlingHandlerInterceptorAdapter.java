package fi.vm.sade.login.throttling;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class AbstractLoginThrottlingHandlerInterceptorAdapter extends HandlerInterceptorAdapter {

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

        if (!isPostRequest(request)) {
            return;
        }

        if( hasSuccessfullAuthenticationEvent(request) ) {
            notifySuccessfullLogin(request);
        } else {
            notifyFailedLoginAttempt(request);
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
        return "success".equals(context.getCurrentEvent().getId());
    }

    public abstract boolean allowLoginAttempt(HttpServletRequest request);

    public abstract void notifySuccessfullLogin(HttpServletRequest request);

    public abstract void notifyFailedLoginAttempt(HttpServletRequest request);

}
