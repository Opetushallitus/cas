package fi.vm.sade;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.vm.sade.auth.ldap.LdapUser;
import fi.vm.sade.auth.ldap.LdapUserImporter;
import fi.vm.sade.auth.ldap.exception.UserDisabledException;
import fi.vm.sade.authentication.service.AuthenticationService;
import fi.vm.sade.authentication.service.CustomAttributeService;
import fi.vm.sade.authentication.service.types.AccessRightType;
import fi.vm.sade.authentication.service.types.IdentifiedHenkiloType;
import fi.vm.sade.authentication.service.types.dto.CustomUserRoleType;
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

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    public boolean tryAuthenticationWithCustomOphAuthenticationService(UsernamePasswordCredentials cred) {
        try {
            IdentifiedHenkiloType henkilo = authenticationService.getIdentityByUsernameAndPassword(cred.getUsername(),
                    cred.getPassword());

            // service vastasi, mutta käyttäjää ei löytynyt.
            if (henkilo == null) {
                log.info("User not found.");
                return false;
            }
        } catch (Exception e) {
            log.warn("WARNING - problem with authentication backend, using only ldap.");//, e);
        }
        return true;
    }
    
    public boolean tryToImportUserFromCustomOphAuthenticationService(UsernamePasswordCredentials cred) {
        
        if (useAuthenticationService) {
            log.error("DEBUG::Using Authentication Service!");
            try {
                IdentifiedHenkiloType henkilo = authenticationService.getIdentityByUsernameAndPassword(cred.getUsername(),
                        cred.getPassword());

                // service vastasi, mutta käyttäjää ei löytynyt.
                if (henkilo == null) {
                    log.info("User not found.");
                    return false;
                }

                tryToImport(henkilo, cred.getUsername(), cred.getPassword());
            } catch (Exception e) {
                log.warn("WARNING - problem with authentication backend, using only ldap.");//, e);
            }
        }
        return true;
    }

    public void tryToImportUserFromCustomOphAuthenticationService(SAMLCredentials cred) {
        try {
            IdentifiedHenkiloType henkiloType = authenticationService.getIdentityByAuthToken(cred.getToken());
            cred.setUserDetails(henkiloType);
            tryToImport(henkiloType, henkiloType.getKayttajatiedot().getUsername(), cred.getToken());
        } catch (Exception e) {
            log.warn("WARNING - problem with authentication backend, using only ldap.", e);
            return;
        }
    }

    protected void tryToImport(IdentifiedHenkiloType henkilo, String username, String password) {
        log.info("CustomBindLdapAuthenticationHandler.preAuthenticate, henkilo: {}", henkilo.getIdentifier());
        LdapUser user = new LdapUser();
        user.setUid(username);
        user.setOid(henkilo.getOidHenkilo());
        user.setFirstName(henkilo.getEtunimet());
        user.setLastName(henkilo.getSukunimi());
        user.setLang(henkilo.getAsiointiKieli() != null ? henkilo.getAsiointiKieli().getKieliKoodi() : null);

        // TODO: Quick fix: LDAP vaatii, että mail-fieldissä on jotain.
        if (henkilo.getEmail() == null || StringUtils.isBlank(henkilo.getEmail())) {
            log.warn("User {} does not have email address at all", username);
            user.setEmail(username + "@oph.local");
        } else {
            user.setEmail(henkilo.getEmail());
        }
        user.setPassword(password);

        if (henkilo.isPassivoitu()) {
            ldapUserImporter.remove(user);
            throw new UserDisabledException("User " + username + " is disabled.");
        }

        List<CustomUserRoleType> roleTypes = new ArrayList<CustomUserRoleType>();

        try {
            long start = System.currentTimeMillis();
            System.out.println("Roles START--");
            roleTypes.addAll(customAttributeService.listCustomUserRole(henkilo.getOidHenkilo()));
            long took = System.currentTimeMillis() - start;
            System.out.println("Roles DONE in " +took);
        } catch (Exception e) {
            log.warn("Could not get user custom attributes.", e);
        }

        try {
            log.debug("CustomBindLdapAuthenticationHandler.preAuthenticate, user: {}", username);

            // roles - mainly copypaste from TokenAutoLogin
            Set<String> roleStrings = new HashSet<String>();
            // Lisätään kaikki custom roolit, kuten VIRKAILIJA,
            // STRONG_AUTHENTICATED...
            for (CustomUserRoleType role : roleTypes) {
                log.info("Adding role " + role.getRooli() + " to user " + username);
                roleStrings.add(role.getRooli());
            }

            // add also user's language as LANG_[lang] -role
            if (henkilo.getAsiointiKieli() != null) {
                roleStrings.add("LANG_" + henkilo.getAsiointiKieli().getKieliKoodi());
            }

            // roles
            if (henkilo.getAuthorizationData() != null && henkilo.getAuthorizationData().getAccessrights() != null) {
                for (AccessRightType art : henkilo.getAuthorizationData().getAccessrights().getAccessRight()) {
                    log.info("AUTH ROW: OID[" + art.getOrganisaatioOid() + "] PALVELU[" + art.getPalvelu() + "] ROOLI["
                            + art.getRooli() + "] ORGANISAATIO[" + art.getOrganisaatioOid() + "]");
                    StringUtils.isNotEmpty(art.getOrganisaatioOid());
                    StringBuilder role = new StringBuilder(art.getPalvelu()).append("_").append(art.getRooli());

                    // prefix rolename with "APP_"
                    String ROLE_PREFIX = "APP_";

                    // add role PALVELU (esim jos userilla on backendissä rooli
                    // ORGANISAATIOHALLINTA_READ, lisätään hänelle myös
                    // ORGANISAATIOHALLINTA)
                    roleStrings.add(ROLE_PREFIX + art.getPalvelu());

                    // add role PALVELU_ROOLI
                    roleStrings.add(ROLE_PREFIX + role.toString());

                    // also add role PALVELU_ROOLI_ORGANISAATIO
                    roleStrings.add(ROLE_PREFIX + role.toString() + "_" + art.getOrganisaatioOid());
                }
            } else {
                log.info("HENKILO HAD NO AUTHORIZATION DATA: {}/{}",
                        new Object[] { henkilo.getEmail(), henkilo.getOidHenkilo() });
            }
            log.info("CustomBindLdapAuthenticationHandler.preAuthenticate, roleStrings: " + roleStrings);
            user.setGroups(roleStrings.toArray(new String[roleStrings.size()]));

            ldapUserImporter.save(user);
        } catch (UserDisabledException e) {
            throw e;
        } catch (Throwable e) {
            log.warn("failed to import user from backend to ldap, falling back to ldap, user: {} {}", new Object[] {
                    username, e });
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
}

