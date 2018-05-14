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
import org.apache.commons.lang.BooleanUtils;
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
        if (credential.isPresent()
                && (this.requireStrongIdentification
                || this.casRequireStrongIdentificationList.contains(credential.get()))) {
            String username = credential.get();
            String vahvaTunnistusUrl = this.ophProperties.url("kayttooikeus-service.cas.vahva-tunnistus-username", username);
            Boolean vahvastiTunnistettu;
            try {
                vahvastiTunnistettu = this.kayttooikeusClient.get(vahvaTunnistusUrl, Boolean.class);
            } catch (IOException e) {
                throw new AuthenticationException(singletonMap(getClass().getName(), FailedLoginException.class));
            }
            if (BooleanUtils.isFalse(vahvastiTunnistettu)) {
                throw new AuthenticationException(singletonMap(getClass().getName(), NoStrongIdentificationException.class));
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
}
