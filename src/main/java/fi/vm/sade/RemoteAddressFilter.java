package fi.vm.sade;

import fi.vm.sade.javautils.http.HttpServletRequestUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.IOException;

// Copied from fi.vm.sade.java-utils:java-http:0.5.0-SNAPSHOT
// Updated to work with Java 21
public class RemoteAddressFilter implements Filter {
    public RemoteAddressFilter() {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest) {
            chain.doFilter(new HttpServletRequestWrapper(httpRequest) {
                public String getRemoteAddr() {
                    return getRemoteAddress(httpRequest);
                }
            }, response);
        } else {
            chain.doFilter(request, response);
        }
    }
    public static String getRemoteAddress(HttpServletRequest httpRequest) {
        return HttpServletRequestUtils.getRemoteAddress(
                httpRequest.getHeader("X-Real-IP"),
                httpRequest.getHeader("X-Forwarded-For"),
                httpRequest.getRemoteAddr(),
                httpRequest.getRequestURI()
        );
    }
}
