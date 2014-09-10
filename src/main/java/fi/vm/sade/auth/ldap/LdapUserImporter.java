package fi.vm.sade.auth.ldap;

import java.util.List;

import javax.naming.Name;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.filter.EqualsFilter;

/**
 * Imports users to ldap, check save-method which takes LdapUser as parameter.
 *
 * @author Antti Salonen
 */
public class LdapUserImporter {

    private LdapContextSource contextSource;
    private LdapTemplate ldapTemplate;
    private static final Logger log = LoggerFactory.getLogger(LdapUserImporter.class);

    public void init() {
        log.info("LdapUserImporter.init, contextSource: " + contextSource + ", ldapTemplate: " + ldapTemplate);
    }

    public void setContextSource(LdapContextSource contextSource) {
        this.contextSource = contextSource;
    }

    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public String getMemberString(String uid) {
        return "uid=" + uid + ",ou=People,dc=opintopolku,dc=fi";
    }

    public List<String> getUserLdapGroups(String member) {
        Name groupdDn = buildDn("groups", null, null, null);
        return ldapTemplate.search(groupdDn, new EqualsFilter("uniqueMember", member).encode(), new ContextMapper() {
            @Override
            public Object mapFromContext(Object o) {
                return ((DirContextOperations)o).getStringAttribute("cn");
            }
        });
    }
    
    public String getUserRolesAndGroups(String uid) {
        return getLdapUser(uid).getRoles();
    }

    public List<String> getUserLdapAttributes() {
        ldapTemplate.lookup((Name)null);
        return null;
    }

    public LdapUser getLdapUser(String uid) {
        final Name name = buildDn("uid", uid, "People");
        return (LdapUser) ldapTemplate.lookup(name, new LdapUserAttributeMapper());
    }

    public static Name buildDn(String ou, String extraDepartment, String uid, String nameAttribute) {
        DistinguishedName dn = new DistinguishedName();
        dn.add("dc", "fi");
        dn.add("dc", "opintopolku");
        dn.add("ou", ou);
        if (extraDepartment != null) {
            dn.add("ou", extraDepartment);
        }
        if (nameAttribute != null) {
            dn.add(nameAttribute, uid);
        }
        return dn;
    }

    public static Name buildDn(String nameAttribute, String uid, String... ous) {
        DistinguishedName dn = new DistinguishedName();
        dn.add("dc", "fi");
        dn.add("dc", "opintopolku");
        for (String ou : ous) {
            dn.add("ou", ou);
        }
        dn.add(nameAttribute, uid);
        return dn;
    }

    public Attributes buildAttributes(String... objectClasses) {
        Attributes attrs = new BasicAttributes();
        BasicAttribute ocattr = new BasicAttribute("objectclass");
        for (String objectClass : objectClasses) {
            ocattr.add(objectClass);
        }
        //ocattr.add("organizationalPerson"); // inetorgperson perii tämän joten ei tarvetta
        attrs.put(ocattr);
        return attrs;
    }

    public LdapTemplate getLdapTemplate() {
        return ldapTemplate;
    }
}
