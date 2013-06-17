/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.xacml.spring.security;

import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.geoserver.security.impl.GeoServerUser;
import org.geoserver.xacml.geoxacml.XACMLConstants;

import org.geoserver.xacml.role.XACMLRole;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationDetailsSourceImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

/**
 * Creating an AnoynmaAuthenticationToken with XACML Roles
 * 
 * @author Christian Mueller
 * 
 */
public class XACMLAnonymousAuthenticationFilter extends AnonymousAuthenticationFilter {

    private AuthenticationDetailsSource authenticationDetailsSource = new AuthenticationDetailsSourceImpl();
    private static final String KEY = "geoserver";

    public XACMLAnonymousAuthenticationFilter(){
        super(KEY, GeoServerUser.ANONYMOUS_USERNAME, Arrays.asList(
                new GrantedAuthority[] {
                    new XACMLRole("anonymousUser"),
                    new XACMLRole(XACMLConstants.AnonymousRole)
                }));
    }

    @Override
    protected Authentication createAuthentication(HttpServletRequest request) {
        List<GrantedAuthority> auths = this.getAuthorities();
        XACMLRole[] roles = new XACMLRole[auths.size()];
        for (int i = 0; i < auths.size(); i++) {
            roles[i] = new XACMLRole(auths.get(i).getAuthority());
            roles[i].setRoleAttributesProcessed(true); // No userinfo for anonymous
        }

        AnonymousAuthenticationToken auth = new AnonymousAuthenticationToken(KEY, this.getPrincipal(), this.getAuthorities());

        auth.setDetails(authenticationDetailsSource.buildDetails((HttpServletRequest) request));
        return auth;
    }

    @Override
    public void setAuthenticationDetailsSource(
            AuthenticationDetailsSource authenticationDetailsSource) {
        super.setAuthenticationDetailsSource(authenticationDetailsSource);
        this.authenticationDetailsSource = authenticationDetailsSource;
    }

}
