/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.xacml.spring.security;

import java.util.ArrayList;
import java.util.Collection;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.access.SecurityMetadataSource;


/**
 * Spring Security ObjectDefinitonSource implementation for Services
 * 
 * @author Christian Mueller
 * 
 */
public class XACMLFilterSecurityMetadataSource implements SecurityMetadataSource {

    private static final Collection<ConfigAttribute> ConfigDef = new ArrayList<ConfigAttribute>();

    public final static XACMLFilterSecurityMetadataSource Singleton = new XACMLFilterSecurityMetadataSource();

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
