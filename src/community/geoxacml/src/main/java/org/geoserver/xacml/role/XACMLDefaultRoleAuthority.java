/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.xacml.role;

import com.vividsolutions.jts.geom.Geometry;
import org.geoserver.security.impl.GeoServerUser;
import org.geoserver.xacml.geoxacml.XACMLConstants;
import org.geoserver.xacml.geoxacml.XACMLUtil;
import org.geoserver.xacml.request.RequestCtxBuilder;
import org.geoserver.xacml.request.RequestCtxBuilderFactory;
import org.geotools.xacml.geoxacml.attr.GeometryDataTypeAttribute;
import org.geotools.xacml.transport.XACMLTransport;
import org.herasaf.xacml.core.context.impl.*;
import org.herasaf.xacml.core.dataTypeAttribute.impl.*;
import org.herasaf.xacml.core.policy.impl.AttributeAssignmentType;
import org.herasaf.xacml.core.policy.impl.AttributeSelectorType;
import org.herasaf.xacml.core.policy.impl.ObligationType;
import org.herasaf.xacml.core.policy.impl.ObligationsType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.net.URI;
import java.util.*;

/**
 * Spring Security implementation for {@link XACMLRoleAuthority}
 * 
 * 
 * @author Christian Mueller
 * 
 */
public class XACMLDefaultRoleAuthority implements XACMLRoleAuthority<GeoServerUser> {

    private static InheritableThreadLocal<Set<Authentication>> AlreadyPrepared = new InheritableThreadLocal<Set<Authentication>>();

    private RequestCtxBuilderFactory requestCtxBuilderFactory;
    private XACMLTransport transport;

    @Override
    public void transformUserDetails(GeoServerUser details){
        Collection<GrantedAuthority> authorities = (Collection<GrantedAuthority>)
                details.getAuthorities();
        Set<GrantedAuthority> newAuthorities = new HashSet();

        for (GrantedAuthority grantedAuthority : details.getAuthorities()) {
            XACMLRole role = XACMLUtil.toXACMLRoleFrom(grantedAuthority);
            newAuthorities.add(role);
        }

        details.setAuthorities(newAuthorities);

    }

    @Override
    public void prepareRoles(Authentication auth) {

        // Trying to avoid multiple processing within one thread, result cannot change
        if (AlreadyPrepared.get() == null) {
            AlreadyPrepared.set(new HashSet<Authentication>());
        }

        if (AlreadyPrepared.get().contains(auth)) {
            return; // nothing todo
        }

        List<RequestType> requests = new ArrayList<RequestType>(auth.getAuthorities().size());

        String userName = null;
        if (auth.getPrincipal() instanceof UserDetails)
            userName = ((UserDetails) auth.getPrincipal()).getUsername();
        if (auth.getPrincipal() instanceof String) {
            userName = auth.getPrincipal().toString();
        }

        for (GrantedAuthority ga : auth.getAuthorities()) {
            RequestCtxBuilder requestCtxBuilder = requestCtxBuilderFactory.getXACMLRoleRequestCtxBuilder(ga, userName);
            RequestType requestType = requestCtxBuilder.createRequest();
            requests.add(requestType);
        }

        List<ResponseType> responses = transport.evaluateRequestCtxList(
                requests);

        Object[] authorities = auth.getAuthorities().toArray();
        outer: for (int i = 0; i < responses.size(); i++) {
            ResponseType response = responses.get(i);
            XACMLRole role = XACMLUtil.toXACMLRoleFrom((GrantedAuthority)authorities[i]);
            for (ResultType result : response.getResults()) {
                if (result.getDecision() != DecisionType.PERMIT) {
                    role.setEnabled(false);
                    continue outer;
                }
                role.setEnabled(true);
                setUserProperties(auth, result, role);
            }

        }
        AlreadyPrepared.get().add(auth); // avoid further processing within one thread
    }

