package fi.vm.sade.login.throttling;

import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CasInMemoryByUserAndIpLoginFailureFilter extends AbstractInMemoryByUserAndIpLoginFailureFilter {

    @Override
    public boolean isSuccessLogin(HttpServletRequest request, HttpServletResponse response) {
        RequestContext context = (RequestContext) request.getAttribute("flowRequestContext");
        if( null == context || null == context.getCurrentEvent() ) {
            return false;
        }
        return "success".equals(context.getCurrentEvent().getId());
    }
}
