package fi.vm.sade;

import fi.vm.sade.auth.ldap.LdapUser;
import fi.vm.sade.auth.ldap.LdapUserImporter;
import fi.vm.sade.auth.ldap.exception.UserDisabledException;
import fi.vm.sade.authentication.service.AuthenticationService;
import fi.vm.sade.authentication.service.AuthenticationService_Service;
import fi.vm.sade.authentication.service.types.AccessRightType;
import fi.vm.sade.authentication.service.types.IdentifiedHenkiloType;
import fi.vm.sade.organisaatio.api.model.OrganisaatioService;
import fi.vm.sade.saml.action.SAMLCredentials;
import org.apache.commons.lang.StringUtils;
import org.jasig.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * User: tommiha
 * Date: 3/26/13
 * Time: 2:47 PM
 */
public class AuthenticationUtil {

    private LdapUserImporter ldapUserImporter;
    private String authenticationServiceWsdlUrl;
    private OrganisaatioService organisaatioService;
    private String rootOrganisaatioOid;
    private AuthenticationService authenticationService;

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    public void tryToImportUserFromCustomOphAuthenticationService(UsernamePasswordCredentials cred) {
        try {
            IdentifiedHenkiloType henkilo = authenticationService.getIdentityByUsernameAndPassword(cred.getUsername(), cred.getPassword());
            tryToImport(henkilo, cred.getUsername(), cred.getPassword());
        } catch (Exception e) {
            log.warn("WARNING - problem with authentication backend, using only ldap.", e);
            return;
        }
    }

    public void tryToImportUserFromCustomOphAuthenticationService(SAMLCredentials cred) {
        try {
            IdentifiedHenkiloType henkiloType = authenticationService.getIdentityByAuthToken(cred.getToken());
            cred.setUserDetails(henkiloType);
            tryToImport(henkiloType, henkiloType.getKayttajatunnus(), cred.getToken());
        } catch (Exception e) {
            log.warn("WARNING - problem with authentication backend, using only ldap.", e);
            return;
        }
    }

    protected void tryToImport(IdentifiedHenkiloType henkilo, String username, String password) {
        log.info("CustomBindLdapAuthenticationHandler.preAuthenticate, henkilo: " + henkilo);
        LdapUser user = new LdapUser();
        user.setUid(username);
        user.setOid(henkilo.getOidHenkilo());
        user.setFirstName(henkilo.getEtunimet());
        user.setLastName(henkilo.getSukunimi());
        user.setEmail(henkilo.getEmail());
        user.setPassword(password);

        if(henkilo.isPassivoitu()) {
            ldapUserImporter.remove(user);
            throw new UserDisabledException("User " + username + " is disabled.");
        }

        try {
            log.info("CustomBindLdapAuthenticationHandler.preAuthenticate, user: " + user);

            // roles - mainly copypaste from TokenAutoLogin
            Set<String> roleStrings = new HashSet<String>();
            roleStrings.add("virkailija"); // TODO: temp keino saada kaikki käyttäjät virkailija-ryhmään, joka on jäsenenä virkailijan työpöytä -sitella, oikeasti ryhmä pitäisi olla jo backendissä
            if(henkilo.getAuthorizationData() != null && henkilo.getAuthorizationData().getAccessrights() !=null) {
                for(AccessRightType art : henkilo.getAuthorizationData().getAccessrights().getAccessRight()) {
                    log.info("AUTH ROW: OID[" + art.getOrganisaatioOid() + "] PALVELU[" + art.getPalvelu() + "] ROOLI[" + art.getRooli() + "] ORGANISAATIO[" + art.getOrganisaatioOid() + "]");
                    StringUtils.isNotEmpty(art.getOrganisaatioOid());
                    StringBuilder role = new StringBuilder(art.getPalvelu()).append("_").append(art.getRooli());

                    // prefix rolename with "APP_"
                    String ROLE_PREFIX = "APP_";

                    // add role PALVELU (esim jos userilla on backendissä rooli ORGANISAATIOHALLINTA_READ, lisätään hänelle myös ORGANISAATIOHALLINTA)
                    roleStrings.add(ROLE_PREFIX + art.getPalvelu());

                    // add role PALVELU_ROOLI
                    roleStrings.add(ROLE_PREFIX + role.toString());

                    // also add role PALVELU_ROOLI_ORGANISAATIO
                    roleStrings.add(ROLE_PREFIX + role.toString() + "_" + art.getOrganisaatioOid());
                }
            } else {
                log.info("HENKILO HAD NO AUTHORIZATION DATA: "+henkilo.getEmail()+"/"+henkilo.getOidHenkilo());
            }
            log.info("CustomBindLdapAuthenticationHandler.preAuthenticate, roleStrings: " + roleStrings);
            user.setGroups(roleStrings.toArray(new String[roleStrings.size()]));

            ldapUserImporter.save(user);
        } catch (UserDisabledException e) {
            throw e;
        } catch (Throwable e) {
            log.warn("failed to import user from backend to ldap, falling back to ldap, user: "+username, e);
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
}
