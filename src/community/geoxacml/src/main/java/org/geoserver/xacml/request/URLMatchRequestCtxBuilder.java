/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.xacml.request;

import org.geoserver.xacml.geoxacml.XACMLConstants;
import org.geoserver.xacml.role.XACMLRole;
import org.herasaf.xacml.core.context.impl.*;
import org.herasaf.xacml.core.dataTypeAttribute.impl.DnsNameDataTypeAttribute;
import org.herasaf.xacml.core.dataTypeAttribute.impl.IpAddressDataTypeAttribute;

import java.net.*;
import java.util.Map;
import java.util.Map.Entry;


/**
 * Builds a request for URL Matching against regular expressions Http parameters are encoded as
 * resources
 * 
 * 
 * @author Christian Mueller
 * 
 */
public class URLMatchRequestCtxBuilder extends RequestCtxBuilder {
    private String urlString = null, remoteHost = null, remoteIP = null;

    private Map<String, Object> httpParams;

    public String getUrlString() {
        return urlString;
    }

    public URLMatchRequestCtxBuilder(XACMLRole role, String urlString, String method,
            Map<String, Object> httpParams, String remoteIP, String remoteHost) {
        super(role, method);
        this.urlString = urlString;
        this.httpParams = httpParams;
        this.remoteHost = remoteHost;
        this.remoteIP = remoteIP;
    }

    @Override
    public RequestType createRequest() {

        SubjectType subject = new SubjectType();
        addRole(subject);

        ResourceType resource = new ResourceType();
        addGeoserverResource(resource);

        if(urlString != null)
            addResource(resource, XACMLConstants.URlResourceURI, urlString.equals("") ? "/" : urlString);

        if (httpParams != null && httpParams.size() > 0) {
            for (Entry<String, Object> entry : httpParams.entrySet()) {
                URI paramURI = null;
                try {
                    paramURI = new URI(XACMLConstants.URLParamPrefix + entry.getKey());
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e); // should never happen
                }
                if (entry.getValue() instanceof String[]) {
                    for (String value : (String[]) entry.getValue()) {
                        addResource(resource, paramURI, value);
                    }
                } else {
                    addResource(resource, paramURI, entry.getValue().toString());
                }

            }
        }


        ActionType action = new ActionType();
        addAction(action);

        EnvironmentType environment = new EnvironmentType();
        try {
            if (remoteHost != null) {
                AttributeValueType value = new AttributeValueType();
                AttributeType attr = new AttributeType();

                attr.setAttributeId(XACMLConstants.DNSNameEnvironmentId);
                attr.setDataType(new DnsNameDataTypeAttribute());
                attr.getAttributeValues().add(value);

                value.getContent().add(remoteHost);
                environment.getAttributes().add(attr);
            }
            if (remoteIP != null) {
                AttributeValueType value = new AttributeValueType();
                AttributeType attr = new AttributeType();

                attr.setAttributeId(XACMLConstants.IPAddressEnvironmentId);
                attr.setDataType(new IpAddressDataTypeAttribute());
                attr.getAttributeValues().add(value);
                value.getContent().add(remoteIP);
                value.getContent().add(remoteIP);

                environment.getAttributes().add(attr);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex); // should not happen
        }
        
        

        RequestType ctx = new RequestType();
        ctx.setAction(action);
        ctx.setEnvironment(environment);
        ctx.getResources().add(resource);
        ctx.getSubjects().add(subject);

        return ctx;
    }

}
