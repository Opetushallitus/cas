package fi.vm.sade.saml.action;

import lombok.*;
import org.apereo.cas.authentication.credential.AbstractCredential;

import java.io.Serial;

/** Practically identical to {@link org.apereo.cas.authentication.credential.OneTimeTokenCredential} */
@ToString
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SAMLCredentials extends AbstractCredential {
    @Serial
    private static final long serialVersionUID = -7570600701132111037L;

    private String token;

    @Override
    public String getId() {
        return token;
    }
}
