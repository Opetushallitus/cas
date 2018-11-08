/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas;

import fi.vm.sade.auth.clients.KayttooikeusRestClient;
import fi.vm.sade.auth.exception.EmailVerificationException;
import fi.vm.sade.auth.exception.NoStrongIdentificationException;
import fi.vm.sade.properties.OphProperties;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import static java.util.Collections.singletonMap;
import java.util.List;
import java.util.Optional;
import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.BasicCredentialMetaData;
import org.jasig.cas.authentication.CredentialMetaData;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.logout.LogoutManager;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.ticket.TicketFactory;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Transactional(readOnly = false, transactionManager = "ticketTransactionManager")
public class StrongIdentificationRequiringCentralAuthenticationServiceImpl extends CentralAuthenticationServiceImpl {
    @NotNull
    private KayttooikeusRestClient kayttooikeusClient;

    @NotNull
    private OphProperties ophProperties;

    @NotNull
    private boolean requireStrongIdentification;

    @NotNull
    private String casRequireStrongIdentificationListAsString;

    @NotNull
    private List<String> casRequireStrongIdentificationList;

    @NotNull
    private boolean casEmailVerificationEnabled;

    @NotNull
    private String casEmailVerificationListAsString;

    @NotNull
    private List<String> casEmailVerificationList;


    public static final String STRONG_IDENTIFICATION = "STRONG_IDENTIFICATION";
    public static final String EMAIL_VERIFICATION = "EMAIL_VERIFICATION";

    public StrongIdentificationRequiringCentralAuthenticationServiceImpl(
            final TicketRegistry ticketRegistry,
            final TicketFactory ticketFactory,
            final ServicesManager servicesManager,
            final LogoutManager logoutManager) {
        super(ticketRegistry, ticketFactory, servicesManager, logoutManager);
    }

    @Override
    public void checkStrongIdentificationHook(final Collection<? extends CredentialMetaData> credentials) throws AuthenticationException {
        Assert.notNull(credentials, "credentials cannot be null");

        Optional<String> credential = credentials.stream()
                .filter(BasicCredentialMetaData.class::isInstance)
                .map(BasicCredentialMetaData.class::cast)
                .filter(this::isUsernamePasswordCredential)
                .map(BasicCredentialMetaData::getId)
                .findFirst();
        // Do this only for UsernamePasswordCredentials. Service-provider-app wants to do this before creating authentication token.
        if (credential.isPresent()) {
            String username = credential.get();
            String redirectCodeUrl = this.ophProperties.url("kayttooikeus-service.cas.login.redirect.username", username);
            String redirectCode;

            // Where to redirect. null for no redirect
            try {
                redirectCode = this.kayttooikeusClient.get(redirectCodeUrl, String.class);
            } catch (IOException e) {
                throw new AuthenticationException(singletonMap(getClass().getName(), FailedLoginException.class));
            }

            if(STRONG_IDENTIFICATION.equals(redirectCode) && (this.requireStrongIdentification
                    || this.casRequireStrongIdentificationList.contains(credential.get()))) {
                throw new AuthenticationException(singletonMap(getClass().getName(), NoStrongIdentificationException.class));
            } else if(EMAIL_VERIFICATION.equals(redirectCode) && (this.casEmailVerificationEnabled
                    || this.casEmailVerificationList.contains(credential.get()))) {
                throw new AuthenticationException(singletonMap(getClass().getName(), EmailVerificationException.class));
            }
        }
    }

    private boolean isUsernamePasswordCredential(BasicCredentialMetaData metaData) {
        return UsernamePasswordCredential.class.isAssignableFrom(metaData.getCredentialClass());
    }

    public void setKayttooikeusClient(KayttooikeusRestClient kayttooikeusClient) {
        this.kayttooikeusClient = kayttooikeusClient;
    }

    public void setOphProperties(OphProperties ophProperties) {
        this.ophProperties = ophProperties;
    }

    public void setRequireStrongIdentification(boolean requireStrongIdentification) {
        this.requireStrongIdentification = requireStrongIdentification;
    }

    public String getCasRequireStrongIdentificationListAsString() {
        return casRequireStrongIdentificationListAsString;
    }

    public void setCasRequireStrongIdentificationListAsString(String casRequireStrongIdentificationList) {
        this.casRequireStrongIdentificationListAsString = casRequireStrongIdentificationList;
        this.casRequireStrongIdentificationList = !"".equals(casRequireStrongIdentificationList)
                ? Arrays.asList(casRequireStrongIdentificationList.split(","))
                : new ArrayList<String>();
    }

    public void setCasEmailVerificationEnabled(boolean casEmailVerificationEnabled) {
        this.casEmailVerificationEnabled = casEmailVerificationEnabled;
    }

    public void setCasEmailVerificationListAsString(String casEmailVerificationListAsString) {
        this.casEmailVerificationListAsString = casEmailVerificationListAsString;
        this.casEmailVerificationList = !"".equals(casEmailVerificationListAsString)
                ? Arrays.asList(casEmailVerificationListAsString.split(","))
                : new ArrayList<String>();
    }

    public String getCasEmailVerificationListAsString() {
        return casEmailVerificationListAsString;
    }
}
