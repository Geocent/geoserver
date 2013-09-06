/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.xacml.spring.security;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Spring Security ObjectDefinitonSource implementation for Services
 * 
 * @author Christian Mueller
 * 
 */
public class XACMLFilterSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {

    private static final Collection<ConfigAttribute> ConfigDef = new ArrayList<ConfigAttribute>();

    static {
        ConfigDef.add(new SecurityConfig("xacml"));
    }


    @Override
    public Collection<ConfigAttribute> getAttributes(Object obj) throws IllegalArgumentException {
        return ConfigDef;
    }


    @Override
    public boolean supports(Class aClass) {
        return true;
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return ConfigDef;
    }

}
