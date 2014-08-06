package fi.vm.sade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fi.vm.sade.log.client.Logger;
import fi.vm.sade.log.model.Tapahtuma;

@Component
public class CASAuditLogger {

    @Autowired
    private Logger casLogger;
    
    private final String SERVICE_NAME = "cas";
    
    public void auditLoginUsernamePassword(String currentUser) {
        Tapahtuma tapahtuma = Tapahtuma.createCREATE(SERVICE_NAME, currentUser, "Login/CreateST", "Username/Password");
        casLogger.log(tapahtuma);
    }
    
    public void auditLoginHaka(String currentUser) {
        Tapahtuma tapahtuma = Tapahtuma.createCREATE(SERVICE_NAME, currentUser, "Login/CreateST", "SAML2/Haka");
        casLogger.log(tapahtuma);
    }
    
    public void auditLoginVetuma(String currentUser) {
        Tapahtuma tapahtuma = Tapahtuma.createCREATE(SERVICE_NAME, currentUser, "Login/CreateST", "SAML2/Vetuma");
        casLogger.log(tapahtuma);
    }
    
    public void auditLogout(String currentUser, String serviceTicket) {
        Tapahtuma tapahtuma = Tapahtuma.createDELETE(SERVICE_NAME, currentUser, "Logout/DestroyST", serviceTicket);
        casLogger.log(tapahtuma);
    }
}
