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
import org.springframework.security.authentication.encoding.LdapShaPasswordEncoder;

import javax.naming.Name;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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

    public LdapUser update(final LdapUser user) {
        save("People", user.getDepartment(), user.getUid(), buildAttributes(user), "uid", false);
        return user;
    }
    
    public LdapUser save(final LdapUser user) {
        synchronized (user.getUid().intern()) {

            // save user
            save("People", user.getDepartment(), user.getUid(), buildAttributes(user), "uid", false);

            // save groups...

            List<String> userBackendGroups = Arrays.asList(user.getGroups());
            String member = getMemberString(user.getUid());

            // get current ldap groups
            List<String> userLdapGroups = getUserLdapGroups(member);
            Collection<String> deletedMemberships = CollectionUtils.subtract(userLdapGroups, userBackendGroups);
            Collection<String> addedMemberships = CollectionUtils.subtract(userBackendGroups, userLdapGroups);
            if(log.isDebugEnabled()) {
                log.debug("user: {}, backendGroups: {}, ldapGroups: {}", new Object[]{user.getEmail(), userBackendGroups.size(), userLdapGroups.size()});
                log.debug("user: {}, deletedMemberships: {}", user.getEmail(), deletedMemberships);
                log.debug("user: {}, addedMemberships: {}", user.getEmail(), addedMemberships);
            }
            // remove deleted membership (membership deleted in backend groups but exists in ldap groups)
            for (String deletedMembership : deletedMemberships) {
                Name groupDn = buildDn("groups", null, deletedMembership, "cn");
                log.debug("user: {}, ...remove membership to: {}", user.getEmail(), deletedMembership);
                DirContextOperations group = ldapTemplate.lookupContext(groupDn);
                removeUniqueMember(group, member, "mail="+user.getEmail()); // todo: tuo maili memberinä deprecated, käytetäänkö jossain muka vielä?
            }
            // add new membership (membership exists in backend groups but not in ldap groups)
            for (String group : addedMemberships) {
                Name groupDn = buildDn("groups", null, group, "cn");
                log.debug("user: {}, ...add membership to: {}", user.getEmail(), group);
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

            log.info("save done, user: "+user);

            return user;
        }
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

    public List<String> getUserLdapAttributes() {
        ldapTemplate.lookup((Name)null);
        return null;
    }

    private final LdapUserAttributeMapper mapper = new LdapUserAttributeMapper();

    public LdapUser getLdapUser(String uid) {
        final Name name = buildDn("uid", uid, "People");
        return (LdapUser) ldapTemplate.lookup(name, new LdapUserAttributeMapper());
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

    private Name save(String ou, String department, String id, Attributes attributes, String idAttribute, boolean failIfAlreadyExists) {
        // '#' replace tarvitaan facebook profiileja varten, koska niistä tulee uid: FacebookProfile#123456789, ja '#' ei kelpaa dn:ään
        Name dn = buildDn(ou, department, id.replaceAll("#", "_"), idAttribute);
        return save(dn, attributes, failIfAlreadyExists);
    }

    private Name save(Name dn, Attributes attributes, boolean failIfAlreadyExists) {
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

    public void remove(LdapUser user) {
        Name name = buildDn("People", user.getDepartment(), user.getUid(), "uid");
        remove(name);
    }

    public void remove(Name dn) {
        ldapTemplate.unbind(dn);
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

    private Attributes buildAttributes(final LdapUser user) {
        Attributes attrs = buildAttributes(new String[]{"person", "inetOrgPerson"});
        attrs.put("cn", user.getFirstName());
        attrs.put("sn", user.getLastName());
        attrs.put("givenName", user.getFirstName());
        if (user.getLang() != null) {
            attrs.put("preferredLanguage", user.getLang());
        }
        if (user.getOid() != null) {
            attrs.put("employeeNumber", user.getOid());
        }
        if (user.getPassword() != null) {
            attrs.put("userPassword", encrypt(user.getPassword()));
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
        // create random 4 byte salt
        byte[] salt = new byte[4];
        new SecureRandom().nextBytes(salt);
        // create digest
        LdapShaPasswordEncoder encoder = new LdapShaPasswordEncoder();
        String digest = encoder.encodePassword(plaintext, salt);
        return digest;
    }

    public LdapTemplate getLdapTemplate() {
        return ldapTemplate;
    }
}
