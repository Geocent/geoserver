/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.xacml.security;

import org.geoserver.security.impl.GeoServerUserDao;
import org.geoserver.xacml.role.XACMLRoleAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

public class XACMLGeoserverUserDao extends GeoServerUserDao {

    private final XACMLRoleAuthority authority;

    public XACMLGeoserverUserDao(XACMLRoleAuthority authority){
        this.authority = authority;
    }

    @Override
    protected User createUserObject(String username, String password, boolean isEnabled,
            List<GrantedAuthority> authorities) {
        User user = super.createUserObject(username, password, isEnabled, authorities);
        authority.transformUserDetails(user);
        return user;
    }

}
