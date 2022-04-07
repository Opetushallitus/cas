package fi.vm.sade.auth.cas;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import org.apereo.cas.authentication.DefaultAuthentication;
import org.apereo.cas.authentication.principal.DefaultPrincipalAttributesRepository;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

public class OldTicketDeserializationProblemHandler extends DeserializationProblemHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(OldTicketDeserializationProblemHandler.class);
    static final String CAS_6_0_8_EXPIRATION_POLICY_SUBCLASS_PACKAGE = "org.apereo.cas.ticket.support";
    static final String CAS_6_1_7_2_EXPIRATION_POLICY_SUBCLASS_PACKAGE = "org.apereo.cas.ticket.expiration";
    static final String[] CAS_6_1_7_2_DROPPED_PRINCIPAL_ATTRIBUTES_REPOSITORY_PROPERTIES = {"timeUnit", "expiration"};

    @Override
    public JavaType handleUnknownTypeId(DeserializationContext ctxt, JavaType baseType, String subTypeId,
                                        TypeIdResolver idResolver, String failureMsg) throws IOException {
        if (baseType.getRawClass().equals(ExpirationPolicy.class) && subTypeId.contains(CAS_6_0_8_EXPIRATION_POLICY_SUBCLASS_PACKAGE)) {
            String ticketType = subTypeId.substring(CAS_6_0_8_EXPIRATION_POLICY_SUBCLASS_PACKAGE.length());
            return idResolver.typeFromId(ctxt, CAS_6_1_7_2_EXPIRATION_POLICY_SUBCLASS_PACKAGE.concat(ticketType));

        }
        return null;
    }

    @Override
    public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser p, JsonDeserializer<?> deserializer,
                                         Object beanOrClass, String propertyName) {

        if (beanOrClass.getClass().equals(DefaultPrincipalAttributesRepository.class)
                && Arrays.asList(CAS_6_1_7_2_DROPPED_PRINCIPAL_ATTRIBUTES_REPOSITORY_PROPERTIES).contains(propertyName)) {
            LOGGER.info("Unknown property for DefaultPrincipalAttributesRepository noticed: {}, ignoring",
                    propertyName);
            return true;
        }

        if (beanOrClass.getClass().equals(DefaultAuthentication.class)) {
            LOGGER.info("Unknown property for DefaultAuthentication noticed: {}, ignoring", propertyName);
            return true;
        }
        return false;
    }
}
