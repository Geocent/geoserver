package org.geoserver.security.web.auth;

import org.geoserver.security.config.Saml2AuthenticationFilterConfig;
import org.geoserver.security.filter.GeoServerSaml2AuthenticationFilter;

public class Saml2AuthFilterPanelInfo extends AuthenticationFilterPanelInfo<Saml2AuthenticationFilterConfig, Saml2AuthFilterPanel> {

    private static final long serialVersionUID = 1L;

    public Saml2AuthFilterPanelInfo() {
        setComponentClass(Saml2AuthFilterPanel.class);
        setServiceClass(GeoServerSaml2AuthenticationFilter.class);
        setServiceConfigClass(Saml2AuthenticationFilterConfig.class);
        setPriority(0);
    }
}
