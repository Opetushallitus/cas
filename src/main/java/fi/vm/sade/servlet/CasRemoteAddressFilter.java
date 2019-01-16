package fi.vm.sade.servlet;

import fi.vm.sade.javautils.http.RemoteAddressFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CasRemoteAddressFilter extends RemoteAddressFilter {
}
