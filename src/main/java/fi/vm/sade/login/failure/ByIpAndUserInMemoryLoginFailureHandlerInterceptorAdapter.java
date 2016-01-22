package fi.vm.sade.login.failure;

import javax.servlet.http.HttpServletRequest;

public class ByIpAndUserInMemoryLoginFailureHandlerInterceptorAdapter extends AbstractInMemoryLoginFailureHandlerInterceptorAdapter {

    @Override
    public String createKey(HttpServletRequest request) {
        String username = request.getParameter("username");
        if(null == username) {
            return request.getRemoteAddr();
        }
        return request.getRemoteAddr() + ";" + username.toLowerCase();
    }
}
