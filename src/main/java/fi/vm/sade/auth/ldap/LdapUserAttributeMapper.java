package fi.vm.sade.auth.ldap;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;

public class LdapUserAttributeMapper implements AttributesMapper {

    @Override
    public Object mapFromAttributes(Attributes attrs) throws NamingException {
        LdapUser user = new LdapUser();
        user.setFirstName(attrs.get("cn").get().toString());
        user.setLastName(attrs.get("sn").get().toString());
        user.setLang(attrs.get("preferredLanguage").get().toString());
        user.setOid(attrs.get("employeeNumber").get().toString());
        return user;
    }

}
