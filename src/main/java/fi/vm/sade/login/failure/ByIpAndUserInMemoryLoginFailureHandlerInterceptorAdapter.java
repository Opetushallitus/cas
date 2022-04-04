package fi.vm.sade.login.failure;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public class ByIpAndUserInMemoryLoginFailureHandlerInterceptorAdapter extends AbstractInMemoryLoginFailureHandlerInterceptorAdapter {

    public ByIpAndUserInMemoryLoginFailureHandlerInterceptorAdapter(LoginFailureStore loginFailureStore) {
        super(loginFailureStore);
    }

    @Override
    public Optional<String> createKey(HttpServletRequest request) {
        return Optional.ofNullable(request.getParameter(getUsernameParameterFromRequest(request)))
                .map(username -> request.getRemoteAddr() + ";" + username.toLowerCase());
    }
}
