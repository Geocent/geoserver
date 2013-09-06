/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.xacml.request;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import org.geoserver.ows.Dispatcher;
import org.geoserver.xacml.geoxacml.XACMLConstants;
import org.geoserver.xacml.role.XACMLRole;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.xacml.geoxacml.attr.GeometryDataTypeAttribute;
import org.herasaf.xacml.core.context.impl.*;
import org.herasaf.xacml.core.dataTypeAttribute.impl.StringDataTypeAttribute;
import org.vfny.geoserver.Request;

import java.net.URI;
import java.util.Map;

/**
 * Base class for geoxacml request context builders The class inheritance structure is mirrored from
 * {@link Request}
 * 
 * 
 * @author Christian Mueller
 * 
 */
public abstract class RequestCtxBuilder {

    private XACMLRole role;

    private String action;

    public XACMLRole getRole() {
        return role;
    }

    protected RequestCtxBuilder(XACMLRole role, String action) {
        this.role = role;
        this.action = action;
    }

    protected void addRole(SubjectType subject) {

        AttributeValueType roleAttributeValue = new AttributeValueType();
        roleAttributeValue.getContent().add(role.getAuthority());

        AttributeType roleAttribute = new AttributeType();
        roleAttribute.setDataType(new StringDataTypeAttribute());
                roleAttribute.setAttributeId(XACMLConstants.RoleAttributeId);
        roleAttribute.getAttributeValues().add(roleAttributeValue);


        subject.getAttributes().add(roleAttribute);

        for (AttributeType attr : role.getAttributes()) {
            subject.getAttributes().add(attr);
        }

    }

    protected void addAction(ActionType action) {

        AttributeType attribute = new AttributeType();
        attribute.setAttributeId(XACMLConstants.ActionAttributeId);
        attribute.setDataType(new StringDataTypeAttribute());

        AttributeValueType attributeValue = new AttributeValueType();
        attributeValue.getContent().add(this.action);

        attribute.getAttributeValues().add(attributeValue);
        action.getAttributes().add(attribute);
    }

    protected void addResource(ResourceType resource, URI id, String resourceName) {
        AttributeType attribute = new AttributeType();
        attribute.setAttributeId(id.toString());
        attribute.setDataType(new StringDataTypeAttribute());

        AttributeValueType attributeValue = new AttributeValueType();
        attributeValue.getContent().add(resourceName);

        attribute.getAttributeValues().add(attributeValue);
        resource.getAttributes().add(attribute);
    }

    protected void addGeoserverResource(ResourceType resource) {
        AttributeType attribute = new AttributeType();
        attribute.setAttributeId(XACMLConstants.ResourceAttributeId);
        attribute.setDataType(new StringDataTypeAttribute());

        AttributeValueType attributeValue = new AttributeValueType();
        attributeValue.getContent().add("GeoServer");

        attribute.getAttributeValues().add(attributeValue);
        resource.getAttributes().add(attribute);
    }

    protected void addOWSService(ResourceType resource) {
        org.geoserver.ows.Request owsRequest = Dispatcher.REQUEST.get();
        if (owsRequest == null)
            return;
        AttributeType attribute = new AttributeType();
        attribute.setAttributeId(XACMLConstants.OWSRequestResouceId);
        attribute.setDataType(new StringDataTypeAttribute());

        AttributeValueType attributeValue = new AttributeValueType();
        attributeValue.getContent().add(owsRequest.getRequest());

        attribute.getAttributeValues().add(attributeValue);
        resource.getAttributes().add(attribute);

        attribute = new AttributeType();
        attribute.setAttributeId(XACMLConstants.OWSServiceResouceId);
        attribute.setDataType(new StringDataTypeAttribute());

        attributeValue = new AttributeValueType();
        attributeValue.getContent().add(owsRequest.getService());

        attribute.getAttributeValues().add(attributeValue);
        resource.getAttributes().add(attribute);
    }

    protected void addGeometry(ResourceType resource, URI attributeURI, Geometry g,
            String srsName) {


        AttributeType attribute = new AttributeType();
        attribute.setAttributeId(attributeURI.toString());
        attribute.setDataType(new GeometryDataTypeAttribute());

        AttributeValueType attributeValue = new AttributeValueType();
        attributeValue.getContent().add(g);

        attribute.getAttributeValues().add(attributeValue);
        resource.getAttributes().add(attribute);
    }

    protected void addBbox(ResourceType resource) {
        org.geoserver.ows.Request owsRequest = Dispatcher.REQUEST.get();
        if (owsRequest == null)
            return;

        Map kvp = owsRequest.getKvp();
        if (kvp == null)
            return;

        ReferencedEnvelope env = (ReferencedEnvelope) kvp.get("BBOX");
        if (env == null)
            return;

        String srsName = (String) kvp.get("SRS");
        Geometry geom = JTS.toGeometry((Envelope) env);

        addGeometry(resource, XACMLConstants.BBoxResourceURI, geom, srsName);

    }

    abstract public RequestType createRequest();

}
