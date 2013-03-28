package fi.vm.sade.auth.ldap;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.NameAlreadyBoundException;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.filter.EqualsFilter;
import sun.misc.BASE64Encoder;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

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

        // save user
        save("people", user.getDepartment(), user.getUid(), buildAttributes(user), "uid", false);

        // save groups...

        String member = "uid=" + user.getUid() + ",ou=people,dc=example,dc=com";

        // Update Groups to remove user's old memberships
        List<DirContextOperations> allgroups = ldapTemplate.search(buildDn("groups", null, null, null), new EqualsFilter("objectclass", "groupOfUniqueNames").encode(), new ContextMapper() {
            @Override
            public Object mapFromContext(Object o) {
                return o;
            }
        });
        for (DirContextOperations group : allgroups) {
            removeUniqueMember(group, member, "mail="+user.getEmail()); // todo: tuo maili memberinä deprecated, käytetäänkö jossain muka vielä?
        }

        // todo: parempi import-logiikka + ryhmien/käyttäjien poistaminen kun ei ole enää backendissä, ei rebindia koskaan, import irti casista? alhainen prioriteetti koska ei haittaa muuta kuin roskaa ldappia

        // Update Groups to add current memberships
        for (String group : user.getGroups()) {
            Name groupDn = buildDn("groups", null, group, "cn");
            try {
                // if group doesn't exist, bind it and add first uniqueMember
                //Name groupDn = save("groups", null, group, roleAttrs, "cn", true);
                Attributes roleAttrs = buildRoleAttributes(group, member);
                ldapTemplate.bind(groupDn, null, roleAttrs);
            } catch (NameAlreadyBoundException nabe) {
                // if group exists, just add new member - note if member already exits, this will fail silently
                DirContextOperations context = ldapTemplate.lookupContext(groupDn);
                context.addAttributeValue("uniqueMember", member);
                ldapTemplate.modifyAttributes(context);
            }
        }

        return user;
    }

    private void removeUniqueMember(DirContextOperations group, String... membersToRemove) {
        List<String> uniqueMembers = Arrays.asList(group.getStringAttributes("uniqueMember"));
        List<String> membersToRemoveList = Arrays.asList(membersToRemove);
        if (CollectionUtils.containsAny(uniqueMembers, membersToRemoveList)) {
            Collection newUniqueMembers = CollectionUtils.subtract(uniqueMembers, membersToRemoveList);
            if (newUniqueMembers.size() == 0) { // mikäli membereitä jäisi nolla, pitää poista koko group
//                System.out.println("    remove uniquemember, delete empty group, group: "+group.getDn()+", membersToRemove: "+membersToRemoveList+", uniqueMembers: "+uniqueMembers);
                ldapTemplate.unbind(group.getDn());
            } else {
                group.setAttributeValues("uniqueMember", newUniqueMembers.toArray());
                //group.removeAttributeValue("uniqueMember", member);
//                System.out.println("    remove uniqueMember, group: " + group.getDn() + ", membersToRemove: " + membersToRemoveList + ", uniqueMembers: " + uniqueMembers + ", modification: " + Arrays.asList(group.getModificationItems()));
                ldapTemplate.modifyAttributes(group);
            }
        }
    }

    public Name save(String ou, String department, String id, Attributes attributes, String idAttribute, boolean failIfAlreadyExists) {
        // '#' replace tarvitaan facebook profiileja varten, koska niistä tulee uid: FacebookProfile#123456789, ja '#' ei kelpaa dn:ään
        Name dn = buildDn(ou, department, id.replaceAll("#", "_"), idAttribute);
        return save(dn, attributes, failIfAlreadyExists);
    }

    public Name save(Name dn, Attributes attributes, boolean failIfAlreadyExists) {
        try {
            log.info("LdapUserImporter.save, bind to ldap: " + dn);
            ldapTemplate.bind(dn, null, attributes);
        } catch (Exception e) {
            if (e instanceof NameAlreadyBoundException) {
                if (failIfAlreadyExists) {
                    throw (NameAlreadyBoundException)e;
                }
                log.info("LdapUserImporter.save, rebind to ldap: " + dn);
                ldapTemplate.rebind(dn, null, attributes);
            } else {
                throw new RuntimeException(e);
            }
        }
        return dn;
    }

    public static Name buildDn(String ou, String extraDepartment, String uid, String nameAttribute) {
        DistinguishedName dn = new DistinguishedName();
        dn.add("dc", "com");
        dn.add("dc", "example");
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
        dn.add("dc", "com");
        dn.add("dc", "example");
        for (String ou : ous) {
            dn.add("ou", ou);
        }
        dn.add(nameAttribute, uid);
        return dn;
    }

    private Attributes buildAttributes(final LdapUser user) {
        Attributes attrs = buildAttributes(new String[]{"person", "inetOrgPerson"});
        attrs.put("cn", user.getFirstName());
        attrs.put("sn", user.getLastName());
        attrs.put("givenName", user.getFirstName());
        if (user.getOid() != null) {
            attrs.put("employeeNumber", user.getOid());
        }
        if (user.getPassword() != null) {
            attrs.put("userPassword", "{SHA}" + this.encrypt(user.getPassword()));
        }
        attrs.put("mail", user.getEmail());
        return attrs;
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
