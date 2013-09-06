/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.xacml.request;

import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.security.AccessMode;
import org.geoserver.xacml.role.XACMLRole;

import java.util.Map;

/**
 * Interface for RequestCtxBuilders User can do an own implementation and configure in
 * applicationContext.xml
 * 
 * @author Christian Mueller
 * 
 */
public interface RequestCtxBuilderFactory {
    public RequestCtxBuilder getCatalogRequestCtxBuilder();

    public RequestCtxBuilder getXACMLRoleRequestCtxBuilder(XACMLRole targetRole, String userName);

    public RequestCtxBuilder getWorkspaceRequestCtxBuilder(XACMLRole role, WorkspaceInfo info,
            AccessMode mode);

    public RequestCtxBuilder getURLMatchRequestCtxBuilder(XACMLRole role, String urlString,
            String method, Map<String, Object> httpParams,String remoteIP,String remoteHost);

    public RequestCtxBuilder getResourceInfoRequestCtxBuilder(XACMLRole role,
            ResourceInfo resourceInfo, AccessMode mode);
}
