package fi.vm.sade.login.failure;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public class ByIpAndUserInMemoryLoginFailureHandlerInterceptorAdapter extends AbstractInMemoryLoginFailureHandlerInterceptorAdapter {

    @Override
    public String createKey(HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        String username = request.getParameter(getUsernameParameter());
        return Stream.of(ipAddress, username)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(joining(";"));
    }
}
