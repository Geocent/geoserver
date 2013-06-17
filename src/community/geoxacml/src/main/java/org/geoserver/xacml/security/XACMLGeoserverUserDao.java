/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.xacml.security;

import java.util.List;
import org.geoserver.security.impl.GeoServerUserDao;
import org.geoserver.xacml.geoxacml.GeoXACMLConfig;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class XACMLGeoserverUserDao extends GeoServerUserDao {

    @Override
    protected User createUserObject(String username, String password, boolean isEnabled,
            List<GrantedAuthority> authorities) {
        User user = super.createUserObject(username, password, isEnabled, authorities);
        GeoXACMLConfig.getXACMLRoleAuthority().transformUserDetails(user);
        return user;
    }

}
