package fi.vm.sade.login.failure;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByIpAndUserInMemoryLoginFailureHandlerInterceptorAdapter extends AbstractInMemoryLoginFailureHandlerInterceptorAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ByIpAndUserInMemoryLoginFailureHandlerInterceptorAdapter.class);

    @Override
    public String createKey(HttpServletRequest request) {
        String username = request.getParameter("username");
        String ipAddress = request.getHeader("x-forwarded-for");

        if(null == ipAddress) {
            LOGGER.warn("Unable to find x-forwarded-for request header!! Using remote address instead!!");
            ipAddress = request.getRemoteAddr();
        }

        if(null == username) {
            return ipAddress;
        }

        return ipAddress + ";" + username.toLowerCase();
    }
}
