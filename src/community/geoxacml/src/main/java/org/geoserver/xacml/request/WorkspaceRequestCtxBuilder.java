/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.xacml.request;

import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.security.AccessMode;
import org.geoserver.xacml.geoxacml.XACMLConstants;
import org.geoserver.xacml.role.XACMLRole;
import org.herasaf.xacml.core.context.impl.*;

/**
 * Builds a request for workspace access control
 * 
 * 
 * @author Christian Mueller
 * 
 */
public class WorkspaceRequestCtxBuilder extends RequestCtxBuilder {
    private String workspaceName = null;

    public String getWorkspaceName() {
        return workspaceName;
    }

    public WorkspaceRequestCtxBuilder(XACMLRole role, WorkspaceInfo workspace, AccessMode mode) {
        super(role, mode.toString());
        this.workspaceName = workspace.getName();
    }

    @Override
    public RequestType createRequest() {

        SubjectType subject = new SubjectType();
        ResourceType resource = new ResourceType();
        ActionType action = new ActionType();
        EnvironmentType environment = new EnvironmentType();
        addRole(subject);

        addGeoserverResource(resource);
        addResource(resource, XACMLConstants.WorkspaceURI, workspaceName);

        addAction(action);

        RequestType ctx = new RequestType();
        ctx.getSubjects().add(subject);
        ctx.getResources().add(resource);
        ctx.setAction(action);
        ctx.setEnvironment(environment);
        return ctx;
    }

}
