package fi.vm.sade;

import java.util.List;

import fi.vm.sade.auth.ldap.LdapUser;
import fi.vm.sade.auth.ldap.LdapUserImporter;

public class AuthenticationUtil {

    private LdapUserImporter ldapUserImporter;

    public LdapUserImporter getLdapUserImporter() {
        return ldapUserImporter;
    }

    public void setLdapUserImporter(LdapUserImporter ldapUserImporter) {
        this.ldapUserImporter = ldapUserImporter;
    }

    public String getUserRoles(String uid) {
        return ldapUserImporter.getUserRolesAndGroups(uid);
    }
    
    public List<String> getRoles(String uid) {
        String member = ldapUserImporter.getMemberString(uid);
        return ldapUserImporter.getUserLdapGroups(member);
    }
    
    public LdapUser getUser(String uid) {
        return ldapUserImporter.getLdapUser(uid);
    }

}
