package fi.vm.sade.login.failure;

import javax.servlet.http.HttpServletRequest;

import fi.vm.sade.auth.exception.UsernameMissingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByIpAndUserInMemoryLoginFailureHandlerInterceptorAdapter extends AbstractInMemoryLoginFailureHandlerInterceptorAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ByIpAndUserInMemoryLoginFailureHandlerInterceptorAdapter.class);

    @Override
    public String createKey(HttpServletRequest request) throws UsernameMissingException {

        String ipAddress = request.getHeader("x-real-ip");
        if(null == ipAddress) {
            ipAddress = request.getHeader("x-forwarded-for");
            if(null == ipAddress) {
                LOGGER.warn("Unable to find x-real-ip or x-forwarded-for request header!! Using remote address instead!!");
                ipAddress = request.getRemoteAddr();
            }
        }

        String username = request.getParameter("username");
        if(null == username) {
            throw new UsernameMissingException("Cannot create key because username is missing");
        }

        return ipAddress + ";" + username.toLowerCase();
    }
}
