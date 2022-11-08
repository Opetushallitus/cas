package fi.vm.sade.saml.action;

import lombok.*;
import org.apereo.cas.authentication.credential.AbstractCredential;

import java.io.Serial;

@ToString
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SAMLCredentials extends AbstractCredential {

    @Serial
    private static final long serialVersionUID = -3570177055782538061L;

    private String token;

    @Override
    public String getId() {
        return token;
    }
}
