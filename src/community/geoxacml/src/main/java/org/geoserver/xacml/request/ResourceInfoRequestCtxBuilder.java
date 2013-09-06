/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.xacml.request;

import org.geoserver.catalog.ResourceInfo;
import org.geoserver.security.AccessMode;
import org.geoserver.xacml.geoxacml.XACMLConstants;
import org.geoserver.xacml.role.XACMLRole;
import org.herasaf.xacml.core.context.impl.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Builds a request for layer info access control
 * 
 * 
 * @author Christian Mueller
 * 
 */
public class ResourceInfoRequestCtxBuilder extends RequestCtxBuilder {
    private String resourceName = null;

    private String workspaceName = null;

    public String getResouceName() {
        return resourceName;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public ResourceInfoRequestCtxBuilder(XACMLRole role, ResourceInfo resourceInfo, AccessMode mode) {
        super(role, mode.toString());
        this.resourceName = resourceInfo.getName();
        if (resourceInfo.getNamespace() != null) {
            this.workspaceName = resourceInfo.getNamespace().getName();
            if (this.workspaceName == null)
                this.workspaceName = resourceInfo.getNamespace().getURI();
        } else {
            this.workspaceName = resourceInfo.getStore().getWorkspace().getName();
        }
    }

    @Override
    public RequestType createRequest() {

        SubjectType subject = new SubjectType();
        addRole(subject);

        ResourceType resource = new ResourceType();
        addGeoserverResource(resource);
        addOWSService(resource);
        addResource(resource, XACMLConstants.WorkspaceURI, workspaceName);
        addResource(resource, XACMLConstants.GeoServerResouceURI, resourceName);
        addBbox(resource);

        ActionType action = new ActionType();
        addAction(action);

        Set<AttributeType> environment = new HashSet<AttributeType>(1);

        RequestType ctx = new RequestType();
        ctx.getSubjects().add(subject);
        ctx.getResources().add(resource);
        ctx.setAction(action);
        ctx.setEnvironment(new EnvironmentType());
        return ctx;
    }

}
