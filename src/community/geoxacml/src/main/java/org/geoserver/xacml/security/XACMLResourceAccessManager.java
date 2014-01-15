/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.xacml.security;

import org.geoserver.catalog.*;
import org.geoserver.security.*;
import org.geoserver.xacml.geoxacml.XACMLConstants;
import org.geoserver.xacml.geoxacml.XACMLUtil;
import org.geoserver.xacml.request.RequestCtxBuilderFactory;
import org.geoserver.xacml.role.XACMLRole;
import org.geoserver.xacml.role.XACMLRoleAuthority;
import org.geotools.xacml.transport.XACMLTransport;
import org.herasaf.xacml.core.SyntaxException;
import org.herasaf.xacml.core.context.impl.DecisionType;
import org.herasaf.xacml.core.context.impl.RequestType;
import org.herasaf.xacml.core.context.impl.ResponseType;
import org.herasaf.xacml.core.context.impl.ResultType;
import org.herasaf.xacml.core.policy.impl.AttributeAssignmentType;
import org.herasaf.xacml.core.policy.impl.ObligationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class XACMLResourceAccessManager implements ResourceAccessManager, DataAccessManager {

    private CatalogMode mode;

    private final Object modeLock = new Object();

    private final DataAccessManagerAdapter adapter;

    private final XACMLRoleAuthority xacmlRoleAuthority;
    private final XACMLTransport xacmlTransport;
    private final RequestCtxBuilderFactory requestCtxBuilderFactory;

    private static final Logger Log =
            Logger.getLogger(XACMLResourceAccessManager.class.getName());

    private static final Map<String, CatalogMode> CatalogModeMap;
    static {
        CatalogModeMap = new HashMap<String, CatalogMode>(3);
        CatalogModeMap.put("HIDE", CatalogMode.HIDE);
        CatalogModeMap.put("CHALLENGE", CatalogMode.CHALLENGE);
        CatalogModeMap.put("MIXED", CatalogMode.MIXED);
    }

    @Autowired
    public XACMLResourceAccessManager(RequestCtxBuilderFactory requestCtxBuilderFactory, XACMLRoleAuthority xacmlRoleAuthority, XACMLTransport xacmlTransport) {
        this.xacmlRoleAuthority = xacmlRoleAuthority;
        this.xacmlTransport = xacmlTransport;
        this.requestCtxBuilderFactory = requestCtxBuilderFactory;
        this.adapter = new DataAccessManagerAdapter(this);
    }

    @Override
    public boolean canAccess(Authentication user, WorkspaceInfo workspace, AccessMode mode) {
        //TODO: validate this never, ever occurs except on init
        if(user == null){
            return true;
        }

        xacmlRoleAuthority.prepareRoles(user);
        List<RequestType> requestCtxts = buildWorkspaceRequestCtxListFromRoles(user, workspace, mode);
        if (requestCtxts.isEmpty())
            return false;

        List<ResponseType> responseCtxts = xacmlTransport
                .evaluateRequestCtxList(requestCtxts);

        DecisionType xacmlDecision = XACMLUtil.getDecisionFromRoleResponses(responseCtxts);

        if(xacmlDecision == DecisionType.PERMIT)
            return true;

        return false;
    }

    @Override
    public boolean canAccess(Authentication user, LayerInfo layer, AccessMode mode) {
        return canAccess(user, layer.getResource(), mode);
    }

    @Override
    public boolean canAccess(Authentication user, ResourceInfo resource, AccessMode mode) {

        //TODO: validate this never, ever occurs except on init
        if(user == null){
            return true;
        }

        xacmlRoleAuthority.prepareRoles(user);
        List<RequestType> requestCtxts = buildResourceInfoRequestCtxListFromRoles(user, resource,
                mode);
        if (requestCtxts.isEmpty())
            return false;

        List<ResponseType> responseCtxts = xacmlTransport
                .evaluateRequestCtxList(requestCtxts);

        DecisionType xacmlDecision = XACMLUtil.getDecisionFromRoleResponses(responseCtxts);

        if (xacmlDecision == DecisionType.PERMIT)
            return true;
        return false;
    }

    @Override
    public CatalogMode getMode() {
        synchronized (modeLock) {
            if (mode != null)
                return mode;

            RequestType requestCtx = requestCtxBuilderFactory.getCatalogRequestCtxBuilder().createRequest();
            ResponseType responseCtx = xacmlTransport.evaluateRequestCtx(
                    requestCtx);

            ResultType result = responseCtx.getResults().iterator().next();
            if (result == null || result.getDecision() != DecisionType.PERMIT){
                Log.severe("Geserver cannot access its catalog !!!");
                Log.severe(XACMLUtil.asXMLString(requestCtx));
                return useDefaultMode();
            }

            ObligationType obligation = null;
            try{
                obligation = result.getObligations().getObligations().iterator().next();
            }catch(NullPointerException e){
                //ignore
            }

            if (obligation == null
                    || XACMLConstants.CatalogModeObligationId.equals(obligation.getObligationId()
                            .toString()) == false) {
                Log.severe("No obligation with id: " + XACMLConstants.CatalogModeObligationId);
                Log.severe(XACMLUtil.asXMLString(requestCtx));
                return useDefaultMode();
            }

            AttributeAssignmentType attributeAssignment = obligation.getAttributeAssignments().iterator().next();

            String catalogModeAssignment = null;
            try {
                catalogModeAssignment = (attributeAssignment == null) ? null :
                    attributeAssignment.getDataType().convertTo(attributeAssignment.getContent()).toString();
            }catch(SyntaxException e){
                Log.log(Level.SEVERE, "Error while processing AttributeAssignment value", e);
            }

            if (catalogModeAssignment == null
                    || CatalogModeMap.containsKey(catalogModeAssignment) == false){
                Log.severe("No valid catalog mode ");
                Log.severe(XACMLUtil.asXMLString(requestCtx));
                return useDefaultMode();
            }

            String catalogModeKey = (catalogModeAssignment);
            mode = CatalogModeMap.get(catalogModeKey);
            return mode;
        }

    }

    private CatalogMode useDefaultMode() {
        Log.log(Level.INFO, "Falling back to CatalogMode {0}", CatalogMode.HIDE);
        mode = CatalogMode.HIDE;
        return mode;
    }

    private List<RequestType> buildWorkspaceRequestCtxListFromRoles(Authentication auth,
            WorkspaceInfo workspaceInfo, AccessMode mode) {

        List<RequestType> resultList = new ArrayList<RequestType>();

        for (GrantedAuthority role : auth.getAuthorities()) {
            XACMLRole xacmlRole = XACMLUtil.toXACMLRoleFrom(role);
            if (xacmlRole.isEnabled() == false)
                continue;
            RequestType requestCtx = requestCtxBuilderFactory.getWorkspaceRequestCtxBuilder(xacmlRole, workspaceInfo, mode)
                    .createRequest();
            resultList.add(requestCtx);
        }

        return resultList;
    }

    private List<RequestType> buildResourceInfoRequestCtxListFromRoles(Authentication auth,
            ResourceInfo resourceInfo, AccessMode mode) {

        List<RequestType> resultList = new ArrayList<RequestType>();

        for (GrantedAuthority role : auth.getAuthorities()) {
            XACMLRole xacmlRole = XACMLUtil.toXACMLRoleFrom(role);
            if (xacmlRole.isEnabled() == false)
                continue;
            RequestType requestCtx = requestCtxBuilderFactory.getResourceInfoRequestCtxBuilder(xacmlRole, resourceInfo, mode)
                    .createRequest();
            resultList.add(requestCtx);
        }

        return resultList;
    }

    @Override
    public WorkspaceAccessLimits getAccessLimits(Authentication user, WorkspaceInfo workspace) {
        //TODO: validate this never, ever occurs except on init
        if(user == null){
            return null;
        }
        return adapter.getAccessLimits(user, workspace);
    }

    @Override
    public DataAccessLimits getAccessLimits(final Authentication user, final LayerInfo layer) {
        //TODO: validate this never, ever occurs except on init
        if(user == null){
            return null;
        }
        return adapter.getAccessLimits(user, layer);
    }

    @Override
    public DataAccessLimits getAccessLimits(Authentication user, ResourceInfo resource) {
        //TODO: validate this never, ever occurs except on init
        if(user == null){
            return null;
        }
        return adapter.getAccessLimits(user, resource);
    }

    @Override
    public StyleAccessLimits getAccessLimits(Authentication user, StyleInfo style) {
        //TODO: validate this never, ever occurs except on init
        if(user == null){
            return null;
        }
        return adapter.getAccessLimits(user, style);
    }

    @Override
    public LayerGroupAccessLimits getAccessLimits(Authentication user, LayerGroupInfo layerGroup) {
        //TODO: validate this never, ever occurs except on init
        if(user == null){
            return null;
        }
        return adapter.getAccessLimits(user, layerGroup);
    }

}
