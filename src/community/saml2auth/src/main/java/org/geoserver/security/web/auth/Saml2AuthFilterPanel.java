package org.geoserver.security.web.auth;

import org.apache.wicket.model.IModel;
import org.geoserver.security.config.Saml2AuthenticationFilterConfig;
import org.geoserver.security.web.role.RoleServiceChoice;

public class Saml2AuthFilterPanel extends AuthenticationFilterPanel<Saml2AuthenticationFilterConfig> {

    private static final long serialVersionUID = 1L;

    public Saml2AuthFilterPanel(String id, IModel<Saml2AuthenticationFilterConfig> model) {
        super(id, model);
        add(new RoleServiceChoice("roleServiceName"));
    }
}
