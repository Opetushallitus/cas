package fi.vm.sade.auth.ldap;

//*

import fi.vm.sade.authentication.service.AuthenticationService;
import fi.vm.sade.authentication.service.AuthenticationService_Service;
import fi.vm.sade.authentication.service.types.AccessRightType;
import fi.vm.sade.authentication.service.types.IdentifiedHenkiloType;
import fi.vm.sade.organisaatio.api.model.OrganisaatioService;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import org.apache.commons.lang.StringUtils;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

import javax.xml.namespace.QName;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//*/

/**
 * Extend BindLdapAuthenticationHandler to try to import user data from custom authenticationservice to ldap before
 * authenticating against ldap.
 *
 * @author Antti Salonen
 */
public class CustomBindLdapAuthenticationHandler extends org.jasig.cas.adaptors.ldap.BindLdapAuthenticationHandler {

    private LdapUserImporter ldapUserImporter;
    private String authenticationServiceWsdlUrl;
    private OrganisaatioService organisaatioService;
    private String rootOrganisaatioOid;

    @Override
    protected boolean preAuthenticate(Credentials credentials) {
        log.info("CustomBindLdapAuthenticationHandler.preAuthenticate, credentials: " + credentials.toString());
        UsernamePasswordCredentials cred = (UsernamePasswordCredentials) credentials;
        log.info("CustomBindLdapAuthenticationHandler.preAuthenticate, user: " + cred.getUsername() + ", pass: " + cred.getPassword());

        tryToImportOrganisaatios();

        tryToImportUserFromCustomOphAuthenticationService(cred);

        return true;
    }

    private void tryToImportOrganisaatios() {
/*
        System.out.println("CustomBindLdapAuthenticationHandler.tryToImportOrganisaatios, rootOrganisaatioOid: "+rootOrganisaatioOid);
        try {
            OrganisaatioDTO root = organisaatioService.findByOid(rootOrganisaatioOid);
            importOrganisaatioRecursive(root, new ArrayList<OrganisaatioDTO>());
            // TODO: hae organisaation perustiedot ja importtaa ldappiin
        } catch (Throwable e) {
            log.warn("failed to import organisaatios from backend to ldap", e);
        }
//*/
    }

    private void importOrganisaatioRecursive(OrganisaatioDTO organisaatio, List<OrganisaatioDTO> parents) {
        /*
        System.out.println("CustomBindLdapAuthenticationHandler.importOrganisaatioRecursive: "+organisaatio);

        // build parent oids
        List<String> path = new ArrayList<String>();
        path.add("organisaatios");
        for (OrganisaatioDTO parent : parents) {
            path.add(parent.getOid());
        }
        // import organisaatio to ldap
        Name dn = LdapUserImporter.buildDn("ou", organisaatio.getOid(), path.toArray(new String[path.size()]));
        Attributes attribs = ldapUserImporter.buildAttributes("top", "organizationalUnit");
        // TODO: other org attribs? what are needed?
        ldapUserImporter.save(dn, attribs, false);

        // process organisaatio's children
        parents = new ArrayList<OrganisaatioDTO>(parents); // remake new parents list for children
        parents.add(organisaatio);
        List<OrganisaatioDTO> children = organisaatioService.findChildrenTo(organisaatio.getOid());
        for (OrganisaatioDTO child : children) {
            importOrganisaatioRecursive(child, parents);
        }
        */
    }

    private void tryToImportUserFromCustomOphAuthenticationService(UsernamePasswordCredentials cred) {
//*
        AuthenticationService_Service ass = null;
        try {
            ass = new AuthenticationService_Service(new URL(authenticationServiceWsdlUrl), new QName("http://service.authentication.sade.vm.fi/", "AuthenticationService"));
        } catch (Exception e) {
            log.warn("WARNING - problem with authentication backend, using only ldap, error: " + e, e);
            return;
        }

        try {
            AuthenticationService as = ass.getAuthenticationServicePort();
            IdentifiedHenkiloType henkilo = as.getIdentityByUsernameAndPassword(cred.getUsername(), cred.getPassword());
            log.info("CustomBindLdapAuthenticationHandler.preAuthenticate, henkilo: " + henkilo);

            LdapUser user = new LdapUser();
            user.setUid(cred.getUsername());
            user.setOid(henkilo.getOidHenkilo());
            user.setFirstName(henkilo.getEtunimet());
            user.setLastName(henkilo.getSukunimi());
            //user.setEmail(henkilo.getEmail());
            user.setEmail(cred.getUsername());
            user.setPassword(cred.getPassword());
            log.info("CustomBindLdapAuthenticationHandler.preAuthenticate, user: " + user);

            // roles - mainly copypaste from TokenAutoLogin
            Set<String> roleStrings = new HashSet<String>();
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
                roleStrings.add("virkailija"); // TODO: temp keino saada kaikki käyttäjät virkailija-ryhmään, joka on jäsenenä virkailijan työpöytä -sitella, oikeasti ryhmä pitäisi olla jo backendissä
            } else {
                log.info("HENKILO HAD NO AUTHORIZATION DATA: "+henkilo.getEmail()+"/"+henkilo.getOidHenkilo());
            }
            log.info("CustomBindLdapAuthenticationHandler.preAuthenticate, roleStrings: " + roleStrings);
            user.setGroups(roleStrings.toArray(new String[roleStrings.size()]));

            ldapUserImporter.save(user);

            /* jos tarvitsee saada lista rooleista esim mock contextia varten
            log.info("CustomBindLdapAuthenticationHandler.preAuthenticate, roleStrings prefixed with 'ROLE_': ");
            List<String> temp = new ArrayList<String>(roleStrings);
            Collections.sort(temp);
            for (String roleString : temp) {
                System.out.print("ROLE_"+roleString+", ");
            }
            System.out.println();
            */

        } catch (Throwable e) {
            //throw new RuntimeException("failed to import user from backend to ldap, user: "+cred.getUsername()+", error: "+e, e);
            log.warn("failed to import user from backend to ldap, falling back to ldap, user: "+cred.getUsername(), e);
        }
//*/
    }

    public void setLdapUserImporter(LdapUserImporter ldapUserImporter) {
        this.ldapUserImporter = ldapUserImporter;
    }

    public void setOrganisaatioService(OrganisaatioService organisaatioService) {
        this.organisaatioService = organisaatioService;
    }

    public void setAuthenticationServiceWsdlUrl(String authenticationServiceWsdlUrl) {
        this.authenticationServiceWsdlUrl = authenticationServiceWsdlUrl;
    }

    public void setRootOrganisaatioOid(String rootOrganisaatioOid) {
        this.rootOrganisaatioOid = rootOrganisaatioOid;
    }
}
