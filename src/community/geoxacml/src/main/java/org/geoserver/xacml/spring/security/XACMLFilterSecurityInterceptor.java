/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.xacml.spring.security;

import org.springframework.security.access.SecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;


/**
 * Url based authorization
 * 
 * 
 * @author Christian Mueller
 * 
 */
public class XACMLFilterSecurityInterceptor extends FilterSecurityInterceptor {

    private XACMLFilterSecurityMetadataSource source = new XACMLFilterSecurityMetadataSource();

    @Override
    public SecurityMetadataSource obtainSecurityMetadataSource() {
        return source;
    }

}
