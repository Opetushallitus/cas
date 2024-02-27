package fi.vm.sade.auth.interrupt;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.interrupt.InterruptResponse;
import org.apereo.cas.interrupt.InterruptTrackingEngine;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.WebUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.execution.RequestContext;
import java.util.Optional;

@RequiredArgsConstructor
@Getter
@Qualifier(InterruptTrackingEngine.BEAN_NAME)
public class CustomInterruptTrackingEngine implements InterruptTrackingEngine {
    private static final Logger log = LoggerFactory.getLogger(CustomInterruptTrackingEngine.class);

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final CasCookieBuilder casCookieBuilder;

    private final CasConfigurationProperties casProperties;

    @Override
    public void trackInterrupt(final RequestContext requestContext, final InterruptResponse response) throws Throwable {
        val httpRequest = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val httpResponse = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val authentication = WebUtils.getAuthentication(requestContext);
        authentication.addAttribute(AUTHENTICATION_ATTRIBUTE_FINALIZED_INTERRUPT, Boolean.TRUE);
        log.warn("authentication attribute saved for " + authentication.getPrincipal().getId());
        val cookieValue = EncodingUtils.encodeBase64(MAPPER.writeValueAsString(response));
        log.warn("cookie value saved for " + authentication.getPrincipal().getId() + ":" + cookieValue);
        casCookieBuilder.addCookie(httpRequest, httpResponse, cookieValue);
    }

    @Override
    public Optional<InterruptResponse> forCurrentRequest(final RequestContext requestContext) {
        return FunctionUtils.doAndHandle(__ -> {
            val httpRequest = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            val cookieValue = casCookieBuilder.retrieveCookieValue(httpRequest);
            val authentication = WebUtils.getAuthentication(requestContext);
            log.warn("cookie value " + authentication.getPrincipal().getId() + ":" + cookieValue);
            return StringUtils.isNotBlank(cookieValue)
                ? Optional.ofNullable(MAPPER.readValue(EncodingUtils.decodeBase64ToString(cookieValue), InterruptResponse.class))
                : Optional.<InterruptResponse>empty();
        }, e -> Optional.<InterruptResponse>empty()).apply(requestContext);
    }

    @Override
    public boolean isInterrupted(final RequestContext requestContext) {
        return FunctionUtils.doAndHandle(
                __ -> {
                    val interruptResponse = forCurrentRequest(requestContext);
                    val authentication = WebUtils.getAuthentication(requestContext);
                    log.warn("check if is interrupted for " + authentication.getPrincipal().getId() + ":" + (authentication != null && authentication.containsAttribute(AUTHENTICATION_ATTRIBUTE_FINALIZED_INTERRUPT)));
                    return interruptResponse.stream().anyMatch(InterruptResponse::isInterrupt)
                        || (authentication != null && authentication.containsAttribute(AUTHENTICATION_ATTRIBUTE_FINALIZED_INTERRUPT));
                }, e -> false)
            .apply(requestContext);
    }
}
