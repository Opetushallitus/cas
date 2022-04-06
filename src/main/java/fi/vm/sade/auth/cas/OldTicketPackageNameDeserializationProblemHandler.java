package fi.vm.sade.auth.cas;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import org.apereo.cas.ticket.ExpirationPolicy;

import java.io.IOException;

public class OldTicketPackageNameDeserializationProblemHandler extends DeserializationProblemHandler {
    static final String CAS_VERSION_6_0_8_EXPIRATION_POLICY_SUBCLASS_PACKAGE ="org.apereo.cas.ticket.support";
    static final String CAS_VERSION_6_1_7_2_EXPIRATION_POLICY_SUBCLASS_PACKAGE="org.apereo.cas.ticket.expiration";
    @Override
    public JavaType handleUnknownTypeId(DeserializationContext ctxt, JavaType baseType, String subTypeId, TypeIdResolver idResolver, String failureMsg) throws IOException {
        if (baseType.getRawClass().equals(ExpirationPolicy.class) &&
                subTypeId.contains(CAS_VERSION_6_0_8_EXPIRATION_POLICY_SUBCLASS_PACKAGE)) {
            String ticketType = subTypeId.substring(CAS_VERSION_6_0_8_EXPIRATION_POLICY_SUBCLASS_PACKAGE.length());
            return idResolver.typeFromId(ctxt, CAS_VERSION_6_1_7_2_EXPIRATION_POLICY_SUBCLASS_PACKAGE.concat(ticketType));

        }
        return null;
    }
}
