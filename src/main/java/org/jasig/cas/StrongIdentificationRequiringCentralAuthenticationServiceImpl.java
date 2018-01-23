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
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.util.Assert;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    @Override
    public void checkStrongIdentificationHook(final Credentials credentials) throws NoStrongIdentificationException {
        Assert.notNull(credentials, "credentials cannot be null");

            // Do this only for UsernamePasswordCredentials. Service-provider-app wants to do this before creating authentication token.
            if (this.requireStrongIdentification && credentials instanceof UsernamePasswordCredentials
                    && (casRequireStrongIdentificationList.isEmpty()
                    || casRequireStrongIdentificationList.contains(((UsernamePasswordCredentials) credentials).getUsername()))) {
                String username = ((UsernamePasswordCredentials) credentials).getUsername();
                String vahvaTunnistusUrl = this.ophProperties.url("kayttooikeus-service.cas.vahva-tunnistus-username", username);
                Boolean vahvastiTunnistettu;
                try {
                    vahvastiTunnistettu = this.kayttooikeusClient.get(vahvaTunnistusUrl, Boolean.class);
                } catch (IOException e) {
                    throw new NoStrongIdentificationException("error", "Could not determine if user is strongly identified");
                }
                if (BooleanUtils.isFalse(vahvastiTunnistettu)) {
                    // type (3rd parameter) is important since it decides webflow route. Default is "error".
                    throw new NoStrongIdentificationException("noStrongIdentification", "Need strong identificatin", "noStrongIdentification");
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
