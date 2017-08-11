package fi.vm.sade;

import fi.vm.sade.auth.dto.IdentifiedHenkiloType;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.vm.sade.auth.ldap.LdapUser;
import fi.vm.sade.auth.ldap.LdapUserImporter;
import fi.vm.sade.generic.rest.CachingRestClient;
import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.saml.action.SAMLCredentials;

/**
 * User: tommiha Date: 3/26/13 Time: 2:47 PM
 */
public class AuthenticationUtil {

    private LdapUserImporter ldapUserImporter;
    private OphProperties ophProperties;
    private CachingRestClient restClient = new CachingRestClient().setClientSubSystemCode("authentication.cas");

    protected Logger log = LoggerFactory.getLogger(this.getClass());
    private static final Pattern hetuRegExp = Pattern.compile("([0-3][0-9])([0-1][0-9])([0-9]{2})(\\-|[A]|\\+)([0-9]{3})([0-9]|[A-Z])");

    public boolean tryToImportUserFromCustomOphAuthenticationService(SAMLCredentials cred) {
        boolean success = false;
        try {
            IdentifiedHenkiloType henkiloType = null;
            henkiloType = restClient.get(ophProperties.url("kayttooikeus-service.cas.henkiloByAuthToken", cred.getToken()), IdentifiedHenkiloType.class);
            
            if (henkiloType != null) {
                cred.setUserDetails(henkiloType);
                success = true;
            }
        }
        catch (Exception e) {
            log.warn("WARNING - problem with authentication backend, using only ldap.", e);
            success = false;
        }
        return success;
    }

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

    public OphProperties getOphProperties() {
        return ophProperties;
    }

    public void setOphProperties(OphProperties ophProperties) {
        this.ophProperties = ophProperties;
    }

}

