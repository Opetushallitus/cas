package fi.vm.sade.auth.ldap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.NameAlreadyBoundException;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import sun.misc.BASE64Encoder;

import javax.naming.Name;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Imports users to ldap, check save-method which takes LdapUser as parameter.
 *
 * @author Antti Salonen
 */
public class LdapUserImporter {

    private LdapContextSource contextSource;
    private LdapTemplate ldapTemplate;
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    public void init() {
        log.info("LdapUserImporter.init, contextSource: " + contextSource + ", ldapTemplate: " + ldapTemplate);
    }

    public void setContextSource(LdapContextSource contextSource) {
        this.contextSource = contextSource;
    }

    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public LdapUser save(final LdapUser user) {
        save("people", user.getDepartment(), user.getUid(), buildAttributes(user), "uid", false);

        // Update Groups
        for (String group : user.getGroups()) {
            Name groupDn = buildDn("groups", null, group, "cn");
            String member = "uid=" + user.getUid() + ",ou=people,dc=example,dc=com";
            try {
                // if group doesn't exist, bind it and add first uniqueMember
                //Name groupDn = save("groups", null, group, roleAttrs, "cn", true);
                Attributes roleAttrs = buildRoleAttributes(group, member);
                ldapTemplate.bind(groupDn, null, roleAttrs);
            } catch (NameAlreadyBoundException nabe) {
                // todo: parempi import-logiikka + ryhmien/käyttäjien poistaminen kun ei ole enää backendissä, import irti casista?
                // if group exists, just add new member - note if member already exits, this will fail silently
                DirContextOperations context = ldapTemplate.lookupContext(groupDn);
                context.addAttributeValue("uniqueMember", member);
                ldapTemplate.modifyAttributes(context);
            }
        }

        return user;
    }

    private Name save(String ou, String department, String id, Attributes attributes, String idAttribute, boolean failIfAlreadyExists) {
        // '#' replace tarvitaan facebook profiileja varten, koska niistä tulee uid: FacebookProfile#123456789, ja '#' ei kelpaa dn:ään
        Name dn = buildDn(ou, department, id.replaceAll("#", "_"), idAttribute);
        try {
            log.info("LdapUserImporter.save, bind to ldap: " + id + " (" + dn + ")");
            ldapTemplate.bind(dn, null, attributes);
        } catch (Exception e) {
            if (e instanceof NameAlreadyBoundException) {
                if (failIfAlreadyExists) {
                    throw (NameAlreadyBoundException)e;
                }
                log.info("LdapUserImporter.save, rebind to ldap: " + id + " (" + dn + ")");
                ldapTemplate.rebind(dn, null, attributes);
            } else {
                throw new RuntimeException(e);
            }
        }
        return dn;
    }

    public static Name buildDn(String ou, String department, String uid, String nameAttribute) {
        DistinguishedName dn = new DistinguishedName();
        dn.add("dc", "com");
        dn.add("dc", "example");
        dn.add("ou", ou);
        if (department != null) {
            dn.add("ou", department);
        }
        dn.add(nameAttribute, uid);
        return dn;
    }

    private Attributes buildAttributes(final LdapUser user) {
        Attributes attrs = new BasicAttributes();
        BasicAttribute ocattr = new BasicAttribute("objectclass");
        ocattr.add("person");
        ocattr.add("inetOrgPerson");
        //ocattr.add("organizationalPerson"); // inetorgperson perii tämän joten ei tarvetta
        attrs.put(ocattr);
        attrs.put("cn", user.getFirstName());
        attrs.put("sn", user.getLastName());
        attrs.put("givenName", user.getFirstName());
        if (user.getPassword() != null) {
            attrs.put("userPassword", "{SHA}" + this.encrypt(user.getPassword()));
        }
        attrs.put("mail", user.getEmail());
        return attrs;
    }

    private Attributes buildRoleAttributes(String role, String firstMember) {
        Attributes attrs = new BasicAttributes();
        BasicAttribute ocattr = new BasicAttribute("objectclass");
        ocattr.add("groupOfUniqueNames"); // ocattr.add("organizationalRole");
        attrs.put(ocattr);
        attrs.put("cn", role);
        attrs.put("uniqueMember", firstMember);
        return attrs;
    }

    public static String encrypt(final String plaintext) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage());
        }
        try {
            md.update(plaintext.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage());
        }
        byte raw[] = md.digest();
        String hash = (new BASE64Encoder()).encode(raw);
        return hash;
    }

    public LdapTemplate getLdapTemplate() {
        return ldapTemplate;
    }
}
