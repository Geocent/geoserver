package org.geoserver.security.web.role;

import org.apache.wicket.model.IModel;
import org.geoserver.security.config.Saml2RoleServiceConfig;

public class Saml2RoleServicePanel extends RoleServicePanel<Saml2RoleServiceConfig> {

    private static final long serialVersionUID = 1L;

    public Saml2RoleServicePanel(String id, IModel<Saml2RoleServiceConfig> model) {
        super(id, model);
    }
}
