package fi.vm.sade.auth.ldap;

//*

import fi.vm.sade.AuthenticationUtil;
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

    private AuthenticationUtil authenticationUtil;

    @Override
    protected boolean preAuthenticate(Credentials credentials) {
        log.info("CustomBindLdapAuthenticationHandler.preAuthenticate, credentials: " + credentials.toString());
        UsernamePasswordCredentials cred = (UsernamePasswordCredentials) credentials;
        log.info("CustomBindLdapAuthenticationHandler.preAuthenticate, user: " + cred.getUsername() + ", pass: " + cred.getPassword());

        tryToImportOrganisaatios();

        authenticationUtil.tryToImportUserFromCustomOphAuthenticationService(cred);

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

    public AuthenticationUtil getAuthenticationUtil() {
        return authenticationUtil;
    }

    public void setAuthenticationUtil(AuthenticationUtil authenticationUtil) {
        this.authenticationUtil = authenticationUtil;
    }
}
