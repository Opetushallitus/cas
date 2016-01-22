package fi.vm.sade.login.throttling;

import javax.servlet.http.HttpServletRequest;

public abstract class AbstractInMemoryByUserAndIpLoginFailureFilter extends AbstractInMemoryLoginFailureFilter {

    @Override
    public String createKey(HttpServletRequest request) {
        String username = request.getParameter("username");
        if(null == username) {
            return request.getRemoteAddr();
        }
        return request.getRemoteAddr() + ";" + username.toLowerCase();
    }

}
