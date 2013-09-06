package org.geoserver.security.config;

public class Saml2RoleServiceConfig extends BaseSecurityNamedServiceConfig
        implements SecurityRoleServiceConfig {
    private static final long serialVersionUID = 1L;
    protected String adminRoleName;
    protected String groupAdminRoleName;

    public Saml2RoleServiceConfig() {
    }

    public Saml2RoleServiceConfig(Saml2RoleServiceConfig other) {
        super(other);
        this.adminRoleName = other.getAdminRoleName();
        this.groupAdminRoleName = other.getGroupAdminRoleName();
    }

    @Override
    public String getAdminRoleName() {
        return adminRoleName;
    }

    @Override
    public void setAdminRoleName(String adminRoleName) {
        this.adminRoleName = adminRoleName;
    }

    public String getGroupAdminRoleName() {
        return groupAdminRoleName;
    }

    public void setGroupAdminRoleName(String groupAdminRoleName) {
        this.groupAdminRoleName = groupAdminRoleName;
    }

}
