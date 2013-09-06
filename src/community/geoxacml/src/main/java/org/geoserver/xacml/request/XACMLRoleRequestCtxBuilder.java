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
import org.herasaf.xacml.core.dataTypeAttribute.impl.StringDataTypeAttribute;

/**
 * Builds a request for testing access of geoserver to the catalog (always Permit) The idea here is
 * to pass back the {@link CatalogMode} in an XACML obligation.
 * 
 * @author Christian Mueller
 * 
 */
public class XACMLRoleRequestCtxBuilder extends RequestCtxBuilder {
    public final static XACMLRole RoleEnablementRole = new XACMLRole(
            XACMLConstants.RoleEnablementRole);

    XACMLRole targetRole = null;

    String userName = null;

    public XACMLRoleRequestCtxBuilder(XACMLRole targetRole, String userName) {
        super(RoleEnablementRole, AccessMode.READ.toString());
        this.targetRole = targetRole;
        this.userName = userName;
    }

    @Override
    public RequestType createRequest() {

        SubjectType subject = new SubjectType();
        ResourceType resource = new ResourceType();
        ActionType action = new ActionType();
        EnvironmentType environment = new EnvironmentType();
        addRole(subject);

        addGeoserverResource(resource);
        addResource(resource, XACMLConstants.RoleEnablemetnResourceURI, targetRole.getAuthority());


        addAction(action);

        if (userName != null) {
            AttributeType attribute = new AttributeType();
            AttributeValueType attributeValue = new AttributeValueType();

            attribute.setAttributeId(XACMLConstants.UserEnvironmentId);
            attribute.setDataType(new StringDataTypeAttribute());
            attribute.getAttributeValues().add(attributeValue);
            attributeValue.getContent().add(userName);

            environment.getAttributes().add(attribute);
        }


        RequestType ctx = new RequestType();
        ctx.getSubjects().add(subject);
        ctx.getResources().add(resource);
        ctx.setAction(action);
        ctx.setEnvironment(environment);
        return ctx;
    }

}
