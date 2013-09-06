/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.xacml.spring.security;

import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.xacml.geoxacml.XACMLUtil;
import org.geoserver.xacml.request.RequestCtxBuilderFactory;
import org.geoserver.xacml.role.XACMLRole;
import org.geoserver.xacml.role.XACMLRoleAuthority;
import org.geotools.xacml.transport.XACMLTransport;
import org.herasaf.xacml.core.context.impl.DecisionType;
import org.herasaf.xacml.core.context.impl.RequestType;
import org.herasaf.xacml.core.context.impl.ResponseType;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.FilterInvocation;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Spring Security Decision Voter using XACML policies
 * 
 * @author Christian Mueller
 * 
 */
public class XACMLFilterDecisionVoter<S> implements AccessDecisionVoter<S> {

    private final XACMLRoleAuthority roleAuthority;
    private final XACMLTransport transport;
    private final RequestCtxBuilderFactory factory;

    public XACMLFilterDecisionVoter(){
        this.roleAuthority = GeoServerExtensions.bean(XACMLRoleAuthority.class);
        this.transport = GeoServerExtensions.bean(XACMLTransport.class);
        this.factory = GeoServerExtensions.bean(RequestCtxBuilderFactory.class);
    }

    @Override
    public boolean supports(ConfigAttribute attr) {
        return true;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return true;
    }

    @Override
    public int vote(Authentication auth, S request, Collection<ConfigAttribute> arg2) {
        
        HttpServletRequest httpRequest = ((FilterInvocation) request).getHttpRequest();
        String urlPath = httpRequest.getServletPath().toLowerCase();
        // String urlPath = ((FilterInvocation) request).getRequestUrl().toLowerCase();
        String method = httpRequest.getMethod();
        Map<String, Object> httpParams = httpRequest.getParameterMap();
        
        String remoteIP = httpRequest.getRemoteAddr();
        String remoteHost = httpRequest.getRemoteHost();
        

        List<RequestType> requestCtxts = buildRequestCtxListFromRoles(auth, urlPath, method,
                httpParams, remoteIP,remoteHost);
        if (requestCtxts.isEmpty())
            return XACMLDecisionMapper.Exact.getSpringSecurityDecisionFor(DecisionType.DENY);

        List<ResponseType> responseCtxts = transport.evaluateRequestCtxList(requestCtxts);

        DecisionType xacmlDecision = XACMLUtil.getDecisionFromRoleResponses(responseCtxts);
        return XACMLDecisionMapper.Exact.getSpringSecurityDecisionFor(xacmlDecision);

    }

    private List<RequestType> buildRequestCtxListFromRoles(Authentication auth, String urlPath,
            String method, Map<String, Object> httpParams,String remoteIP,String remoteHost) {

        roleAuthority.prepareRoles(auth);

        List<RequestType> resultList = new ArrayList<RequestType>();

        for (GrantedAuthority role : auth.getAuthorities()) {
            XACMLRole xacmlRole = (XACMLRole) role;
            if (xacmlRole.isEnabled() == false)
                continue;
            RequestType requestCtx = factory.getURLMatchRequestCtxBuilder(xacmlRole, urlPath, method, httpParams,remoteIP,remoteHost)
                    .createRequest();
            resultList.add(requestCtx);
        }

        return resultList;
    }

}
