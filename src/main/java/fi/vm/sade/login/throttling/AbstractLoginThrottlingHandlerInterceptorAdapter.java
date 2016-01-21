package fi.vm.sade.login.throttling;

import org.jasig.cas.web.support.WebUtils;
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

        if(!isPostRequest(request)) {
            return true;
        }

        int loginDelay = getSecondsToAllowLogin(request);

        if(0 != loginDelay /*&& null == request.getParameter("waitBeforeNextLogin")*/ ) {
            LOGGER.error("Not allowing login attempt.");

            response.setIntHeader("LoginDelay", loginDelay);
            response.sendError(503);
            //String service = request.getParameter("service");
            //response.sendRedirect(request.getRequestURI() + "?service=" + service + "&waitBeforeNextLogin=" + loginDelay);

            return false;
        }



        /*long allowLoginAttemptInMs = allowLoginAttempt(request);

        if(allowLoginAttemptInMs != 0 && null == request.getParameter("tooManyLoginAttempts")) {
            LOGGER.error("Not allowing login attempt; too many attempts");

            String service = request.getParameter("service");

            response.sendRedirect(request.getRequestURI() + "?tooManyLoginAttempts=" + allowLoginAttemptInMs + "&service=" + service);
            return false;
        }*/

        /*if (isPostRequest(request) && !allowLoginAttempt(request) && null == request.getParameter("tooManyLoginAttempts")) {
            LOGGER.error("Not allowing login attempt");
            response.sendRedirect(request.getRequestURI() + "?tooManyLoginAttempts=true");
            //response.setIntHeader("Retry-After", 120);
            //response.sendError(503);
            return false;
        }*/
        LOGGER.error("Allowing login attempt.");
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        /*LOGGER.error("POST HANDLE");*/

        if (!isPostRequest(request)) {
            LOGGER.error("NOT POST REQUEST");
            return;
        }

        /*LOGGER.error("View name " + modelAndView.getViewName());
        LOGGER.error("model" + modelAndView.getModel());
        LOGGER.error("" + modelAndView.toString());*/

        if( hasSuccessfullAuthenticationEvent(request) ) {
            LOGGER.error("NOTIFY SUCCESSFULL AUTH");
            notifySuccessfullLogin(request);
        } else {
            LOGGER.error("NOTIFY FAILED ATTEMPT");
            notifyFailedLoginAttempt(request);
            //LOGGER.error("SETTING ATTRIBUTE");
            //request.setAttribute("loginWaitTime", getSecondsToAllowLogin(request));
            //LOGGER.error("ATTRIBUTE SET");
            /*Enumeration names = request.getAttributeNames();
            while(names.hasMoreElements()) {
                String attribute = (String)names.nextElement();
                LOGGER.error("{}={}", attribute, request.getAttribute(attribute));
            }*/
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

    public abstract int getSecondsToAllowLogin(HttpServletRequest request);

    public abstract void notifySuccessfullLogin(HttpServletRequest request);

    public abstract void notifyFailedLoginAttempt(HttpServletRequest request);

}
