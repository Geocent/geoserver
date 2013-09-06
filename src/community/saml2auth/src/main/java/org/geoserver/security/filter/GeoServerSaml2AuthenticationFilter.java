package org.geoserver.security.filter;

/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml.SAMLEntryPoint;
import org.springframework.security.saml.metadata.MetadataGeneratorFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.FilterChainProxy;

import javax.servlet.*;
import java.io.IOException;

/**
 * SAML2 authentication filter
 * 
 */
public class GeoServerSaml2AuthenticationFilter extends
        GeoServerCompositeFilter implements GeoServerAuthenticationFilter {
    
    private static final Log logger = LogFactory.getLog(GeoServerSaml2AuthenticationFilter.class);

    private SAMLEntryPoint aep;
    private FilterChainProxy samlFilter;

    private String roleServiceName;

    //TODO: use role service to handle building GrantedAuthorities
    public String getRoleServiceName() {
        return roleServiceName;
    }

    public void setRoleServiceName(String roleServiceName) {
        this.roleServiceName = roleServiceName;
    }

    @Override
    public void initializeFromConfig(SecurityNamedServiceConfig config)
            throws IOException {
        super.initializeFromConfig(config);
        
        logger.debug("Initializing Saml2AuthenticationFilter");

        //TODO: build these here from config instead of using the spring beans
        samlFilter = (FilterChainProxy) GeoServerExtensions.bean("samlFilter");
        aep = (SAMLEntryPoint) GeoServerExtensions.bean("samlEntryPoint");
        MetadataGeneratorFilter metadataGeneratorFilter = (MetadataGeneratorFilter) GeoServerExtensions
                .bean("metadataGeneratorFilter");

        this.nestedFilters.add(metadataGeneratorFilter);
        this.nestedFilters.add(samlFilter);

        logger.debug("Saml2AuthenticationFilter initialized");
    }

    @Override
    public AuthenticationEntryPoint getAuthenticationEntryPoint() {
        return aep;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
            FilterChain chain) throws IOException, ServletException {
        
        logger.debug("doFilter on Saml2AuthenticationFilter");

        req.setAttribute(GeoServerSecurityFilter.AUTHENTICATION_ENTRY_POINT_HEADER, aep);

        Authentication authn = SecurityContextHolder.getContext().getAuthentication();


        if (authn != null && authn.isAuthenticated()) {
            logger.debug("Pre-authenticated as " + authn.getPrincipal().toString() + " by " + authn.getName());
        }

        super.doFilter(req, res, chain);

        authn = SecurityContextHolder.getContext().getAuthentication();
        if (authn != null && authn.isAuthenticated()) {
            logger.debug("Authenticated as " + authn.getPrincipal().toString() + " by " + authn.getName());
        }
    }

    /**
     * @see org.geoserver.security.filter.GeoServerAuthenticationFilter#applicableForHtml()
     */
    @Override
    public boolean applicableForHtml() {
        return true;
    }

    /**
     * @see org.geoserver.security.filter.GeoServerAuthenticationFilter#applicableForServices()
     */
    @Override
    public boolean applicableForServices() {
        return true;
    }

}
