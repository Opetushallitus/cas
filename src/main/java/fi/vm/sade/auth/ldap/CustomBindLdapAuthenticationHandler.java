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
 * Extend BindLdapAuthenticationHandler to do custom things pre/post authentication
 *
 * @author Antti Salonen
 */
public class CustomBindLdapAuthenticationHandler extends org.jasig.cas.adaptors.ldap.BindLdapAuthenticationHandler {

    private LdapUserImporter ldapUserImporter;



    @Override
    protected boolean preAuthenticate(Credentials credentials) {

        System.out.println("CustomBindLdapAuthenticationHandler.preAuthenticate, credentials: "+credentials.toString());
        UsernamePasswordCredentials cred = (UsernamePasswordCredentials) credentials;
        System.out.println("CustomBindLdapAuthenticationHandler.preAuthenticate, user: "+cred.getUsername()+", pass: "+cred.getPassword());

//        try {
            importUserFromAuthenticationService(cred);

            return super.preAuthenticate(credentials);
//        } catch (Exception e) { // TODO: virheenkäsittely
//            System.err.println("ERROR importing user "+cred.getUsername()+" from ldap: "+e);
//            e.printStackTrace();
//            return true;
//        }
    }

    private void importUserFromAuthenticationService(UsernamePasswordCredentials cred) {
        // TODO: wsdl, soutit
        AuthenticationService_Service ass = null;
        try {
            ass = new AuthenticationService_Service(new URL("http://localhost:8181/cxf/authenticationService?wsdl"), new QName("http://service.authentication.sade.vm.fi/", "AuthenticationServiceService"));
        } catch (Exception e) {
            System.err.println("WARNING - problem with authentication webservice, using only ldap, error: "+e);
            return;
        }
        AuthenticationService as = ass.getAuthenticationServicePort();
        IdentifiedHenkiloType henkilo = as.getIdentityByUsernameAndPassword(cred.getUsername(), cred.getPassword());
        System.out.println("CustomBindLdapAuthenticationHandler.preAuthenticate, henkilo: "+henkilo);

        User user = new User();
        user.setUserName(cred.getUsername());
        user.setFirstName(henkilo.getEtunimet());
        user.setLastName(henkilo.getSukunimi());
        //user.setEmail(henkilo.getEmail()); todo:
        user.setEmail(cred.getUsername());
        user.setPassword(cred.getPassword());
        System.out.println("CustomBindLdapAuthenticationHandler.preAuthenticate, user: "+user);

        // roles - mainly copypaste from TokenAutoLogin
        Set<String> roleStrings = new HashSet<String>();
        if(henkilo.getAuthorizationData() != null && henkilo.getAuthorizationData().getAccessrights() !=null) {
            for(AccessRightType art : henkilo.getAuthorizationData().getAccessrights().getAccessRight()) {
                System.out.println("AUTH ROW: OID["+  art.getOrganisaatioOid() +"] PALVELU[" + art.getPalvelu() +"] ROOLI[" + art.getRooli()+ "] ORGANISAATIO["+art.getOrganisaatioOid()+"]");
                StringUtils.isNotEmpty(art.getOrganisaatioOid());
                StringBuilder role = new StringBuilder(art.getPalvelu()).append("_").append(art.getRooli());

                // add role PALVELU_ROOLI
                roleStrings.add(role.toString());

                // also add role PALVELU_ROOLI_ORGANISAATIO
                role.append("_").append(art.getOrganisaatioOid());
                roleStrings.add(role.toString());
            }
            // TODO: roolien poisto käyttäjiltä jos backendistä poistunut?
            roleStrings.add("virkailija"); // TODO: temp keino saada kaikki käyttäjät virkailija-ryhmään, joka on jäsenenä virkailijan työpöytä -sitella, oikeasti ryhmä pitäisi olla jo backendissä
        } else {
            log.info("HENKILO HAD NO AUTHORIZATION DATA" );
        }
        System.out.println("CustomBindLdapAuthenticationHandler.preAuthenticate, roleStrings: "+roleStrings);
        user.setGroups(roleStrings.toArray(new String[roleStrings.size()]));

        ldapUserImporter.save(user);
    }

    public void setLdapUserImporter(LdapUserImporter ldapUserImporter) {
        this.ldapUserImporter = ldapUserImporter;
    }

}