    private void setUserProperties(Authentication auth, ResultType result, XACMLRole role) {

        if (role.isRoleAttributesProcessed())
            return; // already done

        if (auth.getPrincipal() == null || auth.getPrincipal() instanceof String) {
            role.setRoleAttributesProcessed(true);
            return;
        }

        ObligationsType obligations = result.getObligations();
        if(obligations != null){
            for (ObligationType obligation : obligations.getObligations()){
                if (XACMLConstants.UserPropertyObligationId.equals(obligation.getObligationId()))
                    setRoleParamsFromUserDetails(auth, obligation, role);
                if (XACMLConstants.RoleConstantObligationId.equals(obligation.getObligationId()))
                    setRoleParamsFromConstants(obligation, role);

            }
        }
        role.setRoleAttributesProcessed(true);
    }

    private void setRoleParamsFromConstants(ObligationType obligation, XACMLRole role) {
        for (AttributeAssignmentType assignment : obligation.getAttributeAssignments()) {
            for(Object o : assignment.getContent()){
                if( o instanceof AttributeType){
                    role.getAttributes().add((AttributeType)o);
                }

                if(o instanceof AttributeSelectorType){
                    //The 'handle' method of AttributeSelectorType isn't implemented
                    //TODO: Write handler logic here?
                }
            }
        }
    }

    private void setRoleParamsFromUserDetails(Authentication auth, ObligationType obligation,
            XACMLRole role) {

        BeanInfo bi = null;
        try {
            bi = Introspector.getBeanInfo(auth.getPrincipal().getClass());
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }

        for (AttributeAssignmentType assignment : obligation.getAttributeAssignments()) {
            //TODO: Obligations are going to be no bueno here
            for(Object o: assignment.getContent()){
                AttributeType attr = null;
                if(o instanceof AttributeType){
                    attr = (AttributeType)o;
                }

                //TODO: Fix to use AttributeSelector logic

                if(attr != null){
                    String propertyName = attr.getAttributeValues().get(0).toString();
                    for (PropertyDescriptor pd : bi.getPropertyDescriptors()) {
                        if (pd.getName().equals(propertyName)) {
                            Serializable value = null;
                            try {
                                Object tmp = pd.getReadMethod().invoke(auth.getPrincipal(), new Object[0]);
                                if (tmp == null)
                                    continue;
                                if (tmp instanceof Serializable == false) {
                                    throw new RuntimeException("Role params must be serializable, "
                                            + tmp.getClass() + " is not");
                                }
                                value = (Serializable) tmp;
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            // special check for geometries
                            if (value instanceof Geometry) {
                                if (((Geometry) value).getUserData() == null)
                                    throw new RuntimeException("Property: " + propertyName
                                            + " : Geometry must have srs name as userdata");
                            }
                            AttributeType xacmlAttr = createAttributeValueFromObject(value, attr.getAttributeId());
                            role.getAttributes().add(xacmlAttr);
                        }
                    }
                }
            }
        }

    }

    protected AttributeType createAttributeValueFromObject(Serializable object, String attributeId) {
        AttributeType attr = new AttributeType();
        attr.setAttributeId(attributeId);
        AttributeValueType value = new AttributeValueType();
        value.getContent().add(object);
        attr.getAttributeValues().add(value);

        if (object instanceof String)
            attr.setDataType(new StringDataTypeAttribute());
        if (object instanceof URI)
            attr.setDataType(new AnyURIDataTypeAttribute());
        if (object instanceof Boolean)
            attr.setDataType(new BooleanDataTypeAttribute());
        if (object instanceof Double)
            attr.setDataType(new DoubleDataTypeAttribute());
        if (object instanceof Float)
            attr.setDataType(new DoubleDataTypeAttribute());
        if (object instanceof Integer)
            attr.setDataType(new IntegerDataTypeAttribute());
        if (object instanceof Date)
            attr.setDataType(new DateDataTypeAttribute());
        if (object instanceof Geometry) {
            attr.setDataType(new GeometryDataTypeAttribute());
        }

        return attr;
    }

    public RequestCtxBuilderFactory getRequestCtxBuilderFactory() {
        return requestCtxBuilderFactory;
    }

    @Autowired
    public void setRequestCtxBuilderFactory(RequestCtxBuilderFactory requestCtxBuilderFactory) {
        this.requestCtxBuilderFactory = requestCtxBuilderFactory;
    }

    public XACMLTransport getTransport() {
        return transport;
    }

    @Autowired
    public void setTransport(XACMLTransport transport) {
        this.transport = transport;
    }


}
