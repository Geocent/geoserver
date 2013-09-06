package org.geoserver.xacml.security;

import org.geoserver.security.config.SecurityFilterConfig;

import java.io.Serializable;

public class XACMLSecurityInterceptorFilterConfig extends SecurityFilterConfig implements Serializable {

    private static final long serialVersionUID = 1L;
    private boolean allowIfAllAbstainDecisions;
    private String securityMetadataSource;

    public boolean isAllowIfAllAbstainDecisions() {
        return allowIfAllAbstainDecisions;
    }
    public void setAllowIfAllAbstainDecisions(boolean allowIfAllAbstainDecisions) {
        this.allowIfAllAbstainDecisions = allowIfAllAbstainDecisions;
    }
}
