/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.xacml.geoxacml;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.geoserver.xacml.role.XACMLRole;
import org.geoserver.xacml.spring.security.XACMLFilterDecisionVoter;

import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.ctx.Attribute;
import com.sun.xacml.ctx.RequestCtx;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.FilterInvocation;

/**
 * Testing URL matching
 * 
 * 
 * @author Christian Mueller
 * 
 */
public class XACMLURLMatchingTest extends TestCase {
    Authentication anonymous, admin, authenticated;

    @Override
    protected void setUp() throws Exception {

        super.setUp();
        GeoXACMLConfig.setPolicyRepsoitoryBaseDir("src/test/resources/urltest/");
        GeoXACMLConfig.reset();
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

        authorities.add(new XACMLRole(XACMLConstants.AnonymousRole));

        anonymous = new TestingAuthenticationToken((Object)"anonymous", (Object)"passwd",
                authorities);

        authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new XACMLRole(XACMLConstants.AdminRole));
        
        admin = new TestingAuthenticationToken((Object)"admin", (Object)"passwd",
                authorities);


        authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new XACMLRole(XACMLConstants.Authenticated));

        authenticated = new TestingAuthenticationToken((Object)"xy", (Object)"passwd",
                authorities);
    }

    public void testGeoXACMLURL() {

        assertTrue(AccessDecisionVoter.ACCESS_GRANTED == executeFor(anonymous,
                "/security/geoxacml", "GET"));
        assertTrue(AccessDecisionVoter.ACCESS_GRANTED == executeFor(authenticated,
                "/security/geoxacml", "GET"));
        assertTrue(AccessDecisionVoter.ACCESS_GRANTED == executeFor(admin, "/security/geoxacml",
                "GET"));
    }

    public void testConfigURL() {
        assertTrue(AccessDecisionVoter.ACCESS_DENIED == executeFor(anonymous, "/COnFig/abc", "GET"));
        assertTrue(AccessDecisionVoter.ACCESS_DENIED == executeFor(authenticated, "/COnFig/abc",
                "GET"));
        assertTrue(AccessDecisionVoter.ACCESS_GRANTED == executeFor(admin, "/COnFig/abc", "GET"));
    }

    public void testRestURL() {
        assertTrue(AccessDecisionVoter.ACCESS_GRANTED == executeFor(anonymous, "/rest/abc", "GET"));
        assertTrue(AccessDecisionVoter.ACCESS_GRANTED == executeFor(authenticated, "/rest/abc",
                "GET"));
        assertTrue(AccessDecisionVoter.ACCESS_GRANTED == executeFor(admin, "/rest/abc", "GET"));

        String methods[] = new String[] { "POST", "PUT", "DELETE" };

        for (String method : methods) {
            assertTrue(AccessDecisionVoter.ACCESS_DENIED == executeFor(anonymous, "/rest/", method));
            assertTrue(AccessDecisionVoter.ACCESS_GRANTED == executeFor(authenticated, "/rest/",
                    method));
            assertTrue(AccessDecisionVoter.ACCESS_GRANTED == executeFor(admin, "/rest/", method));
        }
    }

    public void testHttpParams() {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("param1", "value1");
        paramMap.put("param2", new String[] { "value2" });
        RequestCtx request = GeoXACMLConfig.getRequestCtxBuilderFactory()
                .getURLMatchRequestCtxBuilder((XACMLRole) anonymous.getAuthorities().toArray()[0], "/rest/",
                        "GET", paramMap,"127.0.0.1", "localhost").createRequestCtx();

        //System.out.println(XACMLUtil.asXMLString(request));
        int count = 0;
        for (Attribute attr : request.getResource()) {
            if (attr.getId().toString().equals(XACMLConstants.URLParamPrefix + "param1")) {
                assertTrue(((StringAttribute) attr.getValue()).getValue().equals("value1"));
                count++;
            }
            if (attr.getId().toString().equals(XACMLConstants.URLParamPrefix + "param2")) {
                assertTrue(((StringAttribute) attr.getValue()).getValue().equals("value2"));
                count++;
            }
        }
        assertTrue(count == 2);

    }

    private int executeFor(Authentication aut, String path, String method) {

        HttpServletRequest mockRequest = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(mockRequest.getMethod()).andReturn(method).anyTimes();
        EasyMock.expect(mockRequest.getServletPath()).andReturn(path).anyTimes();
        EasyMock.expect(mockRequest.getParameterMap()).andReturn(null).anyTimes();
        EasyMock.expect(mockRequest.getRemoteHost()).andReturn("localhost").anyTimes();
        EasyMock.expect(mockRequest.getRemoteAddr()).andReturn("127.0.0.1").anyTimes();
        EasyMock.replay(mockRequest);

        FilterInvocation filter = org.easymock.classextension.EasyMock
                .createMock(FilterInvocation.class);
        org.easymock.classextension.EasyMock.expect(filter.getRequestUrl()).andReturn(path)
                .anyTimes();
        org.easymock.classextension.EasyMock.expect(filter.getHttpRequest()).andReturn(mockRequest)
                .anyTimes();

        org.easymock.classextension.EasyMock.replay(filter);

        XACMLFilterDecisionVoter voter = new XACMLFilterDecisionVoter();

        List<ConfigAttribute> configAttributes = new ArrayList<ConfigAttribute>();
        configAttributes.add(new SecurityConfig("xacml"));

        return voter.vote(aut, filter, configAttributes);
    }

}
