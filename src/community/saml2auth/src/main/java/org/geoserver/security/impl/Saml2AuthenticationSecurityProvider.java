package org.geoserver.security.impl;

import java.io.IOException;

import org.geoserver.config.util.XStreamPersister;
import org.geoserver.security.GeoServerRoleService;
import org.geoserver.security.GeoServerSecurityManager;
import org.geoserver.security.GeoServerSecurityProvider;
import org.geoserver.security.config.Saml2RoleServiceConfig;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.validation.SecurityConfigValidator;

public class Saml2AuthenticationSecurityProvider extends GeoServerSecurityProvider {
    
    @Override
    public void configure(XStreamPersister xp){
        super.configure(xp);
        xp.getXStream().alias("saml2RoleService", Saml2RoleServiceConfig.class);
    }
    
    @Override
    public Class<? extends GeoServerRoleService> getRoleServiceClass() {
        return GeoServerSaml2RoleService.class;
    }

    @Override
    public GeoServerRoleService createRoleService(SecurityNamedServiceConfig config)
            throws IOException {
        return new GeoServerSaml2RoleService();
    }

    @Override
    public SecurityConfigValidator createConfigurationValidator(GeoServerSecurityManager securityManager) {
        return new SecurityConfigValidator(securityManager); 
    }

}
