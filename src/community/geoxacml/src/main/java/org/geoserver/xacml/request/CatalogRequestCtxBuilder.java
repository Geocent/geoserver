/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.xacml.request;

import org.geoserver.security.AccessMode;
import org.geoserver.security.CatalogMode;
import org.geoserver.xacml.geoxacml.XACMLConstants;
import org.geoserver.xacml.role.XACMLRole;
import org.herasaf.xacml.core.context.impl.*;

/**
 * Builds a request for testing access of geoserver to the catalog (always Permit) The idea here is
 * to pass back the {@link CatalogMode} in an XACML obligation.
 * 
 * @author Christian Mueller
 * 
 */
public class CatalogRequestCtxBuilder extends RequestCtxBuilder {
    public final static XACMLRole GeoServerRole = new XACMLRole(XACMLConstants.GeoServerRole);

    public CatalogRequestCtxBuilder() {
        super(GeoServerRole, AccessMode.READ.toString());
    }

    @Override
    public RequestType createRequest() {

        SubjectType subject = new SubjectType();
        addRole(subject);

        ResourceType resource = new ResourceType();
        addGeoserverResource(resource);
        addResource(resource, XACMLConstants.CatalogResourceURI, XACMLConstants.CatalogResouceName);

        ActionType action = new ActionType();
        addAction(action);

        RequestType ctx = new RequestType();
        ctx.getSubjects().add(subject);
        ctx.getResources().add(resource);
        ctx.setAction(action);
        ctx.setEnvironment(new EnvironmentType());
        return ctx;

    }

}
