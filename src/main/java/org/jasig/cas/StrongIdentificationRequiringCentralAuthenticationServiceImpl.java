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
import org.apache.commons.lang.BooleanUtils;
import org.springframework.util.Assert;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import static java.util.Collections.singletonMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.security.auth.login.FailedLoginException;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.AuthenticationManager;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.logout.LogoutManager;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.util.UniqueTicketIdGenerator;

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

    public StrongIdentificationRequiringCentralAuthenticationServiceImpl(final TicketRegistry ticketRegistry,
                                            final AuthenticationManager authenticationManager,
                                            final UniqueTicketIdGenerator ticketGrantingTicketUniqueTicketIdGenerator,
                                            final Map<String, UniqueTicketIdGenerator> uniqueTicketIdGeneratorsForService,
                                            final ExpirationPolicy ticketGrantingTicketExpirationPolicy,
                                            final ExpirationPolicy serviceTicketExpirationPolicy,
                                            final ServicesManager servicesManager,
                                            final LogoutManager logoutManager) {
        super(ticketRegistry, authenticationManager,
                ticketGrantingTicketUniqueTicketIdGenerator,
                uniqueTicketIdGeneratorsForService,
                ticketGrantingTicketExpirationPolicy,
                serviceTicketExpirationPolicy, servicesManager, logoutManager);
    }

    @Override
    public void checkStrongIdentificationHook(final Set<? extends Credential> credentials) throws AuthenticationException {
        Assert.notNull(credentials, "credentials cannot be null");

        Optional<UsernamePasswordCredential> credential = credentials.stream()
                .filter(UsernamePasswordCredential.class::isInstance)
                .map(UsernamePasswordCredential.class::cast)
                .findFirst();
        // Do this only for UsernamePasswordCredentials. Service-provider-app wants to do this before creating authentication token.
        if (credential.isPresent()
                && (this.requireStrongIdentification
                || this.casRequireStrongIdentificationList.contains(credential.get().getUsername()))) {
            String username = credential.get().getUsername();
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
