package fi.vm.sade.auth.ldap;

import fi.vm.sade.authentication.service.AuthenticationService;
import fi.vm.sade.authentication.service.AuthenticationService_Service;
import fi.vm.sade.authentication.service.types.AccessRightType;
import fi.vm.sade.authentication.service.types.IdentifiedHenkiloType;
import org.apache.commons.lang.StringUtils;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

import javax.xml.namespace.QName;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Extend BindLdapAuthenticationHandler to try to import user data from custom authenticationservice to ldap before
 * authenticating against ldap.
 *
 * @author Antti Salonen
 */
public class CustomBindLdapAuthenticationHandler extends org.jasig.cas.adaptors.ldap.BindLdapAuthenticationHandler {

    private LdapUserImporter ldapUserImporter;
    private String authenticationServiceWsdlUrl;

    @Override
    protected boolean preAuthenticate(Credentials credentials) {
        log.info("CustomBindLdapAuthenticationHandler.preAuthenticate, credentials: " + credentials.toString());
        UsernamePasswordCredentials cred = (UsernamePasswordCredentials) credentials;
        log.info("CustomBindLdapAuthenticationHandler.preAuthenticate, user: " + cred.getUsername() + ", pass: " + cred.getPassword());

        tryToImportUserFromCustomOphAuthenticationService(cred);

        return true;
    }

    private void tryToImportUserFromCustomOphAuthenticationService(UsernamePasswordCredentials cred) {
        AuthenticationService_Service ass = null;
        try {
            ass = new AuthenticationService_Service(new URL(authenticationServiceWsdlUrl), new QName("http://service.authentication.sade.vm.fi/", "AuthenticationServiceService"));
        } catch (Exception e) {
            log.warn("WARNING - problem with authentication webservice, using only ldap, error: " + e);
            return;
        }

        AuthenticationService as = ass.getAuthenticationServicePort();
        IdentifiedHenkiloType henkilo = as.getIdentityByUsernameAndPassword(cred.getUsername(), cred.getPassword());
        log.info("CustomBindLdapAuthenticationHandler.preAuthenticate, henkilo: " + henkilo);

        LdapUser user = new LdapUser();
        user.setUid(cred.getUsername());
        user.setFirstName(henkilo.getEtunimet());
        user.setLastName(henkilo.getSukunimi());
        //user.setEmail(henkilo.getEmail()); todo:
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

                // add role PALVELU_ROOLI
                roleStrings.add(role.toString());

                // also add role PALVELU_ROOLI_ORGANISAATIO
                role.append("_").append(art.getOrganisaatioOid());
                roleStrings.add(role.toString());
            }
            // TODO: roolien poisto käyttäjiltä jos backendistä poistunut? käyttäjän poisto ldapista jos poistettu backendistä? virheenkäsittely
            roleStrings.add("virkailija"); // TODO: temp keino saada kaikki käyttäjät virkailija-ryhmään, joka on jäsenenä virkailijan työpöytä -sitella, oikeasti ryhmä pitäisi olla jo backendissä
        } else {
            log.info("HENKILO HAD NO AUTHORIZATION DATA" );
        }
        log.info("CustomBindLdapAuthenticationHandler.preAuthenticate, roleStrings: " + roleStrings);
        user.setGroups(roleStrings.toArray(new String[roleStrings.size()]));

        ldapUserImporter.save(user);
    }

    public void setLdapUserImporter(LdapUserImporter ldapUserImporter) {
        this.ldapUserImporter = ldapUserImporter;
    }

    public void setAuthenticationServiceWsdlUrl(String authenticationServiceWsdlUrl) {
        this.authenticationServiceWsdlUrl = authenticationServiceWsdlUrl;
    }

}
