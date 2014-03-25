package fi.vm.sade.auth.ldap;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;

public class LdapUserAttributeMapper implements AttributesMapper {

    private static final String EMPLOYEE_NUMBER = "employeeNumber";
    private static final String PREFERRED_LANGUAGE = "preferredLanguage";
    private static final String SN = "sn";
    private static final String CN = "cn";

    @Override
    public Object mapFromAttributes(Attributes attrs) throws NamingException {

        LdapUser user = new LdapUser();
        if (!isNull(attrs.get(CN))) {
            user.setFirstName(attrs.get(CN).get().toString());
        }
        if (!isNull(attrs.get(SN))) {
            user.setLastName(attrs.get(SN).get().toString());
        }
        if (!isNull(attrs.get(PREFERRED_LANGUAGE))) {
            user.setLang(attrs.get(PREFERRED_LANGUAGE).get().toString());
        }
        if (!isNull(attrs.get(EMPLOYEE_NUMBER))) {
            user.setOid(attrs.get(EMPLOYEE_NUMBER).get().toString());
        }
        return user;

    }

    private boolean isNull(Attribute a) throws NamingException {
        return a == null || a.get() == null;
    }

}
