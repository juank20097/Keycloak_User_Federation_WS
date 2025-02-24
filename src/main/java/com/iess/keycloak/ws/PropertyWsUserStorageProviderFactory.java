package com.iess.keycloak.ws;

/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;

import java.util.List;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class PropertyWsUserStorageProviderFactory implements UserStorageProviderFactory<PropertyWsUserStorageProvider> {

    public static final String PROVIDER_NAME = "User-WS";

    protected static final List<ProviderConfigProperty> configMetadata;

    static {
        configMetadata = ProviderConfigurationBuilder.create()
                .property().name("apiUrl")
                .type(ProviderConfigProperty.STRING_TYPE)
                .label("API URL")
                .defaultValue("http://192.168.12.202:8080/login")
                .helpText("URL del servicio REST que devuelve los usuarios")
                .add().build();
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configMetadata;
    }
    
    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config) throws ComponentValidationException {
        String url = config.getConfig().getFirst("apiUrl");
        if (url == null) throw new ComponentValidationException("api_url property is Empty!");
    }

    @Override
    public String getId() {
        return PROVIDER_NAME;
    }

    @Override
    public PropertyWsUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        return new PropertyWsUserStorageProvider(session, model);
    }
}