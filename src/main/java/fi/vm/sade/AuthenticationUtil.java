package fi.vm.sade;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.vm.sade.auth.ldap.LdapUser;
import fi.vm.sade.auth.ldap.LdapUserImporter;
import fi.vm.sade.authentication.service.AuthenticationService;
import fi.vm.sade.authentication.service.CustomAttributeService;
import fi.vm.sade.authentication.service.types.IdentifiedHenkiloType;
import fi.vm.sade.generic.rest.CachingRestClient;
import fi.vm.sade.organisaatio.api.model.OrganisaatioService;
import fi.vm.sade.saml.action.SAMLCredentials;

/**
 * User: tommiha Date: 3/26/13 Time: 2:47 PM
 */
public class AuthenticationUtil {

    private LdapUserImporter ldapUserImporter;
    private String authenticationServiceWsdlUrl;
    private OrganisaatioService organisaatioService;
    private String rootOrganisaatioOid;
    private AuthenticationService authenticationService;
    private CustomAttributeService customAttributeService;
    private boolean useAuthenticationService;
    
    private String authenticationServiceRestUrl;
    private CachingRestClient restClient = new CachingRestClient();

    protected Logger log = LoggerFactory.getLogger(this.getClass());
    private static final Pattern hetuRegExp = Pattern.compile("([0-3][0-9])([0-1][0-9])([0-9]{2})(\\-|[A]|\\+)([0-9]{3})([0-9]|[A-Z])");

    @Deprecated
    public boolean tryAuthenticationWithCustomOphAuthenticationService(
            UsernamePasswordCredentials cred) {
        try {
            IdentifiedHenkiloType henkilo = authenticationService.getIdentityByUsernameAndPassword(cred.getUsername(), cred.getPassword(), true);

            // service vastasi, mutta käyttäjää ei löytynyt.
            if (henkilo == null) {
                log.info("User not found.");
                return false;
            }
        }
        catch (Exception e) {
            log.warn("WARNING - problem with authentication backend, using only ldap.");
        }
        return true;
    }

    @Deprecated
    public boolean tryToImportUserFromCustomOphAuthenticationService(
            UsernamePasswordCredentials cred) {

        if (useAuthenticationService) {
            log.error("DEBUG::Using Authentication Service!");
            try {
                IdentifiedHenkiloType henkilo = authenticationService.getIdentityByUsernameAndPassword(cred.getUsername(), cred.getPassword(), false);

                // service vastasi, mutta käyttäjää ei löytynyt.
                if (henkilo == null) {
                    log.info("User not found.");
                    return false;
                }
            }
            catch (Exception e) {
                log.warn("WARNING - problem with authentication backend, using only ldap.");//, e);
            }
        }
        return true;
    }

    public void tryToImportUserFromCustomOphAuthenticationService(SAMLCredentials cred) {
        try {
            IdentifiedHenkiloType henkiloType = null;
            Matcher m = hetuRegExp.matcher(cred.getToken());
            if (m.matches()) {
                henkiloType = restClient.get(authenticationServiceRestUrl + "cas/hetu/" + cred.getToken(), IdentifiedHenkiloType.class);
            }
            else {
                henkiloType = restClient.get(authenticationServiceRestUrl + "cas/auth/" + cred.getToken(), IdentifiedHenkiloType.class);
            }
            cred.setUserDetails(henkiloType);
        }
        catch (Exception e) {
            log.warn("WARNING - problem with authentication backend, using only ldap.", e);
            return;
        }
    }

    public LdapUserImporter getLdapUserImporter() {
        return ldapUserImporter;
    }

    public void setLdapUserImporter(LdapUserImporter ldapUserImporter) {
        this.ldapUserImporter = ldapUserImporter;
    }

    public String getAuthenticationServiceWsdlUrl() {
        return authenticationServiceWsdlUrl;
    }

    public void setAuthenticationServiceWsdlUrl(String authenticationServiceWsdlUrl) {
        this.authenticationServiceWsdlUrl = authenticationServiceWsdlUrl;
    }

    public OrganisaatioService getOrganisaatioService() {
        return organisaatioService;
    }

    public void setOrganisaatioService(OrganisaatioService organisaatioService) {
        this.organisaatioService = organisaatioService;
    }

    public String getRootOrganisaatioOid() {
        return rootOrganisaatioOid;
    }

    public void setRootOrganisaatioOid(String rootOrganisaatioOid) {
        this.rootOrganisaatioOid = rootOrganisaatioOid;
    }

    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public CustomAttributeService getCustomAttributeService() {
        return customAttributeService;
    }

    public void setCustomAttributeService(CustomAttributeService customAttributeService) {
        this.customAttributeService = customAttributeService;
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

    public boolean isUseAuthenticationService() {
        return useAuthenticationService;
    }

    public void setUseAuthenticationService(boolean useAuthenticationService) {
        this.useAuthenticationService = useAuthenticationService;
    }

    public String getAuthenticationServiceRestUrl() {
        return authenticationServiceRestUrl;
    }

    public void setAuthenticationServiceRestUrl(String authenticationServiceRestUrl) {
        this.authenticationServiceRestUrl = authenticationServiceRestUrl;
    }
}

