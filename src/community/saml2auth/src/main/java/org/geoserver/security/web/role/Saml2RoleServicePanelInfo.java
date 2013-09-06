package org.geoserver.security.web.role;

import org.geoserver.security.config.Saml2RoleServiceConfig;
import org.geoserver.security.impl.GeoServerSaml2RoleService;

public class Saml2RoleServicePanelInfo extends RoleServicePanelInfo<Saml2RoleServiceConfig, Saml2RoleServicePanel> {
    
    private static final long serialVersionUID = 1L;

    public Saml2RoleServicePanelInfo() {
        setComponentClass(Saml2RoleServicePanel.class);
        setServiceClass(GeoServerSaml2RoleService.class);
        setServiceConfigClass(Saml2RoleServiceConfig.class);
        setPriority(0);
    }
}
