package fi.vm.sade.auth.ldap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;

public class LdapUserAttributeMapper implements AttributesMapper {

    private static final String EMPLOYEE_NUMBER = "employeeNumber";
    private static final String PREFERRED_LANGUAGE = "preferredLanguage";
    private static final String SN = "sn";
    private static final String CN = "cn";

    private static final Logger LOG = LoggerFactory.getLogger(LdapUserImporter.class);

    @Override
    public Object mapFromAttributes(Attributes attrs) throws NamingException {

        LdapUser user = new LdapUser();

       user.setFirstName(getAttributeValueAsString(attrs, CN, null));
        user.setLastName(getAttributeValueAsString(attrs, SN, null));
        user.setLang(getAttributeValueAsString(attrs, PREFERRED_LANGUAGE, "fi"));
        user.setOid(getAttributeValueAsString(attrs, EMPLOYEE_NUMBER, null));
        return user;

    }

    private boolean isNull(Attribute a) throws NamingException {
        return a == null || a.get() == null;
    }

    private String getAttributeValueAsString(Attributes attrs, String key, String defaultValueIfNull) throws NamingException {
        if (attrs == null) {
            LOG.warn("getAttributeValueAsString() - user attributes == NULL");
            return defaultValueIfNull;
        }

        if (attrs.get(key) == null) {
            LOG.warn("getAttributeValueAsString() - user attribute with key {} == NULL", key);
            return defaultValueIfNull;
        }

        if (attrs.get(key).get() == null) {
            LOG.warn("getAttributeValueAsString() - user attribute value with key {} == NULL", key);
            return defaultValueIfNull;
        }

        return attrs.get(key).get().toString();
    }
}
