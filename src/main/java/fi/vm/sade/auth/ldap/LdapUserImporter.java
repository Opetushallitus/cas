package fi.vm.sade.auth.ldap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * WORKLOG:
 *
 * -apacheds ladattu
 * -apacheds konffiin vaihdettu ads-pwdCheckQuality: 2 -> 1
 * EI-apacheds lisätty partitio oph.fi (config/partitions.. + server stop + lisätty config.ldif tuolla mainittu http://mail-archives.apache.org/mod_mbox/directory-users/201109.mbox/%3CCABzFU-ce+eA-VveYdDgdtaqfY_24-gFoW6F2Q4j6W=8BkNWS9Q@mail.gmail.com%3E)
 * EI-apacheds luotu domain dc=oph,dc=fi (root des, new ctx entry, 'domain',)
 * -luotu example.com: ou=people + ou=groups
 *
 cas logi jatkuu:
 -luotu facebook app
 -konffittu cassiin fb auth
 -konffittu liferayhyn portal-ext.properties cas enabled ja urlit oikeaksi
 -luotu localhost keystore ja laitettu se java_optsiin java.net truststoreksi JA tompan ssl keystoreksi
 -käsin konffattu liferayhyn password change required pois, sitten taas cas enabloitu
 -OK: TODO: liferay login pääkäyttäjälle cassin ohi? - toimii ihan päänäkymän login portletilla
 -lisätty virkailija-ryhmä liferayhyn, ja konffattu se virk.tp siten jäseneksi (jokainen virkailija user liitetään ko ryhmään - TODO: tällä hetkellä ldap import vaiheessa)

 -TODO: cas/facebook hajosi ilm kun cas/liferay certit laittoi kuntoon?!?!??!
 -TODO: group/role nimet tulee pienellä

 CERT:
 -tomcat tarvitsee localhost_keystore.jks:n ssl konffeihin server.xml:ään
 -tomcat tarvitsee localhost_keystore.jks:n http konffeihin setenv.bat/sh:iin
 -java tarvitsee localhost_certificate.cert:n importattua javan cacertteihin
    keytool -import -alias tomcat -file localhost_certificate.cert -keypass changeit -keystore "C:\Program Files\Java\jre6/lib/security/cacerts"
    HUOM! jren pitää olla täsmälleen sama mitä tomcat käyttää!!!
 -huom:
    -cas http / fb oauth toimii pelkällä java cacerts kohdalla (eikä oo ihan varma tartteeko sitäkään?)
    -jos setenv + ssl jks, fb oauth ei toimi HTTP:llä!
        toimiiko https:llä? ei toimi.. teoria on, että setenvissä ei saa ylikirjoittaa javax.net.ssl.trustStorea, koska sitten siellä ei ole fb certtiä tms?
        ONGELMA olikin, että cacert oli importattu väärälle jre:lle, eli ratkaisu on jks tomcat ssl:lle server.xmlssä + cert import jre cacertseihin + EI setenv-säätöä
            EIH, cas->life ei toimi
        ...koitetaan liferay-cassia http:llä eikä https
    -life http -> cas https toimii vain jos setenv+ssl cert
 -VAIHDETTU PELKKÄ HTTP, EI SERTTEJÄ

 CAS PGT:
 -muutettu CasFilteriä PGT supportin takia, deploy:
    mvn install && xcopy /e /y target\classes\com\* \Users\Antti\servers\liferay\apache-tomcat-7.0.32\webapps\ROOT\WEB-INF\classes\com
 -TEMP CasFilterissä PGT validointikin, pitäis siirtää johonkin CXF proxyyn tms?
 -proxy ticket saatu - TODO: backendsessio + autorisointi
 -PGT toimii nyt - TODO: laita toimimaan kaikilla serviceillä (nyt vain authservice)
 -TODO: pitää tehdä ylimääräinen /c/portal/login kutsu authin jälkeen jotta proxyticket saadaan sessioon
 -TODO:


 -links:
    https://wiki.jasig.org/display/CAS/Proxy+CAS+Walkthrough
    http://www.liferay.com/community/wiki/-/wiki/Main/CAS+Liferay+6+Integration
    http://issues.liferay.com/browse/LPS-7441
    http://forum.springsource.org/showthread.php?47094-CAS-LDAP-Authorization
    http://jasig.275507.n4.nabble.com/using-CAS-for-web-service-authentication-td2131034.html
 http://owulff.blogspot.fi/2011/11/configure-tomcat-for-federation-part.html
 https://wiki.jasig.org/display/CASC/Tomcat+Container+Authentication
 https://liitu.hard.ware.fi/confluence/display/PROG/CAS+POC

 *
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
        save("people", user.getDepartment(), user.getUid(), buildAttributes(user), "uid");

        // Update Groups
        for (String group : user.getGroups()) {
            /*
            DistinguishedName groupDn = new DistinguishedName();
            groupDn.add("dc", "com");
            groupDn.add("dc", "example");
            groupDn.add("ou", "groups");
            groupDn.add("cn", group);
            */

            // luo rooli jollei ole
            Attributes roleAttrs = buildRoleAttributes(group);

            // TODO: jäsenyys pitää lisätä jo tässä ettei hajoo group/role luomiseen koska uniquemember attribuutti puuttuu
            // TODO: salliiko enää monta jäsentä näin?
            BasicAttribute uniqueMember = new BasicAttribute("uniqueMember");
            //uniqueMember.add("uid="+user.getUid());
            uniqueMember.add("uid="+user.getUid()+",ou=people,dc=example,dc=com"); // TODO: temp
            roleAttrs.put(uniqueMember);

            Name groupDn = save("groups", null, group, roleAttrs, "cn");
            //Name groupDn = buildDn("groups", null, group, "cn"); // TODO: roles olisi parempi kuin groups?

            DirContextOperations context = ldapTemplate.lookupContext(groupDn);
//            context.addAttributeValue("memberUid", user.getUid()); posixgroup
//            context.addAttributeValue("roleOccupant", "uid="+user.getUid()); // organizationalrole
            //context.addAttributeValue("uniqueMember", "uid="+user.getUid()); // organizationalrole
//            context.addAttributeValue("uniqueMember", "uid="+buildDn("people", user.getDepartment(), user.getUid(), "uid"));

            ldapTemplate.modifyAttributes(context);
        }

        /*
        // Update Roles - TODO: ei näy ldapissa?
        for (String role : user.getRoles()) {
            DistinguishedName groupDn = new DistinguishedName();
            groupDn.add("ou", "OphRoles");
            groupDn.add("cn", role);
            DirContextOperations context = ldapTemplate.lookupContext(groupDn);
            context.addAttributeValue("memberUid", user.getUid());
            ldapTemplate.modifyAttributes(context);
        }
        */

        return user;
    }

    private Name save(String ou, String department, String id, Attributes attributes, String idAttribute) {
        // '#' replace tarvitaan facebook profiileja varten, koska niistä tulee uid: FacebookProfile#123456789, '#' ei kelpaa dn:ään
        Name dn = buildDn(ou, department, id.replaceAll("#", "_"), idAttribute);
        try {
            ldapTemplate.bind(dn, null, attributes);
        } catch (Exception e) {
            if (e.toString().contains("ENTRY_ALREADY_EXISTS")) { // todo: poc update ldap entry
                log.info("LdapUserImporter.save, alredy exists: " + id);
                ldapTemplate.rebind(dn, null, attributes);
            } else {
                e.printStackTrace();
            }
        }
        log.info("LdapUserImporter.save, saved to ldap: " + id + " (" + dn + ")");
        return dn;
    }

    public static Name buildDn(String ou, String department, String uid, String nameAttribute) {
        DistinguishedName dn = new DistinguishedName();
        /*
        dn.add("ou", "system");
        */
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
        ocattr.add("organizationalPerson"); // todo: inetorgperson perii tämän joten ei tarvetta
        attrs.put(ocattr);
        attrs.put("cn", user.getFirstName());
        attrs.put("sn", user.getLastName());
        attrs.put("givenName", user.getFirstName());
        if (user.getPassword() != null) {
            attrs.put("userPassword", "{SHA}" + this.encrypt(user.getPassword()));
        }
//        attrs.put("userPassword", user.getPassword());
        attrs.put("mail", user.getEmail());
//        attrs.put("member", "cn=asd2");

        /*
        if (user.getRoles().length > 0) {
//        attrs.put("ophRoles", Arrays.asList(user.getRoles()));
            BasicAttribute ophRoles = new BasicAttribute("ophRoles");
            for (String role : user.getRoles()) {
                ophRoles.add(role);
            }
            attrs.put(ophRoles);
        }
        */

        return attrs;
    }

    private Attributes buildRoleAttributes(String role) {
        Attributes attrs = new BasicAttributes();
        BasicAttribute ocattr = new BasicAttribute("objectclass");
//        ocattr.add("organizationalRole");
        ocattr.add("groupOfUniqueNames");
        attrs.put(ocattr);
        attrs.put("cn", role);
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
